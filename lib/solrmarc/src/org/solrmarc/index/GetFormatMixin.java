package org.solrmarc.index;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.ErrorHandler;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.tools.Utils;

public class GetFormatMixin extends SolrIndexerMixin
{
    
    private enum ContentType
    {
        Art,
        ArtReproduction,
        Atlas,
        Book,
        BookCollection,
        BookComponentPart,
        BookSeries,
        BookSubunit,
        Chart,
        ComputerBibliographicData,
        ComputerCombination,
        ComputerDocument,
        ComputerFile,
        ComputerFont,
        ComputerGame,
        ComputerInteractiveMultimedia,
        ComputerNumericData,
        ComputerOnlineSystem,
        ComputerProgram,
        ComputerRepresentational,
        ComputerSound,
        Database,
        Diorama,
        Filmstrip,
        FlashCard,
        Game,
        Globe,
        Graphic,
        Image,
        Kit,
        LooseLeaf,
        Manuscript,
        Map,
        MapBound,
        MapManuscript,
        MapSeparate,
        MapSerial,
        MapSeries,
        MapSingle,
        MicroscopeSlide,
        MixedMaterial,
        Model,
        MotionPicture,
        MusicalScore,
        MusicalScoreManuscript,
        MusicRecording,
        Newspaper,
        Pamphlet,
        Periodical,
        PhysicalObject,
        Picture,
        ProjectedMedium,
        Realia,
        Serial,
        SerialComponentPart,
        SerialIntegratingResource,
        Slide,
        SoundRecording,
        SpecialInstructionalMaterial,
        TechnicalDrawing,
        Thesis,
        Toy,
        Transparency,
        Video,
        Website
    }

    private enum MediaType
    {
        ActivityCard,
        Atlas,
        Braille,
        Chart,
        Collage,
        ComputerCard,
        ComputerChipCartridge,
        ComputerDiscCartridge,
        ComputerDisk,
        ComputerFloppyDisk,
        ComputerMagnetoOpticalDisc,
        ComputerOpticalDisc,
        ComputerOpticalDiscCartridge,
        ComputerOther,
        ComputerTapeCartridge,
        ComputerTapeCassette,
        ComputerTapeReel,
        Drawing,
        Electronic,
        ElectronicDirect,
        FilmCassette,
        FilmOther,
        Filmslip,
        Filmstrip,
        FilmstripCartridge,
        FilmstripRoll,
        FlashCard,
        Globe,
        GlobeCelestial,
        GlobeEarthMoon,
        GlobeOther,
        GlobePlanetary,
        GlobeTerrestrial,
        Icon,
        ImageOther,
        ImagePrint,
        Kit,
        LooseLeaf,
        Map,
        MapDiagram,
        MapModel,
        MapOther,
        MapProfile,
        MapSection,
        MapView,
        Microfiche,
        MicroficheCassette,
        Microfilm,
        MicrofilmCartridge,
        MicrofilmCassette,
        MicrofilmReel,
        MicrofilmRoll,
        MicrofilmSlip,
        Microform,
        MicroformApetureCard,
        Microopaque,
        MusicalScore,
        Online,
        Painting,
        Photo,
        PhotomechanicalPrint,
        Photonegative,
        PhotoPrint,
        Picture,
        Postcard,
        Poster,
        Print,
        PrintLarge,
        ProjectedMediumOther,
        Radiograph,
        SensorImage,
        Slide,
        Software,
        SoundCartridge,
        SoundCassette,
        SoundCylinder,
        SoundDisc,
        SoundDiscCD,
        SoundDiscLP,
        SoundRecordingOther,
        SoundRoll,
        SoundTapeReel,
        SoundTrackFilm,
        SoundWireRecording,
        StudyPrint,
        TactileCombination,
        TactileMoon,
        TactileNoWritingSystem,
        TactileOther,
        TextOther,
        Transparency,
        Video8mm,
        VideoBeta,
        VideoBetacam,
        VideoBetacamSP,
        VideoBluRay,
        VideoCartridge,
        VideoCassette,
        VideoCapacitance,
        VideoD2,
        VideoDisc,
        VideoDVD,
        VideoEIAJ,
        VideoHi8,
        VideoLaserdisc,
        VideoMII,
        VideoOther,
        VideoQuadruplex,
        VideoReel,
        VideoSuperVHS,
        VideoTypeC,
        VideoUMatic,
        VideoVHS, 
    }

    private enum CombinedType
    {
        EBook,
        EJournal
    }

    private enum ControlType
    {
        Archive
    }
    
    /**
     * Return the content type and media types, plus electronic, for this record
     * 
     * @param Record   -  MARC Record
     * @return Set of Strings of content types and media types
     */
    public Set<String> getContentTypesAndMediaTypes(final Record record)
    {
        Set<String> formats = getContentTypes(record);
        formats.addAll( getMediaTypes(record));
        formats = addOnlineTypes(record, formats); 
        return(formats);
    }
    
    /**
     * Return the primary content type, plus electronic, for this record
     * 
     * @param Record  -  MARC Record
     * @return String of primary material types
     */

    public Set<String> getPrimaryContentTypePlusOnline(final Record record)
    {
        Set<String> format = new LinkedHashSet<String>();

        // get primary material type

        String primaryType = getPrimaryContentType(record);
        format.add(primaryType);
        
        format = addOnlineTypes(record, format);
        
        return format;
    }

    /**
     * Add types EBook and Online for electronic items for this record
     * 
     * @param Record  -  MARC Record
     * @param Set<String>  - the Set of formats to add the types EBook and Online to 
     * @return String of primary material types
     */

    public Set<String> addOnlineTypes(final Record record, Set<String> formats)
    {
        // see if we have full-text link

        Boolean online = hasFullText(record);

        // if so, and this is a book, add e-book as well

        if (formats.contains("Book") && online == true)
        {
            formats = addToTop(formats, CombinedType.EBook.toString());
        }

        if (online == true)
        {
            formats.add(MediaType.Online.toString());
        }
        return(formats);
    }
    
