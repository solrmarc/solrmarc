

# Introduction #

The specification of which MARC fields and subfields are to be mapped to which Solr index fields is handled via an index.properties configuration file, the exact name of which is specified by the `solr.indexer.properties` entry in the config.properties file as described on the [ConfigProperties](ConfigProperties.md) page. Since the field specification configuration file is a properties file, there are certain constraints of how the file is structured. Basically all properties files consist of a number of pairs of values separated by an equals sign. The key values represent the name of the field that will be added to the Solr document. All of these must either match a field definition that occurs in the _schema.xml_ file for the Solr search engine, or they must match a !dynamicField definition in that file.

If any Solr field entries are listed here that do not match either a field definition or a !dynamicField definition from the _schema.xml_ file, the indexing will fail.

Other indexing errors can occur that will not prevent the creation of a Solr index.  For example, if a MARC record cannot be read due to a munged leader field, an error message is printed and the record is skipped.  Another example:  a Solr field is not defined as !multiValued in the Solr schema.xml file, but multiple values are created by SolrMarc (e.g. because the MARC record has multiple 245 fields).

```
id = 001, first
author_text = 100a:110a:111a:130a
author_display = 100a:110a
published_text = 260a
material_type_text = 300a
notes_text = 500a:505a
uniform_title_text = 240a:240b
uniform_title_display = 240a
uniform_subtitle_display = 240b
marc_display = FullRecordAsXML
marc_text = custom, getAllSearchableFields(100, 900)

title_text = 245a:245b:240a:240b:700t 
title_display = 245a
subtitle_display = 245b 
title_added_entry_display = 700t
call_number_text = custom, getCallNumberCleaned
call_number_display = 999a:090a:050a, first
year_multisort_i = DateOfPublication
isbn_text = 020a
isbn_display = 020a
oclc_text = custom, getOclcNum
        
call_number_facet = custom, getCallNumberPrefix
date_indexed_facet = index_date
source_facet = "Library Catalog"
subject_era_facet = 650d:650y:651y:655y
topic_form_genre_facet = 650a:650b:650x:655a
subject_geographic_facet = 650c:650z:651a:651x:651z:655z
broad_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first
format_facet = 999t, format_maps.properties(format)
language_facet = 008[35-37]:041a:041d, language_map.properties
location_facet = 999k:999l, location_map.properties
library_facet = 999m, library_map.properties
instrument_facet = 048a[0-1], instrument_map.properties
recording_type_facet = 000[6], (map.recording_type)
recordings_and_scores_facet = custom, getRecordingAndScore
recording_format_facet = custom, getRecordingFormat, format_maps.properties(recording_format)
music_catagory_facet = 999a[0-1]:999a[0], music_maps.properties(music_catagory), first
ports_of_call_facet = 650c:650z:651a:651x:651z:655z, semester_at_sea.properties(port_of_call)
guide_book_facet = 651v, (pattern_map.guide_book)
composition_era_facet = era, music_maps.properties(composition_era)
```

If any duplicates are found in the index specification file, the last entry that occurs will be used, and previous entries will be silently ignored. Also note that due to how java handles properties files, the order in which the entries occur is unimportant, if there is some reason you need the index fields to be added to the Solr index in a certain order, you will need to customize this program.

The values that are defined for the index field entries consist of 1 to 3 fields separated by commas.  The first field specifies either the MARC field or fields that the index entry should be extracted from or it indicates that the field is a special case.  These special cases are described below:

The simplest case is when a quoted string appears after the equals sign. Everything that appears in the quotes is taken verbatim and added to the Solr index as is. So in the above example, every record added to the Solr index by this program will have a value of `“Library Catalog”` stored for the index field named `source_facet`.  This can be useful when data from several different sources is being added to the same Solr index, to allow searchers to narrow their search to data from one or another of the sources.


