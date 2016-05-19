package org.solrmarc.index.vufind;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.solrmarc.index.SolrIndexer;

public class AuthorRelatorExtractor extends SolrIndexer
{
    /**
     * Check if a particular Datafield meets the specified relator requirements.
     * @param authorField      Field to analyze
     * @param noRelatorAllowed Array of tag names which are allowed to be used with
     * no declared relator.
     * @param relatorConfig    The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @return Boolean
     */
    protected Boolean authorHasAppropriateRelator(DataField authorField,
        String[] noRelatorAllowed, String relatorConfig
    ) {
        // get tag number from Field
        String tag = authorField.getTag();
        List<Subfield> subfieldE = normalizeRelatorSubfieldList(authorField.getSubfields('e'));
        List<Subfield> subfield4 = normalizeRelatorSubfieldList(authorField.getSubfields('4'));

        // if no relator is found, check to see if the current tag is in the "no
        // relator allowed" list.
        if (subfieldE.size() == 0 && subfield4.size() == 0) {
            return Arrays.asList(noRelatorAllowed).contains(tag);
        }

        // If we got this far, we need to figure out what type of relation they have
        List permittedRoles = normalizeRelatorStringList(Arrays.asList(loadRelatorConfig(relatorConfig)));
        for (int j = 0; j < subfield4.size(); j++) {
            if (permittedRoles.contains(subfield4.get(j).getData())) {
                return true;
            }
        }
        for (int j = 0; j < subfieldE.size(); j++) {
            if (permittedRoles.contains(subfieldE.get(j).getData())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse a SolrMarc fieldspec into a map of tag name to set of subfield strings
     * (note that we need to map to a set rather than a single string, because the
     * same tag may repeat with different subfields to extract different sections
     * of the same field into distinct values).
     *
     * @param tagList The field specification to parse
     * @return HashMap
     */
    protected HashMap<String, Set<String>> getParsedTagList(String tagList)
    {
        String[] tags = tagList.split(":");//convert string input to array
        HashMap<String, Set<String>> tagMap = new HashMap<String, Set<String>>();
        //cut tags array up into key/value pairs in hash map
        Set<String> currentSet;
        for(int i = 0; i < tags.length; i++){
            String tag = tags[i].substring(0, 3);
            if (!tagMap.containsKey(tag)) {
                currentSet = new LinkedHashSet<String>();
                tagMap.put(tag, currentSet);
            } else {
                currentSet = tagMap.get(tag);
            }
            currentSet.add(tags[i].substring(3));
        }
        return tagMap;
    }

    /**
     * Filter values retrieved using tagList to include only those whose relator
     * values are acceptable. Used for separating different types of authors.
     *
     * @param record               The record (fed in automatically)
     * @param tagList              The field specification to read
     * @param acceptWithoutRelator Colon-delimited list of tags whose values should
     * be accepted even if no relator subfield is defined
     * @param relatorConfig        The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @param firstOnly            Return first result only?
     * @return List result
     */
    public List<String> getAuthorsFilteredByRelator(Record record, String tagList,
        String acceptWithoutRelator, String relatorConfig, Boolean firstOnly) 
    {
        List<String> result = new LinkedList<String>();
        String[] noRelatorAllowed = acceptWithoutRelator.split(":");
        HashMap<String, Set<String>> parsedTagList = getParsedTagList(tagList);
        List fields = super.getFieldSetMatchingTagList(record, tagList);
        Iterator fieldsIter = fields.iterator();
        if (fields != null){
            DataField authorField;
            while (fieldsIter.hasNext()){
                authorField = (DataField) fieldsIter.next();
                //add all author types to the result set
                if (authorHasAppropriateRelator(authorField, noRelatorAllowed, relatorConfig)) {
                    for (String subfields : parsedTagList.get(authorField.getTag())) {
                        String current = this.getDataFromVariableField(authorField, "["+subfields+"]", " ", false);
                        // TODO: we may eventually be able to use this line instead,
                        // but right now it's not handling separation between the
                        // subfields correctly, so it's commented out until that is
                        // fixed.
                        //String current = authorField.getSubfieldsAsString(subfields);
                        if (null != current) {
                            result.add(current);
                            if (firstOnly) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Filter values retrieved using tagList to include only those whose relator
     * values are acceptable. Used for separating different types of authors.
     *
     * @param record               The record (fed in automatically)
     * @param tagList              The field specification to read
     * @param acceptWithoutRelator Colon-delimited list of tags whose values should
     * be accepted even if no relator subfield is defined
     * @param relatorConfig        The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @return List result
     */
    public List<String> getAuthorsFilteredByRelator(Record record, String tagList,
        String acceptWithoutRelator, String relatorConfig
    ) {
        // default firstOnly to false!
        return getAuthorsFilteredByRelator(
            record, tagList, acceptWithoutRelator, relatorConfig, false
        );
    }

    /**
     * Filter values retrieved using tagList to include only those whose relator
     * values are acceptable. Used for separating different types of authors.
     *
     * @param record               The record (fed in automatically)
     * @param tagList              The field specification to read
     * @param acceptWithoutRelator Colon-delimited list of tags whose values should
     * be accepted even if no relator subfield is defined
     * @param relatorConfig        The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @return String
     */
    public String getFirstAuthorFilteredByRelator(Record record, String tagList,
        String acceptWithoutRelator, String relatorConfig
    ) {
        List<String> result = getAuthorsFilteredByRelator(
            record, tagList, acceptWithoutRelator, relatorConfig, true
        );
        for (String s : result) {
            return s;
        }
        return null;
    }

    /**
     * Filter values retrieved using tagList to include only those whose relator
     * values are acceptable. Used for saving relators of authors separated by different
     * types.
     *
     * @param record               The record (fed in automatically)
     * @param tagList              The field specification to read
     * @param acceptWithoutRelator Colon-delimited list of tags whose values should
     * be accepted even if no relator subfield is defined
     * @param relatorConfig        The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @param firstOnly            Return first result only?
     * @return List result
     */
    public List getRelatorsFilteredByRelator(Record record, String tagList,
        String acceptWithoutRelator, String relatorConfig, Boolean firstOnly,
        String defaultRelator
    ) {
        List result = new LinkedList();
        String[] noRelatorAllowed = acceptWithoutRelator.split(":");
        HashMap<String, Set<String>> parsedTagList = getParsedTagList(tagList);
        List fields = this.getFieldSetMatchingTagList(record, tagList);
        Iterator fieldsIter = fields.iterator();
        if (fields != null){
            DataField authorField;
            while (fieldsIter.hasNext()){
                authorField = (DataField) fieldsIter.next();
                //add all author types to the result set
                if (authorHasAppropriateRelator(authorField, noRelatorAllowed, relatorConfig)) {
                    List<Subfield> subfieldE = normalizeRelatorSubfieldList(authorField.getSubfields('e'));
                    List<Subfield> subfield4 = normalizeRelatorSubfieldList(authorField.getSubfields('4'));

                    // get the first non-empty subfield
                    String relator = defaultRelator;

                    // try subfield E first
                    for (int j = 0; j < subfieldE.size(); j++) {
                        if (!subfieldE.get(j).getData().isEmpty()) {
                            relator = subfieldE.get(j).getData();
                            continue;
                        }
                    }
                    // try subfield 4 now and overwrite relator as subfield 4 is most important
                    for (int j = 0; j < subfield4.size(); j++) {
                        if (!subfield4.get(j).getData().isEmpty()) {
                            relator = subfield4.get(j).getData();
                            continue;
                        }
                    }

                    result.add(relator);
                }
            }
        }
        return result;
    }

    /**
     * Filter values retrieved using tagList to include only those whose relator
     * values are acceptable. Used for saving relators of authors separated by different
     * types.
     *
     * @param record               The record (fed in automatically)
     * @param tagList              The field specification to read
     * @param acceptWithoutRelator Colon-delimited list of tags whose values should
     * be accepted even if no relator subfield is defined
     * @param relatorConfig        The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @return List result
     */
    public List getRelatorsFilteredByRelator(Record record, String tagList,
        String acceptWithoutRelator, String relatorConfig
    ) {
        // default firstOnly to false!
        return getRelatorsFilteredByRelator(
            record, tagList, acceptWithoutRelator, relatorConfig, false, ""
        );
    }

    /**
     * This method fetches relator definitions from ini file and casts them to an
     * array. If a colon-delimited string is passed in, this will be directly parsed
     * instead of resorting to .ini loading.
     *
     * @param setting Setting to load from .ini or colon-delimited list.
     * @return String[]
     */
    protected String[] loadRelatorConfig(String setting){
        StringBuilder relators = new StringBuilder();

        // check for pipe-delimited string
        String[] relatorSettings = setting.split("\\|");
        for (String relatorSetting: relatorSettings) {
            // check for colon-delimited string
            String[] relatorArray = relatorSetting.split(":");
            if (relatorArray.length > 1) {
                for (int i = 0; i < relatorArray.length; i++) {
                    relators.append(relatorArray[i]).append(",");
                }
            } else {
                relators.append(this.getConfigSetting(
                    "author-classification.ini", "AuthorRoles", relatorSetting
                )).append(",");
            }
        }

        return relators.toString().split(",");
    }

    /**
     * Normalizes the strings in a list.
     *
     * @param stringList List of strings to be normalized
     * @return stringList Normalized List of strings 
     */
    protected List normalizeRelatorStringList(List<String> stringList)
    {
        for (int j = 0; j < stringList.size(); j++) {
            stringList.set(
                j,
                normalizeRelatorString(stringList.get(j))
            );
        }
        return stringList;
    }

    /**
     * Normalizes the strings in a list of subfields.
     *
     * @param subfieldList List of subfields to be normalized
     * @return subfieldList Normalized List of subfields
     */
    protected List<Subfield> normalizeRelatorSubfieldList(List<Subfield> subfieldList)
    {
        for (int j = 0; j < subfieldList.size(); j++) {
            subfieldList.get(j).setData(
                normalizeRelatorString(subfieldList.get(j).getData())
            );
        }
        return subfieldList;
    }

    /**
     * Normalizes a string
     *
     * @param string String to be normalized
     * @return string
     */
    protected String normalizeRelatorString(String string)
    {
        return string
            .trim()
            .toLowerCase()
            .replaceAll("\\p{Punct}+", "");    //POSIX character class Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
    }

    /**
     * Filter values retrieved using tagList to include only those whose relator
     * values are acceptable. Used for separating different types of authors.
     *
     * @param record               The record (fed in automatically)
     * @param tagList              The field specification to read
     * @param acceptWithoutRelator Colon-delimited list of tags whose values should
     * be accepted even if no relator subfield is defined
     * @param relatorConfig        The setting in author-classification.ini which
     * defines which relator terms are acceptable (or a colon-delimited list)
     * @param firstOnly            Return first result only?
     * @return List result
     */
    public List<String> getAuthorInitialsFilteredByRelator(Record record, String tagList,
        String acceptWithoutRelator, String relatorConfig
    ) {
        List<String> authors = getAuthorsFilteredByRelator(record, tagList, acceptWithoutRelator, relatorConfig);
        List<String> result = new LinkedList<String>();
        for (String author : authors) {
            result.add(this.processInitials(author));
        }
        return result;
    }

    /**
     * Takes a name and cuts it into initials
     * @param authorName e.g. Yeats, William Butler
     * @return initials e.g. w b y wb
     */
    protected String processInitials(String authorName) {
        Boolean isPersonalName = false;
        // we guess that if there is a comma before the end - this is a personal name
        if ((authorName.indexOf(',') > 0) 
            && (authorName.indexOf(',') < authorName.length()-1)) {
            isPersonalName = true;
        }
        // get rid of non-alphabet chars but keep hyphens and accents 
        authorName = authorName.replaceAll("[^\\p{L} -]", "").toLowerCase();
        String[] names = authorName.split(" "); //split into tokens on spaces
        // if this is a personal name we'll reorganise to put lastname at the end
        String result = "";
        if (isPersonalName) {
            String lastName = names[0]; 
            for (int i = 0; i < names.length-1; i++) {
                names[i] = names[i+1];
            }
            names[names.length-1] = lastName;
        }
        // put all the initials together in a space separated string
        for (String name : names) {
            if (name.length() > 0) {
                String initial = name.substring(0,1);
                // if there is a hyphenated name, use both initials
                if (name.indexOf('-') > 0) {
                    int pos = name.indexOf('-');
                    String extra = name.substring(pos+1,pos+2);
                    initial = initial + " " + extra;
                }
                result += " " + initial; 
            }
        }
        // grab all initials and stick them together
        String smushAll = result.replaceAll(" ", "");
        // if it's a long personal name, get all but the last initials as well
        // e.g. wb for william butler yeats
        if (names.length > 2 && isPersonalName) {
            String smushPers = result.substring(0,result.length()-1).replaceAll(" ","");
            result = result + " " + smushPers;
        }
        // now we have initials separate and together
        if (!result.trim().equals(smushAll)) {
            result += " " + smushAll; 
        }
        result = result.trim();
        return result;
    }

    /**
     * Normalize trailing punctuation. This mimics the functionality built into VuFind's
     * textFacet field type, so that you can get equivalent values when indexing into
     * a string field. (Useful for docValues support).
     *
     * Can return null
     *
     * @param record current MARC record
     * @param fieldSpec which MARC fields / subfields need to be analyzed
     * @return Set containing normalized values
     */
    public Set<String> normalizeTrailingPunctuation(Record record, String fieldSpec) {
        // Initialize our return value:
        Set<String> result = new LinkedHashSet<String>();

        // Loop through the specified MARC fields:
        Set<String> input = getFieldList(record, fieldSpec);
        Pattern pattern = Pattern.compile("(?<!\b[A-Z])[.\\s]*$");
        for (String current: input) {
            result.add(pattern.matcher(current).replaceAll(""));
        }

        // If we found no matches, return null; otherwise, return our results:
        return result.isEmpty() ? null : result;
    }
}