    /**
     * Return the primary content type for this record
     * 
     * @param Record
     *            MARC Record
     * @return String of primary material types
     */

    public String getPrimaryContentType(final Record record)
    {
        String primaryFormat = "";

        Set<String> materialType = getContentTypes(record);

        for (String result : materialType)
        {
            primaryFormat = result;
            break;
        }

        return primaryFormat;
    }

    /**
     * Parse out content types from record
     * 
     * @param Record
     *            MARC Record
     * @return List of material types
     */

    public boolean isArchive(final Record record)
    {
        // special case for archive

        if (record.getLeader().toString().toLowerCase().charAt(8) == 'a')
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Parse out content types from record
     * 
     * @param Record
     *            MARC Record
     * @return List of material types
     */

    public Set<String> getContentTypes(final Record record)
    {
        Set<String> materialType = new LinkedHashSet<String>(); // the list of material types

        // // Leader ////

        String leader = record.getLeader().toString();

        // get main type and profile from leader/06

        String leaderType = extractType(leader, "leader"); // main material type, based on leader
        String leaderProfile = extractProfile(leader, "leader"); // 008 profile to use

        // // 008 & 006 ////

        // take both the 008 and 006 (which use the same structure, just at
        // different positions)
        // so we can iterate over them both

        String[] formatTags = { "008", "006" };
        List<ControlField> fieldsFormat = (List<ControlField>)record.getVariableFields(formatTags);

        for (ControlField fieldFormat : fieldsFormat)
        {
            String tag = fieldFormat.getTag();

            String profile = ""; // we'll use this to determine which
                                    // positions to use
            String type = ""; // we'll use this to set a default type

            if (tag.equals("008"))
            {
                // 008 uses profile and type set in leader

                profile = leaderProfile;
                type = leaderType;
            }
            else if (tag.equals("006"))
            {
                // 006 uses position 0 to indicate profile and type

                profile = extractProfile(fieldFormat.getData(), "006");
                type = extractType(fieldFormat.getData(), "006");
            }

            // BOOKS

            // is used when Leader/06 (Type of record) contains code a (Language
            // material)
            // or t (Manuscript language material) and Leader/07 (Bibliographic
            // level)
            // contains code a (Monographic component part), c (Collection), d
            // (Subunit),
            // or m (Monograph)

            if (profile.equals("books"))
            {
                materialType.add(type);
            }

            // COMPUTER FILES

            // is used when Leader/06 (Type of record) contains code m.

            else if (profile.equals("computers"))
            {
                // type of computer file

                int position = 26;

                if (tag.equals("006"))
                {
                    position = position - 7;
                }

                String field = fieldFormat.getData();

                // wasn't long enough, yo!

                if (field.length() - 1 < position)
                {
                    materialType.add(type);
                    if (indexer != null && indexer.errors != null)
                    {
                        indexer.errors.addError(record.getControlNumber(), tag, "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+tag+" is shorter than it ought to be");
                    }
                    continue;
                }

                switch (field.toLowerCase().charAt(position)) {
                    case 'a': // a - Numeric data

                        materialType.add(ContentType.ComputerNumericData.toString());
                        break;

                    case 'b': // b - Computer program

                        materialType.add(ContentType.ComputerProgram.toString());
                        break;

                    case 'c': // c - Representational

                        materialType.add(ContentType.ComputerRepresentational.toString());
                        break;

                    case 'd': // d - Document

                        materialType.add(ContentType.ComputerDocument.toString());
                        break;

                    case 'e': // e - Bibliographic data

                        materialType.add(ContentType.ComputerBibliographicData.toString());
                        break;

                    case 'f': // f - Font

                        materialType.add(ContentType.ComputerFont.toString());
                        break;

                    case 'g': // g - Game

                        materialType.add(ContentType.ComputerGame.toString());
                        break;

                    case 'h': // h - Sound

                        materialType.add(ContentType.ComputerSound.toString());
                        break;

                    case 'i': // i - Interactive multimedia

                        materialType.add(ContentType.ComputerInteractiveMultimedia.toString());
                        break;

                    case 'j': // j - Online system or service

                        materialType.add(ContentType.ComputerOnlineSystem.toString());
                        break;

                    case 'm': // m - Combination

                        materialType.add(ContentType.ComputerCombination.toString());
                        break;

                    // u - Unknown
                    // z - Other

                    default:

                        materialType.add(type);
                        break;
                }
            }

            // MAPS

            // is used when Leader/06 (Type of record) contains code e
            // (Cartographic material)
            // or f (Manuscript cartographic material).

            else if (profile.equals("maps"))
            {
                // type of cartographic material

                int position = 25;

                if (tag.equals("006"))
                {
                    position = position - 7;
                }

                String field = fieldFormat.getData();

                // wasn't long enough, yo!

                if (field.length() - 1 < position)
                {
                    materialType.add(type);
                    if (indexer != null && indexer.errors != null)
                    {
                        indexer.errors.addError(record.getControlNumber(), tag, "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+tag+" is shorter than it ought to be");
                    }
                    continue;
                }

                switch (field.toLowerCase().charAt(position)) {
                    case 'a': // a - Single map

                        materialType.add(ContentType.MapSingle.toString());
                        break;

                    case 'b': // b - Map series

                        materialType.add(ContentType.MapSeries.toString());
                        break;

                    case 'c': // c - Map serial

                        materialType.add(ContentType.MapSerial.toString());
                        break;

                    case 'd': // d - Globe

                        materialType.add(ContentType.Globe.toString());
                        break;

                    case 'e': // e - Atlas

                        materialType.add(ContentType.Atlas.toString());
                        break;

                    case 'f': // f - Separate supplement to another work

                        materialType.add(ContentType.MapSeparate.toString());
                        break;

                    case 'g': // g - Bound as part of another work

                        materialType.add(ContentType.MapBound.toString());
                        break;

                    // u - Unknown
                    // z - Other

                    default:
                        materialType.add(type);
                        break;
                }
            }

            // MUSIC

            // is used when Leader/06 (Type of record) contains code c (Notated
            // music),
            // d (Manuscript notated music), i (Nonmusical sound recording), or
            // j (Musical sound recording)

            else if (profile.equals("music"))
            {
                materialType.add(type);
            }

            // CONTINUING RESOURCES

            // is used when Leader/06 (Type of record) contains code a (Language
            // material) and Leader/07
            // contains code b (Serial component part), i (Integrating
            // resource), or code s (Serial).

            else if (profile.equals("serial"))
            {
                // type of continuing resource

                int position = 21;

                if (tag.equals("006"))
                {
                    position = position - 7;
                }

                String field = fieldFormat.getData();

                // wasn't long enough, yo!

                if (field.length() - 1 < position)
                {
                    materialType.add(type);
                    if (indexer != null && indexer.errors != null)
                    {
                        indexer.errors.addError(record.getControlNumber(), tag, "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+tag+" is shorter than it ought to be");
                    }
                    continue;
                }

                switch (field.toLowerCase().charAt(position)) {
                    case 'd': // updating database

                        materialType.add(ContentType.Database.toString());
                        break;

                    case 'l': // l - Updating loose-leaf

                        materialType.add(ContentType.LooseLeaf.toString());
                        break;
                        
                    case 'm': // Monographic series

                        materialType.add(ContentType.BookSeries.toString());
                        break;

                    case 'n': // Newspaper

                        materialType.add(ContentType.Newspaper.toString());
                        break;

                    case 'p': // Periodical

                        materialType.add(ContentType.Periodical.toString());
                        break;

                    case 'w': // Updating Web site

                        materialType.add(ContentType.Website.toString());
                        break;

                    default:
                        materialType.add(type);
                        break;
                }
            }

            // VISUAL MATERIALS

            // is used when Leader/06 (Type of record) contains code g
            // (Projected medium),
            // code k (Two-dimensional nonprojectable graphic), code o (Kit), or
            // code r (Three-dimensional artifact or naturally occurring
            // object).

            else if (profile.equals("visual"))
            {
                // type of visual material

                int position = 33;

                if (tag.equals("006"))
                {
                    position = position - 7;
                }

                String field = fieldFormat.getData();

                // wasn't long enough, yo!

                if (field.length() - 1 < position)
                {
                    materialType.add(type);
                    if (indexer != null && indexer.errors != null)
                    {
                        indexer.errors.addError(record.getControlNumber(), tag, "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+tag+" is shorter than it ought to be");
                    }
                    continue;
                }

                switch (field.toLowerCase().charAt(position)) {
                    case 'a': // a - Art original

                        materialType.add(ContentType.Art.toString());
                        break;
                        
                    case 'b': // b - Kit

                        materialType.add(ContentType.Kit.toString());
                        break;

                    case 'c': // c - Art reproduction

                        materialType.add(ContentType.ArtReproduction.toString());
                        break;

                    case 'd': // d - Diorama

                        materialType.add(ContentType.Diorama.toString());
                        break;
                        
                    case 'f': // f - Filmstrip

                        materialType.add(ContentType.Filmstrip.toString());
                        break;

                    case 'g': // g - Game

                        materialType.add(ContentType.Game.toString());
                        break;
                        
                    case 'i': // i - Picture

                        materialType.add(ContentType.Picture.toString());
                        break;

                    case 'k': // k - Graphic

                        materialType.add(ContentType.Graphic.toString());
                        break;

                    case 'l': // l - Technical drawing

                        materialType.add(ContentType.TechnicalDrawing.toString());
                        break;

                    case 'm': // m - Motion picture

                        materialType.add(ContentType.MotionPicture.toString());
                        break;

                    case 'n': // n - Chart

                        materialType.add(ContentType.Chart.toString());
                        break;

                    case 'o': // o - Flash card

                        materialType.add(ContentType.FlashCard.toString());
                        break;

                    case 'p': // p - Microscope slide

                        materialType.add(ContentType.MicroscopeSlide.toString());
                        break;

                    case 'q': // q - Model

                        materialType.add(ContentType.Model.toString());
                        break;

                    case 'r': // r - Realia

                        materialType.add(ContentType.Realia.toString());
                        break;

                    case 's': // s - Slide

                        materialType.add(ContentType.Slide.toString());
                        break;

                    case 't': // t - Transparency

                        materialType.add(ContentType.Transparency.toString());
                        break;

                    case 'v': // v - Videorecording

                        materialType.add(ContentType.Video.toString());
                        break;

                    case 'w': // w - Toy

                        materialType.add(ContentType.Toy.toString());
                        break;

                    // z - Other

                    default:
                        materialType.add(type);
                        break;
                }
            }

            // MIXED MATERIALS

            // is used when Leader/06 (Type of record) contains code p (Mixed
            // material).

            else if (profile.equals("mixed"))
            {
                materialType.add(type);
            }
            else
            {
                if (indexer != null && indexer.errors != null)
                {
                    String field = tag.equals("006") ? "006/00" : "LEADER/06";
                    indexer.errors.addError(record.getControlNumber(), field, "n/a", ErrorHandler.MINOR_ERROR, "Unknown item profile specified in "+ field);
                }
                //throw new SolrMarcIndexerException(1, "bad profile: " + profile);
            }
        }

        // / DATA FIELDS ///

        // thesis

        if (!record.getVariableFields("502").isEmpty())
        {
            // set the first (primary) type as thesis

            materialType = addToTop(materialType, ContentType.Thesis.toString());

            // nix manuscript so we can distinguish actual manuscripts

            materialType.remove(ContentType.Manuscript.toString());
        }

        // nothing worked?

        if (materialType.isEmpty())
        {
            // record must have very little data, so we'll take whatever we can
            // get

            // isbn

            if (!record.getVariableFields("020").isEmpty())
            {
                materialType.add(ContentType.Book.toString());
            }

            // only type from leader was available

            else if (leaderType != "")
            {
                materialType.add(leaderType);
            }
        }

        return materialType;
    }

    /**
     * Parse out media / carrier types from record
     * 
     * @param Record
     *            MARC Record
     * @return List of material types
     */

    public Set<String> getMediaTypes(final Record record)
    {
        Set<String> form = new LinkedHashSet<String>(); // the list of form
                                                        // types

        // // Data Fields ////
        // electronic resource from title

        DataField title = (DataField) record.getVariableField("245");

        if (title != null && title.getSubfield('h') != null)
        {
            // general material designator in title 245|h
            if (title.getSubfield('h').getData().toLowerCase().contains("[electronic resource]"))
            {
                form.add(MediaType.Electronic.toString());
            }
        }

        // // 007 ////

        List<ControlField> fields007 = record.getVariableFields("007");

        for (ControlField field007 : fields007)
        {
            // first, check to make sure this is a post-1981 007 by looking at
            // position 2, which should be undefined

            char field007_02 = '?';
            if (field007.getData().length() <= 2 || 
                (field007_02 = field007.getData().toLowerCase().charAt(2)) != ' ' && field007_02 != '|' && field007_02 != '-')
            { 
                {
                    if (indexer != null && indexer.errors != null)
                    {
                        indexer.errors.addError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "Malformed 007 fixed field");
                    }
                    continue;
                }
            }

            char materialGeneral = field007.getData().toLowerCase().charAt(0);
            char materialSpecific = field007.getData().toLowerCase().charAt(1);

            switch (materialGeneral) {
                case 'a': // maps

                    switch (materialSpecific) {
                        // a (Aerial chart) [OBSOLETE, 1997] [CAN/MARC only]
                        // b (Aerial remote-sensing image) [OBSOLETE, 1997]
                        // [CAN/MARC only]
                        // c (Anamorphic map) [OBSOLETE, 1997] [CAN/MARC only]

                        case 'd': // d - Atlas

                            form.add(MediaType.Atlas.toString());
                            break;

                        // e (Celestial chart) [OBSOLETE, 1997] [CAN/MARC only]
                        // f (Chart) [OBSOLETE, 1997] [CAN/MARC only]

                        case 'g': // g - Diagram

                            form.add(MediaType.MapDiagram.toString());
                            break;

                        // h (Hydrographic chart) [OBSOLETE, 1997] [CAN/MARC
                        // only]
                        // i (Imaginative map) [OBSOLETE, 1997] [CAN/MARC only]
                        // j (Orthophoto) [OBSOLETE, 1997] [CAN/MARC only]

                        case 'j': // j - Map

                            form.add(MediaType.Map.toString());
                            break;

                        case 'k': // k - Profile

                            form.add(MediaType.MapProfile.toString());
                            break;

                        // m (Photo mosaic (controlled)) [OBSOLETE, 1997]
                        // [CAN/MARC only]
                        // n (Photo mosaic (uncontrolled)) [OBSOLETE, 1997]
                        // [CAN/MARC only]
                        // o (Photomap) [OBSOLETE, 1997] [CAN/MARC only]
                        // p (Plan) [OBSOLETE, 1997] [CAN/MARC only]

                        case 'q': // q - Model

                            form.add(MediaType.MapModel.toString());
                            break;

                        case 'r': // r - Remote-sensing image

                            form.add(MediaType.SensorImage.toString());
                            break;

                        case 's': // s - Section

                            form.add(MediaType.MapSection.toString());
                            break;

                        // t (Space remote-sensing image) [OBSOLETE, 1997]
                        // [CAN/MARC only]
                        // u - Unspecified
                        // v (Terrestrial remote-sensing image) [OBSOLETE, 1997]
                        // [CAN/MARC only]
                        // w (Topographical drawing) [OBSOLETE, 1997] [CAN/MARC
                        // only]
                        // x (Topographical print) [OBSOLETE, 1997] [CAN/MARC
                        // only]

                        case 'y': // y - View

                            form.add(MediaType.MapView.toString());
                            break;

                        // z - Other

                        default:
                            form.add(MediaType.MapOther.toString());
                            break;
                    }
                    break;

                case 'c': // electronic resource

                    switch (materialSpecific) {
                        case 'a': // a - Tape cartridge

                            form.add(MediaType.ComputerTapeCartridge.toString());
                            break;

                        case 'b': // b - Chip cartridge

                            form.add(MediaType.ComputerChipCartridge.toString());
                            break;

                        case 'c': // c - Computer optical disc cartridge

                            form.add(MediaType.ComputerOpticalDiscCartridge.toString());
                            break;

                        case 'd': // d - Computer disc, type unspecified

                            form.add(MediaType.ComputerDisk.toString());
                            break;

                        case 'e': // e - Computer disc cartridge, type
                                    // unspecified

                            form.add(MediaType.ComputerDiscCartridge.toString());
                            break;

                        case 'f': // f - Tape cassette

                            form.add(MediaType.ComputerTapeCassette.toString());
                            break;

                        case 'h': // h - Tape reel

                            form.add(MediaType.ComputerTapeReel.toString());
                            break;

                        case 'j': // j - Magnetic disk

                            form.add(MediaType.ComputerFloppyDisk.toString());
                            break;

                        case 'k': // k - Computer card

                            form.add(MediaType.ComputerCard.toString());
                            break;

                        case 'm': // m - Magneto-optical disc

                            form.add(MediaType.ComputerMagnetoOpticalDisc.toString());
                            break;

                        case 'o': // o - Optical disc

                            form.add(MediaType.ComputerOpticalDisc.toString());
                            break;

                        case 'r': // r - Remote

                            form.add(MediaType.Online.toString());
                            break;

                        // u - Unspecified
                        // z - Other

                        default:

                            form.add(MediaType.ComputerOther.toString());
                            break;
                    }
                    break;

                case 'd': // globe

                    switch (materialSpecific) {
                        case 'a': // a - Celestial globe

                            form.add(MediaType.GlobeCelestial.toString());
                            break;

                        case 'b': // b - Planetary or lunar globe

                            form.add(MediaType.GlobePlanetary.toString());
                            break;

                        case 'c': // c - Terrestrial globe

                            form.add(MediaType.GlobeTerrestrial.toString());
                            break;

                        // d - Satellite globe (of our solar system), excluding
                        // the earth moon [OBSOLETE, 1997] [CAN/MARC only]

                        case 'e': // e - Earth moon globe

                            form.add(MediaType.GlobeEarthMoon.toString());
                            break;

                        // u - Unspecified
                        // z - Other

                        default:

                            form.add(MediaType.GlobeOther.toString());
                            break;
                    }
                    break;

                case 'f': // tactile material

                    switch (materialSpecific) {
                        case 'a': // a - Moon

                            form.add(MediaType.TactileMoon.toString());
                            break;

                        // b - Braille

                        case 'b': // d - Tactile, with no writing system

                            form.add(MediaType.Braille.toString());
                            break;

                        case 'c': // c - Combination

                            form.add(MediaType.TactileCombination.toString());
                            break;

                        case 'd': // d - Tactile, with no writing system

                            form.add(MediaType.TactileNoWritingSystem.toString());
                            break;

                        // u - Unspecified
                        // z - Other

                        default:
                            form.add(MediaType.TactileOther.toString());
                            break;
                    }
                    break;

                case 'g': // projected graphic

                    switch (materialSpecific) {
                        case 'c': // c - Filmstrip cartridge

                            form.add(MediaType.FilmstripCartridge.toString());
                            break;

                        case 'd': // d - Filmslip

                            form.add(MediaType.Filmslip.toString());
                            break;

                        case 'f': // f - Filmstrip, type unspecified

                            form.add(MediaType.Filmstrip.toString());
                            break;

                        // n - Not applicable [OBSOLETE, 1981] [USMARC only]

                        case 'o': // o - Filmstrip roll

                            form.add(MediaType.FilmstripRoll.toString());
                            break;

                        case 's': // s - Slide

                            form.add(MediaType.Slide.toString());
                            break;

                        case 't': // t - Transparency

                            form.add(MediaType.Transparency.toString());
                            break;

                        // # - Not applicable or no attempt to code [OBSOLETE,
                        // 1980]
                        // u - Unspecified
                        // z - Other

                        default:

                            form.add(MediaType.ProjectedMediumOther.toString());
                            break;
                    }
                    break;

                case 'h': // microform

                    switch (materialSpecific) {
                        case 'a': // a - Aperture card

                            form.add(MediaType.MicroformApetureCard.toString());
                            break;

                        case 'b': // b - Microfilm cartridge

                            form.add(MediaType.MicrofilmCartridge.toString());
                            break;

                        case 'c': // c - Microfilm cassette

                            form.add(MediaType.MicrofilmCassette.toString());
                            break;

                        case 'd': // d - Microfilm reel

                            form.add(MediaType.MicrofilmReel.toString());
                            break;

                        case 'e': // e - Microfiche

                            form.add(MediaType.Microfiche.toString());
                            break;

                        case 'f': // f - Microfiche cassette

                            form.add(MediaType.MicroficheCassette.toString());
                            break;

                        case 'g': // g - Microopaque

                            form.add(MediaType.Microopaque.toString());
                            break;

                        case 'h': // h - Microfilm slip

                            form.add(MediaType.MicrofilmSlip.toString());
                            break;

                        case 'j': // j - Microfilm roll

                            form.add(MediaType.MicrofilmRoll.toString());
                            break;

                        // u - Unspecified
                        // z - Other

                        default:

                            form.add(MediaType.Microform.toString());
                            break;
                    }
                    break;

                case 'k': // non-projected graphic

                    switch (materialSpecific) {
                        case 'a': // a - Activity card

                            form.add(MediaType.ActivityCard.toString());
                            break;

                        case 'c': // c - Collage

                            form.add(MediaType.Collage.toString());
                            break;

                        case 'd': // d - Drawing

                            form.add(MediaType.Drawing.toString());
                            break;

                        case 'e': // e - Painting

                            form.add(MediaType.Painting.toString());
                            break;

                        case 'f': // f - Photomechanical print

                            form.add(MediaType.PhotomechanicalPrint.toString());
                            break;

                        case 'g': // g - Photonegative

                            form.add(MediaType.Photonegative.toString());
                            break;

                        case 'h': // h - Photoprint

                            form.add(MediaType.PhotoPrint.toString());
                            break;

                        case 'i': // i - Picture

                            form.add(MediaType.Picture.toString());
                            break;

                        case 'j': // j - Print

                            form.add(MediaType.ImagePrint.toString());
                            break;

                        case 'k': // k - Poster

                            form.add(MediaType.Poster.toString());
                            break;

                        case 'l': // l - Technical drawing

                            form.add(MediaType.Drawing.toString());
                            break;

                        case 'n': // n - Chart

                            form.add(MediaType.Chart.toString());
                            break;

                        case 'o': // o - Flash card

                            form.add(MediaType.FlashCard.toString());
                            break;

                        case 'p': // p - Postcard

                            form.add(MediaType.Postcard.toString());
                            break;

                        case 'q': // q - Icon

                            form.add(MediaType.Icon.toString());
                            break;

                        case 'r': // r - Radiograph

                            form.add(MediaType.Radiograph.toString());
                            break;

                        case 's': // s - Study print

                            form.add(MediaType.StudyPrint.toString());
                            break;

                        // u - Unspecified

                        case 'v': // v - Photograph, type unspecified

                            form.add(MediaType.Photo.toString());
                            break;

                        // z - Other

                        default:

                            form.add(MediaType.ImageOther.toString());
                            break;
                    }
                    break;

                case 'm': // motion picture

                    switch (materialSpecific) {
                        case 'c': // c - Film cartridge

                            form.add(MediaType.FilmstripCartridge.toString());
                            break;

                        case 'f': // f - Film cassette

                            form.add(MediaType.FilmCassette.toString());
                            break;

                        case 'o': // o - Film roll

                            form.add(MediaType.FilmstripRoll.toString());
                            break;

                        case 'r': // r - Film reel

                            form.add(MediaType.Filmstrip.toString());
                            break;

                        // u - Unspecified
                        // z - Other

                        default:
                            form.add(MediaType.FilmOther.toString());
                            break;
                    }
                    break;

                case 'o': // kit

                    form.add(MediaType.Kit.toString());
                    break;

                case 'q': // notated music

                    form.add(MediaType.MusicalScore.toString());
                    break;

                case 'r': // remote-sensing image

                    form.add(MediaType.SensorImage.toString());
                    break;

                case 's': // sound recording

                    switch (materialSpecific) {
                        case 'd': // d - Sound disc

                            form.add(MediaType.SoundDisc.toString());
                            
                            //  check subtype of sound disc f in 007/03 means CD  one of abde means LP
                            char subSpecific = (field007.getData().length() > 3) ? field007.getData().toLowerCase().charAt(3) : ' ';
                            if (subSpecific == 'f')
                                form.add(MediaType.SoundDiscCD.toString());
                            else if ("abde".indexOf(subSpecific) != -1)
                                form.add(MediaType.SoundDiscLP.toString());
                            
                            break;

                        case 'c': // c - Cylinder [OBSOLETE]
                        case 'e': // e - Cylinder

                            form.add(MediaType.SoundCylinder.toString());
                            break;

                        case 'g': // g - Sound cartridge

                            form.add(MediaType.SoundCartridge.toString());
                            break;

                        case 'f': // f - Sound-track film [OBSOLETE]
                        case 'i': // i - Sound-track film

                            form.add(MediaType.SoundTrackFilm.toString());
                            break;

                        case 'r': // r - Roll [OBSOLETE]
                        case 'q': // q - Roll

                            form.add(MediaType.SoundRoll.toString());
                            break;

                        case 's': // s - Sound cassette

                            form.add(MediaType.SoundCassette.toString());
                            break;

                        case 't': // t - Sound-tape reel

                            form.add(MediaType.SoundTapeReel.toString());
                            break;

                        // u - Unspecified

                        case 'w': // w - Wire recording

                            form.add(MediaType.SoundWireRecording.toString());
                            break;

                        // z - Other

                        default:
                            form.add(MediaType.SoundRecordingOther.toString());
                            break;
                    }
                    break;

                case 't': // text

                    switch (materialSpecific) {
                        case 'a': // a - Regular print

                            form.add(MediaType.Print.toString());
                            break;

                        case 'b': // b - Large print

                            form.add(MediaType.PrintLarge.toString());
                            break;

                        case 'c': // c - Braille

                            form.add(MediaType.Braille.toString());
                            break;

                        case 'd': // d - Loose-leaf

                            form.add(MediaType.LooseLeaf.toString());
                            break;

                        // u - Unspecified
                        // z - Other

                        default:
                            form.add(MediaType.TextOther.toString());
                            break;
                    }

                    break;

                case 'v': // video recording

                    if (field007.getData().length() >= 5)
                    {
                        // 04 - Videorecording format

                        char videoFormat = field007.getData().toLowerCase().charAt(4);
                        String formToAdd = null;
                        String id = record.getControlNumber();
                        switch (videoFormat) {
                            case 'a': // a - Beta (1/2 in., videocassette)

                                formToAdd = MediaType.VideoBeta.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'b': // b - VHS (1/2 in., videocassette)

                                formToAdd = MediaType.VideoVHS.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'c': // c - U-matic (3/4 in., videocasstte)

                                formToAdd = MediaType.VideoUMatic.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'd': // d - EIAJ (1/2 in., reel)

                                formToAdd = MediaType.VideoEIAJ.toString();
                                checkTypeOfVideo(id, materialSpecific, 'r', formToAdd);
                                break;

                            case 'e': // e - Type C (1 in., reel)

                                formToAdd = MediaType.VideoTypeC.toString();
                                checkTypeOfVideo(id, materialSpecific, 'r', formToAdd);
                                break;

                            case 'f': // f - Quadruplex (1 in. or 2 in., reel)

                                formToAdd = MediaType.VideoQuadruplex.toString();
                                checkTypeOfVideo(id, materialSpecific, 'r', formToAdd);
                                break;

                            case 'g': // g - Laserdisc

                                formToAdd = MediaType.VideoLaserdisc.toString();
                                checkTypeOfVideo(id, materialSpecific, 'd', formToAdd);
                                break;

                            case 'h': // h - CED (Capacitance Electronic Disc)
                                        // videodisc

                                formToAdd = MediaType.VideoCapacitance.toString();
                                checkTypeOfVideo(id, materialSpecific, 'd', formToAdd);
                                break;

                            case 'i': // i - Betacam (1/2 in., videocassette)

                                formToAdd = MediaType.VideoBetacam.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'j': // j - Betacam SP (1/2 in.,
                                        // videocassette)

                                formToAdd = MediaType.VideoBetacamSP.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'k': // k - Super-VHS (1/2 in.,
                                        // videocassette)

                                formToAdd = MediaType.VideoSuperVHS.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'm': // m - M-II (1/2 in., videocassette)

                                formToAdd = MediaType.VideoMII.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'o': // o - D-2 (3/4 in., videocassette)

                                formToAdd = MediaType.VideoD2.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'p': // p - 8 mm.

                                formToAdd = MediaType.Video8mm.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 'q': // q - Hi-8 mm.

                                formToAdd = MediaType.VideoHi8.toString();
                                checkTypeOfVideo(id, materialSpecific, 'f', formToAdd);
                                break;

                            case 's': // s - Blu-ray disc

                                formToAdd = MediaType.VideoBluRay.toString();
                                checkTypeOfVideo(id, materialSpecific, 'd', formToAdd);
                                break;

                            // u - Unknown

                            case 'v': // v - DVD

                                formToAdd = MediaType.VideoDVD.toString();
                                checkTypeOfVideo(id, materialSpecific, 'd', formToAdd);
                                break;

                            // z - Other

                            default:

                                formToAdd = MediaType.VideoOther.toString();
                                break;
                        }
                        form.add(formToAdd);
                    }
                    else
                    {
                        if (indexer != null && indexer.errors != null)
                        {
                            indexer.errors.addError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "Malformed 007 fixed field (too short) "+ field007.getData());
                        }
                        String formToAdd = getVideoMediaForm(materialSpecific);
                        form.add(formToAdd);
                    }
                    break;

            }
        }

        // // 008 & 006 ////

        // parse the form of item indicator from 008 and 006

        String[] formatTags = { "008", "006" };
        List<ControlField> fieldsFormat = record.getVariableFields(formatTags);

        for (ControlField fieldFormat : fieldsFormat)
        {
            String profile = "";
            int position = 0; // position we'll use

            // determine the profile
            String tag = fieldFormat.getTag();
            if (tag.equals("008"))
            {
                profile = extractProfile(record.getLeader().toString(), "leader");
            }
            else
            {
                profile = extractProfile(fieldFormat.getData(), "006");
            }

            // from profile, find position

            if (profile.equals("books") || profile.equals("computers") ||
                profile.equals("mixed") || profile.equals("music") ||
                profile.equals("serial"))
            {
                position = 23;
            }
            else if (profile.equals("maps") || profile.equals("visual"))
            {
                position = 29;
            }
            else
            {
                continue; // bad profile?
            }

            // 006 follows same positions as 008, only shifted down seven spots

            if (tag.equals("006"))
            {
                position = position - 7;
            }

            String field = fieldFormat.getData();

            // make sure field has sufficient length

            if (field.length() - 1 < position)
            {
                if (indexer != null && indexer.errors != null)
                {
                    indexer.errors.addError(record.getControlNumber(), tag, "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+tag+" is shorter than it ought to be");
                }
                continue;
            }

            char code = field.toLowerCase().charAt(position);

            switch (code) // form of item
            {
                case 'a': // a - Microfilm

                    form.add(MediaType.Microfilm.toString());
                    break;

                case 'b': // b - Microfiche

                    form.add(MediaType.Microfiche.toString());
                    break;

                case 'c': // c - Microopaque

                    form.add(MediaType.Microopaque.toString());
                    break;

                case 'd': // d - Large print

                    form.add(MediaType.PrintLarge.toString());
                    break;

                case 'f': // f - Braille

                    form.add(MediaType.Braille.toString());
                    break;

                case 'o': // o - Online

                    form.add(MediaType.Online.toString());
                    break;

                case 'q': // q - Direct electronic

                    form.add(MediaType.ElectronicDirect.toString());
                    break;

                case 's': // s - Electronic

                    form.add(MediaType.Electronic.toString());
                    break;

                case 'r': // r - Regular print reproduction

                    form.add(MediaType.Print.toString());
                    break;
            }
        }

        return form;
    }

    /**
     * Return the type of video item (cartridge, disc, reel, cassette) given the single letter from 007/01 that encodes this value.
     * 
     * @param materialSpecific 
     *              letter for the form of the video (taken from 007/01)
     * @returns string
     */
    private String getVideoMediaForm(char materialSpecific)
    {
        String form = null;
        switch (materialSpecific) 
        {
            case 'c': // c - Videocartridge

                form = MediaType.VideoCartridge.toString();
                break;

            case 'd': // d - Videodisc

                form = MediaType.VideoDisc.toString();
                break;

            case 'f': // f - Videocassette

                form = MediaType.VideoCassette.toString();
                break;

            case 'r': // r - Videoreel

                form = MediaType.VideoReel.toString();
                break;

            // # - Not applicable or no attempt to code
            // [OBSOLETE, 1980]
            // n - Not applicable [OBSOLETE, 1981]
            // u - Unspecified
            // z - Other

            default:
                form = MediaType.VideoOther.toString();
                break;
        }
        return form;
    }

    /**
     * Compare the type of video item (cartridge, disc, reel, cassette) with the expected value for the type given the assigned form
     * For instance is the assigned form is DVD and the type if video item is cassette, it's probably an error.
     * 
     * @param id
     *              id of the record being processed
     * @param materialSpecific 
     *              letter for the form of the video (taken from 007/01)
     * @param expectedVal 
     *              letter for expected form of the video (based on the assignedForm)
     * @param assignedForm 
     *              String for the type of video (assigned based on 007/04)
     */
    private void checkTypeOfVideo(String id, char materialSpecific, char expectedVal, String assignedForm)
    {
        String videoMediaForm = getVideoMediaForm(materialSpecific);
        if (materialSpecific != expectedVal)
        {
            if (indexer != null && indexer.errors != null)
            {
                String errMsg = "Mismatch between form of video (007/01)" + videoMediaForm + " and type of video (007/04)" + assignedForm;
                indexer.errors.addError(id, "007", "n/a", ErrorHandler.ERROR_TYPO, errMsg);
            }
        }      
    }

    /**
     * Return the type based on record type character from either leader or
     * 006/00
     * 
     * @param field
     *            leader or 006 as string
     * @param source
     *            whether this is leader of 006
     * @return string
     */

    protected String extractType(String field, String source)
    {
        String[] result = extractTypeProfile(field, source);
        return result[0];
    }

    /**
     * Return the profile based on record type character from either leader or
     * 006/00
     * 
     * @param field
     *            leader or 006 as string
     * @param source
     *            whether this is leader of 006
     * @return string
     */

    protected String extractProfile(String field, String source)
    {
        String[] result = extractTypeProfile(field, source);
        return result[1];
    }

    /**
     * Return the profile and type based on record type character from either
     * leader or 006/00
     * 
     * @param field
     *            leader or 006 as string
     * @param source
     *            whether this is leader of 006
     * @return array as [type,profile]
     */

    protected String[] extractTypeProfile(String field, String source)
    {
        String profile = ""; // we'll use this to determine which positions
                                // of the 006/008 to use
        String type = ""; // we'll use this to set a default type

        char recordType = ' ';

        if (source.equals("leader"))
        {
            recordType = field.toLowerCase().charAt(6);
        }
        else if (source.equals("006"))
        {
            recordType = field.toLowerCase().charAt(0);
        }

        switch (recordType) {
            case 'a': // a - Language material
            case 't': // t - Manuscript language material
                if (source.equals("006"))
                {
                    type = recordType == 'a' ? ContentType.Book.toString() : ContentType.Manuscript.toString();
                    profile = "books";
                }
                else if (source.equals("leader"))
                {
                    char leader7 = field.toLowerCase().charAt(7); // bibliographic
                                                                    // level

                    switch (leader7) {
                        case 'a': // a - Monographic component part

                            type = ContentType.BookComponentPart.toString();
                            profile = "books";
                            break;

                        case 'b': // b - Serial component part

                            type = ContentType.SerialComponentPart.toString();
                            profile = "serial";
                            break;

                        case 'c': // c - Collection

                            type = ContentType.BookCollection.toString();
                            profile = "books";
                            break;

                        case 'd': // d - Subunit

                            type = ContentType.BookSubunit.toString();
                            profile = "books";
                            break;

                        case 'i': // i - Integrating resource

                            type = ContentType.SerialIntegratingResource.toString();
                            profile = "serial";
                            break;

                        case 'p': // p - Pamphlet [OBSOLETE, 1988] [CAN/MARC
                                    // only]

                            type = ContentType.Pamphlet.toString();
                            // no profile
                            break;

                        case 'm': // m - Monograph/Item

                            type = ContentType.Book.toString();
                            profile = "books";
                            break;

                        case 's': // s - Serial

                            type = ContentType.Serial.toString();
                            profile = "serial";
                            break;
                    }
                }

                break;

            case 'b': // b - Archival and manuscripts control [OBSOLETE, 1995]

                type = ContentType.Manuscript.toString();
                // no profile
                break;

            case 'c': // c - Notated music

                type = ContentType.MusicalScore.toString();
                profile = "music";
                break;

            case 'd': // d - Manuscript notated music

                type = ContentType.MusicalScoreManuscript.toString();
                profile = "music";
                break;

            case 'e': // e - Cartographic material

                type = ContentType.Map.toString();
                profile = "maps";
                break;

            case 'f': // f - Manuscript cartographic material

                type = ContentType.MapManuscript.toString();
                profile = "maps";
                break;

            case 'g': // g - Projected medium

                type = ContentType.ProjectedMedium.toString();
                profile = "visual";
                break;

            case 'h': // h - Microform publications [OBSOLETE, 1972] [USMARC
                        // only]

                // no type, since this is a physical form not a material type
                // no profile
                break;

            case 'i': // i - Nonmusical sound recording

                type = ContentType.SoundRecording.toString();
                profile = "music";
                break;

            case 'j': // j - Musical sound recording

                type = ContentType.MusicRecording.toString();
                profile = "music";
                break;

            case 'k': // k - Two-dimensional nonprojectable graphic

                type = ContentType.Image.toString();
                profile = "visual";
                break;

            case 'm': // m - Computer file

                type = ContentType.ComputerFile.toString();
                profile = "computers";
                break;

            case 'n': // n - Special instructional material [OBSOLETE, 1983]

                type = ContentType.SpecialInstructionalMaterial.toString();
                // no profile
                break;

            case 'o': // o - Kit

                type = ContentType.Kit.toString();
                profile = "visual";
                break;

            case 'p': // p - Mixed materials

                type = ContentType.MixedMaterial.toString();
                profile = "mixed";
                break;

            case 'r': // r - Three-dimensional artifact or naturally occurring
                        // object

                type = ContentType.PhysicalObject.toString();
                profile = "visual";
                break;

            case 's': // s - Serial/Integrating resource - Continuing
                        // Resources

                // only the 006 uses this value, so make sure

                if (source.equals("006"))
                {
                    type = ContentType.Serial.toString();
                    profile = "serial";
                }

            //case 't': // t - Manuscript language material

                //combined above with case 'a'
        }

        String[] result = { type, profile };

        return result;
    }

    /**
     * Whether the record contains a full-text link
     * 
     * @param Record
     *            record
     * @return Boolean
     */

    public Boolean hasFullText(final Record record)
    {
        Set<String> urls = indexer.getFullTextUrls(record);

        if (urls.size() == 0)
        {
            return false;
        }
        else
        {
            // double check by looking for loc.gov, which is not accounted for
            // in parent class
            // suggest this be factored out to SolrIndexer

            boolean isFullText = false;

            List<VariableField> list856 = (List<VariableField>)record.getVariableFields("856");

            for (VariableField vf : list856)
            {
                DataField df = (DataField) vf;
                List<String> possUrls = Utils.getSubfieldStrings(df, 'u');

                for (String url : possUrls)
                {
                    if (!url.toLowerCase().contains("loc.gov"))
                    {
                        isFullText = true;
                    }
                }
            }

            return isFullText;
        }
    }
    
    /**
     * Shift an element to the front of our list
     *
     * @param formats list
     * @param add item to add
     * @return
     */

    protected Set<String> addToTop(Set<String> formats, String add )
    {
        // create a new list, and add ours first

        Set<String> temp = new LinkedHashSet<String>();
        temp.add(add);

        // now add all the existing ones
        temp.addAll(formats);

        return temp;
    }
}