If the value after the equals sign is `FullRecordAsMARC`, `FullRecordAsXML`, `date`, `index_date`, or `custom` the following actions will be taken:

  * **FullRecordAsMARC:** specifies that the entire MARC record should be added in the standard binary form (ISO 2709).

  * **FullRecordAsXML:** specifies that the entire MARC record should be added encoded using the MARCXML standard.

  * **FullRecordAsText:** specifies that the entire MARC record should be translated to a readable format, and stored, (with `<br/>` tags being inserted in place of newline characters.

  * **date:** extracts the 260 c subfield from the MARC record, and then extracts a four digit year from that subfield.

  * **index\_date:** returns a value for the date and time of when the record was indexed.

  * **custom:** specifies that a custom java routine is to be invoked to extract the value for this field. Examples of when custom might be necessary are:
    1. Create an index entry based on the value in field X, but only if a certain value appears in field Y.
    1. Create an index entry that consists of the first characters from a given field, but only as many characters as are letters.
    1. Create an index entry based on a portion of a given field, but only if the remaining portion of that field contains a certain value.
> Many custom indexing functions are provided in the SolrMarc code; you can also write your own. Details of how to define a custom indexing routine are explained later in this document.

If the value after the equals sign is not one of these special cases entries, it is assumed to be a list of MARC fields from which to extract the data to use for the index field.

# Specifying Which Fields and Subfields to Use #

The syntax for specifying what fields/subfields (or what portion of a field or subfield) is to be looked-at to create the Solr index field(s) consists of one or more field specifications separated by colons (:).

A field specification consists of a three-digit string (000 – 999) optionally followed by characters indicating which subfields and/or bytes to use.

  * **no subfields specified**, e.g `100` - all subfields of the specific Marc field, in order of occurrence in the Marc record, will be concatenated into a single value.  Each occurrence of the Marc field will create separate instance of the Solr field in the Solr document.

  * **single letter after the field**, e.g. `041a` - for each occurrence of the Marc field, each occurrence of the subfield will create a Solr field instance of the contents of the subfield.

  * **same single letter after the field repeated**, e.g. `650aa` - for each occurrence of the Marc field, one space separated concatenation of all occurrences of the subfield will be a Solr field instance in the Solr doc.

  * **multiple letters after the field**, e.g. `100abcdq` - for each occurrence of the field, all the indicated subfields, in order of occurrence in the Marc record, will be concatenated into a single value.  Each occurrence of the Marc field will create separate instance of the Solr field in the Solr document.

  * **square brackets containing digit pattern** for a fixed length field (i.e. leader, 001-009), , e.g. `008[35-37]` or `000[5]` - the digits in the brackets indicate the characters to be used as a value. The counting is 0-based: the first byte in the fixed field is 0.  `008[35-37]` will return the three character sequence at bytes 35,36,37 in the 008 field.  Each instance of the Marc field (e.g. for an 007, which is repeatable) will create a separate instance of the Solr field in the Solr document.
> > NOTE: as of 2009-08-27, numbers will grab bytes for all fields, not just fixed fields.  This is intended to change to the behavior described.

  * **square brackets indicating a regular expression describing subfields** for a variable length field, , e.g. `110[a-z]` or `243[a-gk-s]`. for each occurrence of the field, all the indicated subfields, in order of occurrence in the Marc record, will be concatenated into a single value.  Each occurrence of the Marc field will create separate instance of the Solr field in the Solr document.
> > NOTE: as of 2009-08-27, the distinction is letters vs. digits.  This intended to change to allow numeric subfields in the future.

TODO: document specification of separator.

There are additional ways to generate Solr fields from your MARC data explained below.

## Example Field Specifications ##

`full_title_display = 245`


> for _each_ MARC 245 field, concatenate _all subfield_ values, separated by a space, then add a field named full\_title\_display to the Solr document with the concatenated value.  Note that there is a single Solr field occurrence for each specified MARC _field_.

`brief_title_display = 245a`

> for _each_ subfield _a_ in each of the 245 fields in the MARC record, add a field to the Solr document named brief\_title\_display, with the value in from the MARC 245 subfield a.  Note that there is a single Solr field occurrence for each specified MARC _subfield_ in the MARC _field_.
Aside:  since the MARC specification states that there can only be a single 245 field and only a single subfield _a_, the results of this specification will be identical to what they would be if the field specification was`brief_title_display = 245a, first`.

`author_text = 100a:110a:111a:130a`

> for _each_ 1) subfield a in each of the 100 fields 2) subfield a in each of the 110 fields 3) each subfield a in each of the 111 fields 4) subfield a in each of the 130 fields in the MARC record, add a field to the Solr document named author\_text with the value from the MARC subfield specified.  Note that each of these values is added as a separate Solr field in the Solr document.

`fruit_text = 999a`

Given a record that contained two 999 fields as follows:

> 999 $a apricot
> 999 $a apple $b banana $a aardvark

would produce:   fruit\_text:apricot   and   fruit\_text:apple   and    fruit\_text:aardvark

`all_fruit_text = 999ab`

Given a record that contained two 999 fields as follows:

> 999 $a apricot
> 999 $a apple $b banana $a aardvark

would produce:  all\_fruit\_text:apricot   and    all\_fruit\_text:apple banana aardvark

`author_addl_t = 700abcegqu:710abcdegnu:711acdegjnqu`

> for _each_ occurrence of a specified MARC field (700, 710 and 711), concatenate the values of the specified subfields, with a space separator, then add a field named author\_addl\_t to the Solr document with the concatenated value.  Note that there is a single Solr field occurrence for each specified MARC _field_.

`material_type_display = 300aa`

> for _each_ MARC 300 field, concatenate all subfield _a_ values, separated by a space, then add a field named material\_type\_display to the Solr document with the concatenated value.  Note that there is a single Solr field occurrence for each MARC _field_, not each MARC _subfield_.

`title_added_entry_t = 700[gk-pr-t]:710[fgk-t]:711fgklnpst:730[a-gk-t]:740anp`

> The subfields specified here are regular expressions.  This field spec is equivalent to
> `title_added_entry_t = 700gklmnoprst:710fgklmnopqrst:711fgklnpst:730abcdefgklmnopqrst]:740anp`

`language_facet = 008[35-37]:041a:041d, language_map.properties`

> This field specification states that characters 35 through 37 should be selected from _each_ 008 control field of the MARC record (which is where a three-letter encoding of the primary language of a bibliographic work is found.)  Additionally, _each_ occurrence of the _a_ and _d_ subfields of all 041 fields in the MARC record become individual Solr fields named language\_facet in the Solr document.

> Note that a second parameter is present on the field specification entry: `language_map.properties`.  If this optional parameter is present, once the set of strings is created for all of the fields and subfields specified in the first parameter, the entire set is translated using the translation map that is defined in the separate property file named `language_map.properties`, (which maps the three-letter abbreviations for languages to the full name of that language; Hence "eng" becomes "English," "fre" becomes "French," "chp" becomes "Chipewyan," and "peo" becomes the ever-popular "Old Persian (ca. 600-400 B.C.)."  The details of how to define a translation map is covered in the next section.

> (Naomi doesn't believe this is true anymore - 2009-08-28:  you CANNOT specify 041ad in the field specification, each colon separated item can only reference a single field and subfield.)

`broad_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first`

> This field specification states that the value of character 6 (counting from 0) of field 000 (which stands for the leader of the MARC record) and character 0 of field 007 are to be extracted.  Both of these values are to then be translated using the translation map that is defined in the separate property file named `format_maps.properties`, by loading all the entries there that start with the string `broad_format`. The first translated value is to be used as the value for the Solr index entry.

Or to put it more succinctly, look up character 6 of the 000 field in the map `broad_format`, if the map contains a mapping for that character, use that value; otherwise, look up character 0 of the 007 field in the map `broad_format`, if the map contains a mapping for that character use that value. If neither extracted value matches an entry in the translation map, check to see whether the map defines a default value, if so use that default value, otherwise leave the `broad_format_facet` index entry unassigned.

# Mapping "Raw" Values to New Values #

The process of creating a translation map to translate from the cryptic entries found in the MARC record to more human-readable strings to make searching and faceting more useful to the end-user of the system, is fairly straightforward.  The first thing that you must do is add a second parameter on the field specification entry in the properties file, as shown in the last two of the examples shown above. This parameter specifies either the name of a separate property file that contains the map or the name of a separate property file plus the name of the property key prefix that should be looked for in that file.  For the last example above, a map named `broad_format` is referenced in the properties file `format_maps.properties`. Those entries in that file that start with the string `broad_format` will be used to define the map. The definition for that map:

```
broad_format.v = Video
broad_format.a = Book
broad_format.t = Book
broad_format.m = Computer File
broad_format.c = Musical Score
broad_format.d = Musical Score
broad_format.j = Musical Recording
broad_format.i = Non-musical Recording
broad_format = Unknown
```

Note that `Unknown` is the default value for the translation map.

Each line defining a translation starts with the name of the map, followed by a period, followed by the string that is to be replaced.  Next there must be an equals sign, followed by the string that should be used to create the replacement.  Note that it is possible to have several different strings be mapped to the same result (as shown for _Book_ or _Musical Score_), but it is not possible to have the same string to map to two different results.  If, for instance, in this specific example, which looks at position 6 from the MARC leader, and position 0 of field 007, if you decided that you wanted to include a mapping for the character _r_ in position 6 of the leader to _Three-dimensional artifact_ and also include a mapping for character _r_ of position 0 of field 007 to _Remote-sensing image_, you could not accomplish this using a field specification and a translation.  Instead you would have to create a custom indexing function.

Note also, that if no mapping is present for a given input, then no value will be entered for that particular index entry in the Solr index record.  This fact can be exploited in conjunction with the `first` field specification command as is shown in the following example:

> `music_catagory_facet = 999a[0-1]:999a[0], music_maps.properties(music_catagory), first`

```
music_catagory.ML = Music Literature
music_catagory.MT = Music Theory
music_catagory.M2 = Monuments of Music
music_catagory.M3 = Composers' Collected Works
music_catagory.M = Printed Music
```

In this example, the first two characters of the 999a subfield are extracted, if these two characters are _ML_, _MT_, _M2_ or _M3_, then the translation map will return the value corresponding to those values.  If the value doesn’t match one of those four strings, then the translation map will return null, and the next step in the specification will be processed; it will take only the first character of the ‘999a’ subfield, and pass that to the translation map, which then can check against the single letter _M_ using the fifth map entry.  If the value matches, then _Printed Music_ will be used for the Solr index field entry, otherwise no value will be used for the `music_catagory_facet` field of the Solr index record.

Lastly, note that the process of winnowing out duplicate entries takes place both before the translation map is applied, and again while collecting the results from applying the translation map. So if the following map were applied:

```
recording_format.MUSIC-CD = CD
recording_format.RSRV-CD = CD
recording_format.AUDIO-CD = CD
      
recording_format.AUDIO-CASS = Cassette
recording_format.MUSIC-CASS = Cassette
recording_format.RSRV-CASS = Cassette
recording_format.RSRV-AUD = Cassette
recording_format.RSRV-CAS2D = Cassette
      
recording_format.DVD = DVD 
recording_format.HS-VDVD = DVD 
recording_format.HS-VDVD3 = DVD 
recording_format.RSRV-VDVD = DVD 
      
recording_format.LP = LP
recording_format.IVY-LP = LP
recording_format.MUSIC-LP = LP
recording_format.OPENREEL = Open Reel Tape
      
recording_format.VIDEO-CASS = VHS
recording_format.RSRV-VCASS = VHS

recording_format.VIDEO-DISC = Video Disc
recording_format.RSRV-VDISC = Video Disc
```

and the set of strings gathered for the item consisted of  `{AUDIO-CASS, MUSIC-CASS`} the final returned result would be `{Cassette`}.

In the case where you want to define only a single translation map in a properties file, which might be the case for large translation maps, you can specify only the name of the properties file on the index field specification line as shown below:

> `instrument_facet = 048a[0-1], instrument_map.properties`

In this case all of the entries that occur in that file will be used to define the translation map, and there is no need to prefix the property keys with a common string, so that the instrument map would be defined as follows:

```
ba = Horn
bb = Trumpet
bc = Cornet
bd = Trombone
be = Tuba
bf = Baritone horn
bn = Brass, Unspecified
bu = Brass, Unknown
by = Brass, Ethnic
bz = Brass, Other
ca = Choruses, Mixed
cb = Chorus, Women's
cc = Choruses, Men's
cd = Choruses, Children's
cn = Choruses, Unspecified
cu = Chorus, Unknown
cy = Choruses, Ethnic
ea = Synthesizer
eb = Electronic Tape
ec = Computer
ed = Ondes Martinot
en = Electronic, Unspecified
eu = Electronic, Unknown
```

This makes the creation and maintaining of translation map properties files much easier to understand.

## Defining a Pattern-Based Translation Map ##

The previous section described how to define a translation map for a field.  However, one limitation of it is that it can only map from a fixed, pre-specified set of values.  If the value in the field doesn’t exactly match one of the translation keys, that value will not be mapped to any other value, and usually would then be discarded.

Sometimes you may want to look for a pattern of characters somewhere in the input field, and if that pattern occurs, then output some value to the index field.  To specify this in the field specification entry, specify the name of the translation map as described above:

> `ports_facet = 650c:650z:651a:651x:651z:655z, semester_at_sea.properties(port)`

Then define the translation map like this:

```
port.pattern_0 = Nassau.*Bahamas=>Nassau
port.pattern_1 = Salvador.*Brazil=>Salvador
port.pattern_2 = Walvis Bay.*Namibia=>Walvis Bay
port.pattern_3 = Cape Town.*South Africa=>Cape Town
port.pattern_4 = Chennai.*India=>Chennai
port.pattern_5 = Penang.*Malaysia=>Penang
port.pattern_6 = Ho Chi Minh City.*Vietnam=>Ho Chi Minh City
port.pattern_7 = Hong Kong=>Hong Kong
port.pattern_8 = Shanghai.*China=>Shanghai
port.pattern_9 = Kobe.*Japan=>Kobe
port.pattern_10 = Yokohama.*Japan=>Yokohama
port.pattern_11 = Puntarenas.*Costa Rica=>Puntarenas
port.pattern_12 = Bombay.*India=>Chennai
port.pattern_13 = Namibia=>Namibia
port.pattern_14 = South Africa=>South Africa
port.pattern_15 = India=>India
port.pattern_16 = Malaysia=>Malaysia
port.pattern_17 = Vietnam=>Vietnam
port.pattern_18 = China=>China
port.pattern_19 = Japan=>Japan
port.pattern_20 = Costa Rica=>Costa Rica
port.pattern_21 = Bahamas=>Bahamas
port.pattern_22 = Brazil=>Brazil
```

Then for every field that is extracted from a given MARC record will be matched against all of the patterns specified in the map.  Note that these entries must start with `(map_identifier).pattern_0` and proceed sequentially from there.  When using multiple translation maps, each map identifier ("port" in the example above) must be unique.  The value of the pattern is then split at the _=>_ with the portion before the arrow being used as a regular expression, and if that regular expression matches anywhere inside any of the fields extracted from the MARC record, the string that occurs after the arrow will be added to the index record.

In this example if a single field extracted from the MARC record contained _Chennai_, followed eventually by India, the value Chennai would be added to the index.  If that same field also contained _Penang_ followed by _Malaysia_, the value _Penang_ would be added to the index also.  Notice that for the last entries in the map above, the pattern that is looked-for is a simple string.  So based on `pattern_19` above if one of the fields extracted from the MARC record contains the word Japan, then the word Japan will be added to the index.

Another way of using the pattern-based translation map feature is to trim out a portion of the original string, using the regular expression grouping characters (   and   )  and the $1 syntax for the replacement string.  For example, suppose your records have several 035 fields, and that some of these field contain OCLC numbers, which are indicated in the field by having a prefix of (OCLC) before the number to use as shown in the following example record:

```
LEADER 00873pam a2200277 a 4500
001 u17922
008 831011s1984    njua          00110 eng
010   $a   83022049
020   $a0135959195 (pbk.)
035   $a(Sirsi) l83022049
035   $a(OCLC)10072685
039 0 $a2$b3$c3$d3$e3
040   $aDLC$cDLC$dVA@
049   $aVA@&
050 0 $aZ52.4$b.G34 1984
082 0 $a652$219
090   $aZ52.4$b.G34 1984$mVA@&$qGRAD BUS.
100 1 $aGalloway, Dianne.
245 10$aLearning to talk word processing /$cDianne Galloway.
260   $aEnglewood Cliffs, N.J. :$bPrentice-Hall,$cc1984.
300   $aviii, 119 p. :$bill. ;$c23 cm.
490 0 $aThe Modern office series
500   $aIncludes index.
596   $a13
650  0$aWord processing.
```

For this example if you wanted to select only the number portion of 035 lines that were OCLC numbers you could use the following index specification:

> `oclc_text = 035a, (pattern_map.oclc_num)`

and then use the following pattern-based translation map:

> `pattern_map.oclc_num.pattern_0 = \\(OCLC\\)(.*)=>$1`

which will discard the first 035 field from above, and then map the second field to the value _10072685_.

Similarly, if you want to trim off everything following the initial letters of an LC call number, you could use the following pattern map:

> `pattern_map.call_num.pattern_0 = ([A-Za-z]*).*=>$1`

# Custom Indexing Routines #

If special processing is required for a given Solr index field that cannot be handled by the any of the above indexing specification commands, you can reference a custom-created indexing function, by specifying `custom` as the second value in the field specification entry as shown in the example below:

> `recordings_and_scores_facet = custom, getRecordingAndScore`

In this case the value after _custom_ specifies the name of a java method to handle this particular bit of indexing.

## Pre-Defined Custom Indexing Methods ##

Because there are many "special processing" indexing tasks that commonly occur when indexing MARC records, there are several pre-defined "custom" indexing routines that can be referenced from your index specification file without requiring you to write any java code. These pre-defined indexing routines are listed in detail on the [CustomIndexingRoutines](CustomIndexingRoutines.md) page.

## Writing Your Own Custom Indexing Methods ##

If you are still unable to accomplish what you need to do using any of the provided indexing techniques (field specifications, patterns, pre-defined custom methods), you have the option of writing your own custom index methods in java (see [WritingCustomMethods](WritingCustomMethods.md)) or in BeanShell scripts (see [WritingCustomScripts](WritingCustomScripts.md)).