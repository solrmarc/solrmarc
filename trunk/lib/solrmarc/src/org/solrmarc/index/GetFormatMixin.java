package org.solrmarc.index;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.marc4j.ErrorHandler;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.tools.Utils;

public class GetFormatMixin extends SolrIndexerMixin
{
    Set<String> errorsFound = null;
    
    public void perRecordInit(Record record)
    {
        errorsFound = new LinkedHashSet<String>();
    }
    
    public void addFormatError(String controlNum, String field, String subfield, int severity, String message)
    {
        String errorStr = controlNum+":"+field+":"+subfield+" : "+message;
        if (!errorsFound.contains(errorStr))
        {
            errorsFound.add(errorStr);
            if (indexer != null && indexer.errors != null)
            {
                indexer.errors.addError(controlNum, field, subfield, severity, "GetFormatMixin - "+message);
            }
        }
    }
    
    private enum ProfileType
    {
        NoneDefined,
        Books,
        Computers,
        Maps,
        Music,
        Serial,
        Visual,
        Mixed;
        @Override public String toString()
        {
            return "ProfileType." + name();
        }
    };
    
    private enum ContentType
    {
        NoneDefined,
        Art,
        ArtReproduction,
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
        Equipment,
        Game,
        GovernmentDocumentFederal,
        GovernmentDocumentState,
        GovernmentDocumentStateUniversity,
        GovernmentDocumentLocal,
        GovernmentDocumentInternational,
        GovernmentDocumentOther,
        Graphic,
        Image,
        Kit,
        LooseLeaf,
        Manuscript,
        Map,
        MapAtlas,
        MapBound,
        MapGlobe,
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
        VisualKit,
        Website;
        @Override public String toString()
        {
            return "ContentType." + name();
        }
    }

    
    private enum MediaType
    {
        ActivityCard,
        Atlas,
        Braille,
        Broadside,
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
        Electronic245,
        ElectronicDirect,
        FilmCartridge, 
        FilmCassette,
        FilmOther,
        Film8mm,
        FilmSuper8mm,
        Film9_5mm,
        Film16mm,
        Film28mm,
        Film35mm,
        FilmRoll, 
        FilmReel, 
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
        OnlineExtra,
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
        SoundRecordingOnline,
        SoundRoll,
        SoundTapeReel,
        SoundTrackFilm,
        SoundWireRecording,
        StudyPrint,
        TactileCombination,
        TactileMoon,
        TactileNoWritingSystem,
        TactileOther,
        TechnicalDrawing,
        TextOther,
        Transparency,
        TypeObsolete,
        Video8mm(0.6),
        VideoBeta(0.6),
        VideoBetacam(0.6),
        VideoBetacamSP(0.6),
        VideoBluRay(0.75),
        VideoCartridge(0.7),
        VideoCassette(0.7),
        VideoCapacitance(0.8),
        VideoD2(0.6),
        VideoDisc(0.7),
        VideoDVD(0.75),
        VideoEIAJ(0.6),
        VideoHi8(0.6),
        VideoLaserdisc(0.8),
        VideoMII(0.6),
        VideoOther(0.6),
        VideoOnline(0.8),
        VideoQuadruplex(0.6),
        VideoReel(0.7),
        VideoSuperVHS(0.6),
        VideoTypeC(0.6),
        VideoUMatic(0.6),
        VideoVHS(0.75), 
        VideoVHS_Heuristic(0.9, VideoVHS), 
        VideoDVD_Heuristic(0.9, VideoDVD), 
        VideoLaserdisc_Heuristic(0.85, VideoLaserdisc),
        VideoBeta_Heuristic(0.65, VideoBeta);
        private double priority;
        private MediaType mapsTo;
        private boolean isHeuristic;
        private String fromFields;
        MediaType() { priority = 0.6; isHeuristic = false; mapsTo = null; fromFields = null;}
        MediaType(double priority) { this.priority = priority; isHeuristic = false; mapsTo = null; fromFields = null;}
        MediaType(double priority, MediaType mapsTo) { this.priority = priority; this.mapsTo = mapsTo; isHeuristic = true; fromFields = null;}
        MediaType(double priority, MediaType mapsTo, String fromField) { this.priority = priority; this.mapsTo = mapsTo; isHeuristic = true; fromFields = fromField;}
        public MediaType mapsTo()
        {
            if (this.mapsTo != null) return(this.mapsTo);
            else return(this);
        }
        public static MediaType selectBest(MediaType t1, MediaType t2)
        {
            if (t1.priority >= t2.priority) return(t1);
            else return(t2);
        }
        public double sigmoidProb()
        {
            double sigmoid = 1 / ( 1 + Math.exp(-1 * (2.0 * (priority -0.5))));
            return(sigmoid);
            
        }
        @Override public String toString()
        {
            return "MediaType." + name();
        }
    };

    private class MediaTypeHeuristic 
    {
        private double priority;
        private MediaType mapsTo;
        private boolean isHeuristic;
        private String fromFields;
        MediaTypeHeuristic(MediaType mapsTo, double priority, String fromField) { this.priority = priority; isHeuristic = false; this.mapsTo = mapsTo; fromFields = fromField;}

        MediaTypeHeuristic(MediaType mapsTo) { priority = 0.5; isHeuristic = false; this.mapsTo = mapsTo; fromFields = null;}
        void combine(MediaTypeHeuristic mth2)
        {
            double newPriority = ((this.priority - 0.5) +  (mth2.priority - 0.5) + 0.5);
            String newFields = (this.fromFields.contains(mth2.fromFields)) ? this.fromFields : (this.fromFields + ":" + mth2.fromFields);
            this.priority = newPriority;
            this.fromFields = newFields;
        }
        public double sigmoidProb()
        {
            double sigmoid = 1 / ( 1 + Math.exp(-1 * (2.0 * (priority -0.5))));
            return(sigmoid);
            
        }


    };
    
    private enum FormOfItem
    {
        Microfilm, 
        Microfiche, 
        Microopaque, 
        PrintLarge, 
        Braille, 
        Online, 
        ElectronicDirect, 
        Electronic, 
        Print;
        @Override public String toString()
        {
            return "FormOfItem." + name();
        }
    }
    
    private enum CombinedType
    {
        EBook,
        EJournal;
        @Override public String toString()
        {
            return "CombinedType." + name();
        }
    }

    private enum ControlType
    {
        Archive;
        @Override public String toString()
        {
            return "ControlType." + name();
        }
    }
    private static LinkedHashMap<Character, ProfileType> mainProfileMap = new LinkedHashMap<Character, ProfileType>() {
        {
            put( 'a', ProfileType.Books);                   //  a - Book
            put( 'b', ProfileType.NoneDefined);             //  b - Archival and manuscripts control OBSOLETE, 1995
            put( 'c', ProfileType.Music);                   //  c - Notated music
            put( 'd', ProfileType.Music);                   //  d - Manuscript notated music
            put( 'e', ProfileType.Maps);                    //  e - Cartographic material
            put( 'f', ProfileType.Maps);                    //  f - Manuscript cartographic material
            put( 'g', ProfileType.Visual);                  //  g - Projected medium
            put( 'h', ProfileType.NoneDefined);             //  h - Microform publications [OBSOLETE, 1972] [USMARC only]
            put( 'i', ProfileType.Music);                   //  i - Nonmusical sound recording
            put( 'j', ProfileType.Music);                   //  j - Musical sound recording
            put( 'k', ProfileType.Visual);                  //  k - Two-dimensional nonprojectable graphic
            put( 'm', ProfileType.Computers);               //  m - Computer file
            put( 'n', ProfileType.NoneDefined);             //  n - Special instructional material [OBSOLETE, 1983]
            put( 'o', ProfileType.Visual);                  //  o - Kit
            put( 'p', ProfileType.Mixed);                   //  p - Mixed materials
            put( 'r', ProfileType.Visual);                  //  r - Three-dimensional artifact or naturally occurring object
            put( 's', ProfileType.Serial);                  //  s - Serial/Integrating resource - Continuing Resources
            put( 't', ProfileType.Books);                   //  t - Manuscript language material
        }
    };
    
    private static LinkedHashMap<Character, ProfileType> mainSubProfileMap = new LinkedHashMap<Character, ProfileType>() {
        {           
            put( 'a', ProfileType.Books);                   //  a - Monographic component part
            put( 'b', ProfileType.Serial);                  //  b - Serial component part
            put( 'c', ProfileType.Books);                   //  c - Collection
            put( 'd', ProfileType.Books);                   //  d - Subunit
            put( 'i', ProfileType.Serial);                  //  i - Integrating resource
            put( 'p', ProfileType.NoneDefined);             // p - Pamphlet [OBSOLETE, 1988] [CAN/MARC only]
            put( 'm', ProfileType.Books);                   // m - Monograph/Item
            put( 's', ProfileType.Serial);                  //  s - Serial
        }
    };
    
    private static LinkedHashMap<String, ContentType[]> field245hTypeMap = new LinkedHashMap<String, ContentType[]>() {
        {
            put ( "art original", new ContentType[]{ContentType.Art});
            put ( "art reproduction", new ContentType[]{ContentType.ArtReproduction});
   //         put ( "computer file");
            put ( "cartographic material", new ContentType[]{ ContentType.Map, ContentType.MapManuscript, ContentType.MapSingle, ContentType.MapSeries, 
                                                              ContentType.MapSerial, ContentType.MapGlobe, ContentType.MapAtlas, ContentType.MapSeparate, ContentType.MapBound });
//          3133  electronic book
//        740178  electronic resource
    //        put ( "graphic"
//         30580  manuscript
  //          put( "microform", new ContentType[]{ MediaType.Microform});
//          1341  picture
//           145  series record
            put ( "slide", new ContentType[]{ ContentType.Slide});
            put ( "sound recording", new ContentType[]{ ContentType.MusicRecording, ContentType.SoundRecording});
            put ( "videorecording", new ContentType[]{ ContentType.Video });
            put ( "videocassette", new ContentType[]{ ContentType.Video });
        }
    };
    
    private static LinkedHashMap<Character, ContentType> mainTypeMap = new LinkedHashMap<Character, ContentType>() {
        {
            put( 'a', ContentType.Book);                    //  a - Book
            put( 'b', ContentType.Manuscript);              //  b - Archival and manuscripts control OBSOLETE, 1995
            put( 'c', ContentType.MusicalScore);            //  c - Notated music
            put( 'd', ContentType.MusicalScoreManuscript);  //  d - Manuscript notated music
            put( 'e', ContentType.Map);                     //  e - Cartographic material
            put( 'f', ContentType.MapManuscript);           //  f - Manuscript cartographic material
            put( 'g', ContentType.ProjectedMedium);         //  g - Projected medium
            put( 'h', ContentType.NoneDefined);             //  h - Microform publications [OBSOLETE, 1972] [USMARC only]
            put( 'i', ContentType.SoundRecording);          //  i - Nonmusical sound recording
            put( 'j', ContentType.MusicRecording);          //  j - Musical sound recording
            put( 'k', ContentType.Image);                   //  k - Two-dimensional nonprojectable graphic
            put( 'm', ContentType.ComputerFile);            //  m - Computer file
            put( 'n', ContentType.NoneDefined);             //  n - Special instructional material [OBSOLETE, 1983]
            put( 'o', ContentType.Kit);                     //  o - Kit
            put( 'p', ContentType.MixedMaterial);           //  p - Mixed materials
            put( 'r', ContentType.PhysicalObject);          //  r - Three-dimensional artifact or naturally occurring object
            put( 's', ContentType.Serial);                  //  s - Serial/Integrating resource - Continuing Resources
            put( 't', ContentType.Manuscript);              //  t - Manuscript language material
        }
    };
    
    private static LinkedHashMap<Character, ContentType> mainSubTypeMap = new LinkedHashMap<Character, ContentType>() {
        {           
            put( 'a', ContentType.BookComponentPart);       //  a - Monographic component part
            put( 'b', ContentType.SerialComponentPart);     //  b - Serial component part
            put( 'c', ContentType.BookCollection);          //  c - Collection
            put( 'd', ContentType.BookSubunit);             //  d - Subunit
            put( 'i', ContentType.SerialIntegratingResource);     //  i - Integrating resource
            put( 'p', ContentType.Pamphlet);                // p - Pamphlet [OBSOLETE, 1988] [CAN/MARC only]
            put( 'm', ContentType.Book);                    // m - Monograph/Item
            put( 's', ContentType.Serial);                  //  s - Serial
        }
    };

    private static LinkedHashMap<Character, ContentType> computersSubTypes = new LinkedHashMap<Character, ContentType>() {
        { 
            put( 'a', ContentType.ComputerNumericData );            // a - Numeric data
            put( 'b', ContentType.ComputerProgram );                // b - Computer program
            put( 'c', ContentType.ComputerRepresentational);        // c - Representational
            put( 'd', ContentType.ComputerDocument);                // d - Document
            put( 'e', ContentType.ComputerBibliographicData);       // e - Bibliographic data
            put( 'f', ContentType.ComputerFont);                    // f - Font
            put( 'g', ContentType.ComputerGame);                    // g - Game
            put( 'h', ContentType.ComputerSound);                   // h - Sound
            put( 'i', ContentType.ComputerInteractiveMultimedia);   // i - Interactive multimedia
            put( 'j', ContentType.ComputerOnlineSystem);            // j - Online system or service
            put( 'm', ContentType.ComputerCombination);             // m - Combination
            put( 'j', ContentType.ComputerOnlineSystem);            // j - Online system or service
            put( 'u', ContentType.ComputerFile);                    // u - Unknown
            put( 'z', ContentType.ComputerFile);                    // z - Other
            put( ' ', ContentType.ComputerFile);                    //   - Anything else
        }
    };
    
    private static LinkedHashMap<Character, ContentType> visualSubTypes = new LinkedHashMap<Character, ContentType>() {
        {
            put( 'a', ContentType.Art);                             // a - Art original   
            put( 'b', ContentType.VisualKit);                       // b - Kit
            put( 'c', ContentType.ArtReproduction);                 // c - Art reproduction
            put( 'd', ContentType.Diorama);                         // d - Diorama
            put( 'f', ContentType.Filmstrip);                       // f - Filmstrip
            put( 'g', ContentType.Game);                            // g - Game
            put( 'i', ContentType.Picture);                         // i - Picture
            put( 'k', ContentType.Graphic);                         // k - Graphic
            put( 'l', ContentType.TechnicalDrawing);                // l - Technical drawing
            put( 'm', ContentType.MotionPicture);                   // m - Motion picture
            put( 'n', ContentType.Chart);                           // n - Chart
            put( 'o', ContentType.FlashCard);                       // o - Flash card
            put( 'p', ContentType.MicroscopeSlide);                 // p - Microscope slide
            put( 'q', ContentType.Model);                           // q - Model
            put( 'r', ContentType.Realia);                          // r - Realia
            put( 's', ContentType.Slide);                           // s - Slide
            put( 't', ContentType.Transparency);                    // t - Transparency
            put( 'v', ContentType.Video);                           // v - Videorecording
            put( 'w', ContentType.Toy);                             // w - Toy
        }
    };
    
    private static LinkedHashMap<Character, String> visualValidSubTypes = new LinkedHashMap<Character, String>() {
        {
            put( 'a', "kr");                                        // a - Art original   
            put( 'b', "o");                                         // b - Kit
            put( 'c', "kr");                                        // c - Art reproduction
            put( 'd', "r");                                         // d - Diorama
            put( 'f', "g");                                         // f - Filmstrip
            put( 'g', "kr");                                        // g - Game
            put( 'i', "kr");                                        // i - Picture
            put( 'k', "k");                                         // k - Graphic
            put( 'l', "k");                                         // l - Technical drawing
            put( 'm', "g");                                         // m - Motion picture
            put( 'n', "k");                                         // n - Chart
            put( 'o', "k");                                         // o - Flash card
            put( 'p', "r");                                         // p - Microscope slide
            put( 'q', "r");                                         // q - Model
            put( 'r', "r");                                         // r - Realia
            put( 's', "gk");                                        // s - Slide
            put( 't', "gk");                                        // t - Transparency
            put( 'v', "g");                                         // v - Videorecording
            put( 'w', "r");                                         // w - Toy
        }
    };

    private static LinkedHashMap<Character, ContentType> mapsSubTypes = new LinkedHashMap<Character, ContentType>() {
        { 
            put( 'a', ContentType.MapSingle );                      // a - Single map
            put( 'b', ContentType.MapSeries );                      // b - Map series
            put( 'c', ContentType.MapSerial);                       // c - Map serial
            put( 'd', ContentType.MapGlobe);                        // d - Globe
            put( 'e', ContentType.MapAtlas);                        // e - Atlas
            put( 'f', ContentType.MapSeparate);                     // f - Separate supplement to another work
            put( 'g', ContentType.MapBound);                        // g - Bound as part of another work
            put( 'u', ContentType.Map);                             // u - Unknown
            put( 'z', ContentType.Map);                             // z - Other
            put( ' ', ContentType.Map);                             //   - Anything else
        }
    };
    
    private static LinkedHashMap<Character, ContentType> serialsSubTypes = new LinkedHashMap<Character, ContentType>() {
        { 
            put( 'd', ContentType.Database );                       // d - updating database
            put( 'l', ContentType.LooseLeaf );                      // l - Updating loose-leaf
            put( 'm', ContentType.BookSeries);                      // m - Monographic series
            put( 'n', ContentType.Newspaper);                       // n - Newspaper
            put( 'p', ContentType.Periodical);                      // p - Periodical
            put( 'w', ContentType.Website);                         // w - Updating Web site
            put( ' ', ContentType.Serial);                          //   - Anything else
        }
    };
    
    private static LinkedHashMap<Character, ContentType> govDocTypes = new LinkedHashMap<Character, ContentType>() {
        { 
            put( 'a', ContentType.GovernmentDocumentOther );        // a - Autonomous or semi-autonomous component
            put( 'c', ContentType.GovernmentDocumentLocal );        // c - Multilocal
            put( 'f', ContentType.GovernmentDocumentFederal);       // f - Federal/national
            put( 'i', ContentType.GovernmentDocumentInternational); // i - International intergovernmental
            put( 'l', ContentType.GovernmentDocumentLocal);         // l - Local
            put( 'm', ContentType.GovernmentDocumentState);         // m - Multistate
            put( 'o', ContentType.GovernmentDocumentOther);         // o - Government publication-level undetermined
        //  put( 's', ContentType.GovernmentDocumentState);         // s - State, provincial, territorial, dependent, etc.
        //  put( 's', ContentType.GovernmentDocumentStateUniversity);         // s - State, provincial, territorial, dependent, etc.
            put( 'z', ContentType.GovernmentDocumentOther);         // z - Other
        }
    };
    
    // used for mapping the 007 field(s)
    private static LinkedHashMap<String, MediaType> mediaTypeMap = new LinkedHashMap<String, MediaType>() {
        {
            // maps
            put( "ad", MediaType.Atlas);                        //  ad - Atlas
            put( "ag", MediaType.MapDiagram);                   //  ag - Diagram
            put( "aj", MediaType.Map);                          //  aj - Map
            put( "ak", MediaType.MapProfile);                   //  ak - Manuscript notated music
            put( "aq", MediaType.MapModel);                     //  aq - Model
            put( "ar", MediaType.SensorImage);                  //  ar - Remote-sensing image
            put( "as", MediaType.MapSection);                   //  as - Section
            put( "ay", MediaType.MapView);                      //  ay - View
            put( "az", MediaType.MapOther);                     //  az - Other Map
            
            put( "aa", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ab", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ac", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ah", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ai", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "am", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "an", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ao", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ap", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "at", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "av", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "aw", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            put( "ax", MediaType.TypeObsolete);                 //  aa ab ac ah ai am an ao ap at av aw ax - Obsolete Map formats
            
            // electronic resource
            put( "ca", MediaType.ComputerTapeCartridge);        // ca - Tape cartridge
            put( "cb", MediaType.ComputerChipCartridge);        // cb - Chip cartridge
            put( "cc", MediaType.ComputerOpticalDiscCartridge); // cc - Computer optical disc cartridge
            put( "cd", MediaType.ComputerDisk);                 // cd - Computer disc, type unspecified
            put( "ce", MediaType.ComputerDiscCartridge);        // ce - Computer disc cartridge, type unspecified
            put( "cf", MediaType.ComputerTapeCassette);         // cf - Tape cassette
            put( "ch", MediaType.ComputerTapeReel);             // ch - Tape reel
            put( "cj", MediaType.ComputerFloppyDisk);           // cj - Magnetic disk
            put( "ck", MediaType.ComputerCard);                 // ck - Computer card
            put( "cm", MediaType.ComputerMagnetoOpticalDisc);   // cm - Magneto-optical disc
            put( "co", MediaType.ComputerOpticalDisc);          // co - Optical disc
            put( "cr", MediaType.Online);                       // cr - Remote
            put( "cu", MediaType.ComputerOther);                // cu - Unspecified
            put( "cz", MediaType.ComputerOther);                // cz - Other
            
            // globe
            put( "da", MediaType.GlobeCelestial);				// da - Celestial globe
            put( "db", MediaType.GlobePlanetary);				// db - Planetary or lunar globe
            put( "dc", MediaType.GlobeTerrestrial);             // dc - Terrestrial globe
            put( "dd", MediaType.TypeObsolete);                 // dd - Satellite globe (of our solar system), excluding the earth moon [OBSOLETE, 1997] [CAN/MARC only]
            put( "de", MediaType.GlobeEarthMoon);				// de - Earth moon globe
            put( "du", MediaType.GlobeOther);                   // du - Unspecified
            put( "dz", MediaType.GlobeOther);                   // dz - Other
            
            // tactile material
            put( "fa", MediaType.TactileMoon);				    // fa - Moon
            put( "fb", MediaType.Braille);                      // fb - Braille
            put( "fc", MediaType.TactileCombination);           // fc - Combination
            put( "fd", MediaType.TactileNoWritingSystem);       // fd - Tactile, with no writing system
            put( "fu", MediaType.TactileOther);                 // fu - Unspecified
            put( "fz", MediaType.TactileOther);                 // fz - Other

            // projected graphic
            put( "gc", MediaType.FilmstripCartridge);           // gc - Filmstrip cartridge
            put( "gd", MediaType.Filmslip);                     // gd - Filmslip
            put( "gf", MediaType.Filmstrip);                    // gf - Filmstrip, type unspecified
            put( "gn", MediaType.TypeObsolete);                 // gn - Not applicable [OBSOLETE, 1981] [USMARC only]
            put( "go", MediaType.FilmstripRoll);				// go - Filmstrip roll
            put( "gs", MediaType.Slide);                        // gs - Slide
            put( "gt", MediaType.Transparency);                 // gt - Transparency
            put( "gu", MediaType.ProjectedMediumOther);         // gu - Unspecified
            put( "gz", MediaType.ProjectedMediumOther);         // gz - Other

            // microform
            put( "ha", MediaType.MicroformApetureCard);         // ha - Aperture card
            put( "hb", MediaType.MicrofilmCartridge);           // hb - Microfilm cartridge
            put( "hc", MediaType.MicrofilmCassette);            // hc - Microfilm cassette
            put( "hd", MediaType.MicrofilmReel);                // hd - Microfilm reel
            put( "he", MediaType.Microfiche);                   // he - Microfiche
            put( "hf", MediaType.MicroficheCassette);           // hf - Microfiche cassette
            put( "hg", MediaType.Microopaque);                  // hg - Microopaque
            put( "hh", MediaType.MicrofilmSlip);                // hh - Microfilm slip
            put( "hj", MediaType.MicrofilmRoll);                // hj - Microfilm roll
            put( "hu", MediaType.Microform);                    // hu - Unspecified
            put( "hz", MediaType.Microform);                    // hz - Other

            // non-projected graphic
            put( "ka", MediaType.ActivityCard);                 // ka - Activity card
            put( "kc", MediaType.Collage);                      // kc - Collage
            put( "kd", MediaType.Drawing);                      // kd - Drawing
            put( "ke", MediaType.Painting);                     // ke - Painting
            put( "kf", MediaType.PhotomechanicalPrint);         // kf - Photomechanical print
            put( "kg", MediaType.Photonegative);                // kg - Photonegative
            put( "kh", MediaType.PhotoPrint);                   // kh - Photoprint
            put( "ki", MediaType.Picture);                      // ki - Picture
            put( "kj", MediaType.ImagePrint);                   // kj - Print
            put( "kk", MediaType.Poster);                       // kk - Poster
            put( "kl", MediaType.TechnicalDrawing);             // kl - Technical drawing
            put( "kn", MediaType.Chart);                        // kn - Chart
            put( "ko", MediaType.FlashCard);                    // ko - Flash card
            put( "kp", MediaType.Postcard);                     // kp - Postcard
            put( "kq", MediaType.Icon);                         // kq - Icon
            put( "kr", MediaType.Radiograph);                   // kr - Radiograph
            put( "ks", MediaType.StudyPrint);                   // ks - Study print
            put( "kv", MediaType.Photo);                        // kv - Photograph, type unspecified
            put( "ku", MediaType.ImageOther);                   // ku - Unspecified
            put( "kz", MediaType.ImageOther);                   // kz - Other

            // motion picture
            put( "mc", MediaType.FilmCartridge);                // mc - Film cartridge
            put( "mf", MediaType.FilmCassette);                 // mf - Film cassette
            put( "mo", MediaType.FilmRoll);                     // mo - Film roll
            put( "mr", MediaType.FilmReel);                     // mr - Film reel
            put( "mu", MediaType.FilmOther);                    // mu - Unspecified
            put( "mz", MediaType.FilmOther);                    // mz - Other

            put( "o?", MediaType.Kit);                          // o - kit
            put( "q?", MediaType.MusicalScore);                 // q - notated music
            put( "r?", MediaType.SensorImage);                  // r - remote-sensing image

            // sound recording
            put( "sd.a", MediaType.SoundDiscLP);                    // sd - Sound disc
            put( "sd.b", MediaType.SoundDiscLP);                    // sd - Sound disc
            put( "sd.c", MediaType.SoundDiscLP);                    // sd - Sound disc
            put( "sd.d", MediaType.SoundDiscLP);                    // sd - Sound disc
            put( "sd.f", MediaType.SoundDiscCD);                    // sd - Sound disc
            put( "sd", MediaType.SoundDisc);                    // sd - Sound disc
            put( "sc", MediaType.TypeObsolete);                 // sc - Cylinder [OBSOLETE]
            put( "se", MediaType.SoundCylinder);                // se - Cylinder
            put( "sf", MediaType.TypeObsolete);                 // sf - Sound-track film [OBSOLETE]
            put( "sg", MediaType.SoundCartridge);               // sg - Sound cartridge
            put( "si", MediaType.SoundTrackFilm);               // si - Sound-track film
            put( "sr", MediaType.TypeObsolete);                 // sr - Roll [OBSOLETE]
            put( "sq", MediaType.SoundRoll);                    // sq - Roll
            put( "ss", MediaType.SoundCassette);                // ss - Sound cassette
            put( "st", MediaType.SoundTapeReel);                // st - Sound-tape reel
            put( "sw", MediaType.SoundWireRecording);           // sw - Wire recording
            put( "su", MediaType.SoundRecordingOther);          // su - Unspecified
         //   put( "sz", MediaType.SoundRecordingOther);          // sz - Other  // needs special handling

            // text
            put( "ta", MediaType.Print);                        // ta - Regular print
            put( "tb", MediaType.PrintLarge);                   // tb - Large print
            put( "tc", MediaType.Braille);                      // tc - Braille
            put( "td", MediaType.LooseLeaf);                    // td - Loose-leaf
            put( "tu", MediaType.TextOther);                    // tu - Unspecified
            put( "tz", MediaType.TextOther);                    // tz - Other
            
            // video recording
             put( "v...a", MediaType.VideoBeta);                // vf--a - Beta (1/2 in., videocassette)
             put( "v...b", MediaType.VideoVHS);                 // vf--b - VHS (1/2 in., videocassette)
             put( "v...c", MediaType.VideoUMatic);              // vf--c - U-matic (3/4 in., videocasstte)
             put( "v...d", MediaType.VideoEIAJ);                // vr--d - EIAJ (1/2 in., reel)
             put( "v...e", MediaType.VideoTypeC);               // vr--e - Type C (1 in., reel)
             put( "v...f", MediaType.VideoQuadruplex);          // vr--f - Quadruplex (1 in. or 2 in., reel)
             put( "v...g", MediaType.VideoLaserdisc);           // vd--g - Laserdisc
             put( "v...h", MediaType.VideoCapacitance);         // vd--h - CED (Capacitance Electronic Disc) videodisc
             put( "v...i", MediaType.VideoBetacam);             // vf--i - Betacam (1/2 in., videocassette)
             put( "v...j", MediaType.VideoBetacamSP);           // vf--j - Betacam SP (1/2 in., videocassette)
             put( "v...k", MediaType.VideoSuperVHS);            // vf--k - Super-VHS (1/2 in., videocassette)
             put( "v...m", MediaType.VideoMII);                 // vf--m - M-II (1/2 in., videocassette)
             put( "v...o", MediaType.VideoD2);                  // vf--o - D-2 (3/4 in., videocassette)
             put( "v...p", MediaType.Video8mm);                 // vf--p - 8 mm.   videocassette
             put( "v...q", MediaType.VideoHi8);                 // vf--q - Hi-8 mm.  videocassette
             put( "v...m", MediaType.VideoMII);                 // vf--m - M-II (1/2 in., videocassette)
             put( "v...s", MediaType.VideoBluRay);              // vd--s - Blu-ray disc
             put( "v...v", MediaType.VideoDVD);                 // vd--v - DVD
             put( "v...n", MediaType.TypeObsolete);             // v---n - Obsolete type specification
             put( "v...?", null);                               // v---? - Obsolete type specification
             put( "v...u", MediaType.VideoOther);               // v---u - Unspecified         
    //       put( "v...z", MediaType.VideoOther);               // v---z - Other video type         // needs special handling
                                         
        }
    };
    
    // used for validating the form of a specific video item
    private static LinkedHashMap<String, Character> videoFormMap = new LinkedHashMap<String, Character>() {
        {
            // video recording
            put( "v...a", 'f');                                 // vf--a - Beta (1/2 in., videocassette)
            put( "v...b", 'f');                                 // vf--b - VHS (1/2 in., videocassette)
            put( "v...c", 'f');                                 // vf--c - U-matic (3/4 in., videocasstte)
            put( "v...d", 'r');                                 // vr--d - EIAJ (1/2 in., reel)
            put( "v...e", 'r');                                 // vr--e - Type C (1 in., reel)
            put( "v...f", 'r');                                 // vr--f - Quadruplex (1 in. or 2 in., reel)
            put( "v...g", 'd');                                 // vd--g - Laserdisc
            put( "v...h", 'd');                                 // vd--h - CED (Capacitance Electronic Disc) videodisc
            put( "v...i", 'f');                                 // vf--i - Betacam (1/2 in., videocassette)
            put( "v...j", 'f');                                 // vf--j - Betacam SP (1/2 in., videocassette)
            put( "v...k", 'f');                                 // vf--k - Super-VHS (1/2 in., videocassette)
            put( "v...m", 'f');                                 // vf--m - M-II (1/2 in., videocassette)
            put( "v...o", 'f');                                 // vf--o - D-2 (3/4 in., videocassette)
            put( "v...p", 'f');                                 // vf--p - 8 mm.   videocassette
            put( "v...q", 'f');                                 // vf--q - Hi-8 mm.  videocassette
            put( "v...m", 'f');                                 // vf--m - M-II (1/2 in., videocassette)
            put( "v...s", 'd');                                 // vd--s - Blu-ray disc
            put( "v...v", 'd');                                 // vd--v - DVD
        }
    };
        
    /**
     * Return the content type and media types, plus electronic, for this record
     * 
     * @param record  MARC Record
     * @return        Set of Strings of content types and media types
     */
    public Set<String> getContentTypesAndMediaTypesMapped(final Record record, String mapFileName)
    {
        Set<String> formats = getContentTypes(record);
        formats.addAll( getMediaTypes(record));
        if (recordIsMinimal(record))
        {
            addFormatError(record.getControlNumber(), "n/a", "n/a", ErrorHandler.MINOR_ERROR, "Record contains minimal metadata, format is likely wrong");                
        }
        formats = addOnlineTypes(record, formats, false);
        if (isArchive(record)) formats.add(ControlType.Archive.toString());
        String mapName = null;
        try
        {
            mapName = indexer.loadTranslationMap(null, mapFileName);
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> translationMap = indexer.findMap(mapName);
        Set<String>formatsMapped = Utils.remap(formats, translationMap, true);
        return(formatsMapped);
    }
    
    /**
     * Return the content type and media types, plus electronic, for this record
     * 
     * @param record MARC Record
     * @return       Set of Strings of content types and media types
     */
    public Set<String> getContentTypesAndMediaTypes(final Record record)
    {
        Set<String> formats = getContentTypes(record);
        formats.addAll( getMediaTypes(record));
        if (recordIsMinimal(record))
        {
            addFormatError(record.getControlNumber(), "n/a", "n/a", ErrorHandler.MINOR_ERROR, "Record contains minimal metadata, format is likely wrong");                
        }
        formats = addOnlineTypes(record, formats, false); 
        if (isArchive(record)) formats.add(ControlType.Archive.toString());
        return(formats);
    }
    
    private boolean recordIsMinimal(Record record)
    {
        ControlField field008 = ((ControlField)record.getVariableField("008"));
        if (field008 == null) return(true);
        String field008Str = field008.getData();
        List<?> vfs = record.getVariableFields(new String[]{"300", "538", "500"});
        if ((field008Str.startsWith("000000n")||field008Str.contains("????????????")) && vfs.size() == 0)  
            return(true);
        return (false);
    }

    /*
     * Online materials
     *   Leader/06 a, i, j, t    AND     006/06 (008/23) o OR 007/00 c AND007/01 r  
     *   Leader/06 m         AND     006/06 (008/23) o
     *   Leader/06 p, c, d   AND 006/06 (008/23) o
     *   Leader/06 g         AND     006/06 (008/23) o OR 007/00 v   AND 007/04 z 
     *   Leader/06 e, f, k, o, r     AND     006/12 (008/29) o

     */
    public boolean isOnlineFormatTypes(final Record record)
    {
        char typeOfRecord = record.getLeader().getTypeOfRecord();
        ControlField field008 = ((ControlField)record.getVariableField("008"));
        String field008Str = field008 != null ? field008.getData() : "";
        List<VariableField> fields006 = record.getVariableFields("006");
        List<VariableField> fields007 = record.getVariableFields("007");
        String types1 = "aijtpcd";
        String types2 = "efgkorm";
        if (types1.indexOf(typeOfRecord) != -1)
        {
            if (field008Str.length() > 23 && field008Str.charAt(23) == 'o') return(true);
            if (setContainsAt(fields006, 6, "o", true))   return(true);
            if (typeOfRecord == 'a' || typeOfRecord == 'i' || typeOfRecord == 'j' || typeOfRecord == 't')
            {
                if (setContainsAt(fields007, 0, "cr", true))   return(true);
            }
        }
        if (types2.indexOf(typeOfRecord) != -1)
        {
            if (field008Str.length() > 29 && field008Str.charAt(29) == 'o') return(true);
            if (setContainsAt(fields006, 12, "o", true))   return(true);
            if (typeOfRecord == 'g')
            {
                if (setContainsAt(fields007, 0, "v...z", true))   
                {
                    if (this.hasFullText(record))  return(true);
                }
            }
        }
        return(false);
    }
    
    private boolean setContainsAt(List<VariableField> fields, int offset, String match, boolean ignoreCase)
    {
        for (VariableField vf : fields)
        {
            ControlField cf = (ControlField)vf;
            String data = cf.getData();
            if (!match.contains("."))
            {
                if (data.regionMatches(ignoreCase, offset, match, 0, match.length())) 
                {
                    return(true);
                }
            }
            else
            {
                if (data.length() > offset+match.length() && data.substring(offset, offset + match.length()).matches(match))
                {
                    return(true);
                }
            }
        }
        return false;
    }

    /**
     * Add types EBook and Online for electronic items for this record
     * @param checkURLs 
     * 
     * @param record  MARC Record
     * @param formats the <code>Set</code> of formats to add the types EBook and Online to 
     * @return        <code>String</code> of primary material types
     */

    public Set<String> addOnlineTypes(final Record record, Set<String> formats, boolean checkURLs)
    {
        // see if we have full-text link

        boolean online = isOnlineFormatTypes(record);
        boolean hasFullLink = hasFullText(record);
        boolean hasSupplLink = hasSupplText(record);
        
        // if so, and this is a book, add e-book as well
        if (online && !hasFullLink && !hasSupplLink)
        {
            addFormatError(record.getControlNumber(), "856", "n/a", ErrorHandler.MINOR_ERROR, "Record claims to be \"Online\" but has no valid 856 field");                
        }
        else if (online && !hasFullLink)
        {
            formats.add(MediaType.OnlineExtra.toString());                
        }
        else if (hasFullLink && !online)
        {
            addFormatError(record.getControlNumber(), "856", "n/a", ErrorHandler.INFO, "Record has valid 856 field, but is missing declarations of online");                
        }
        
        // if so, and this is a book, add e-book as well
        if (formats.contains(ContentType.Book.toString()) && hasFullLink == true)
        {
            formats = addToTop(formats, CombinedType.EBook.toString());
        }

        if (hasFullLink == true)
        {
            formats.add(MediaType.Online.toString());
        }
        return(formats);
    }
    
    /**
     * Return the primary content type for this record
     * 
     * @param record MARC Record
     * @return       <code>String</code> of primary material types
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
     * Return the primary content type, plus electronic, for this record
     * 
     * @param record MARC Record
     * @return       String of primary material types
     */

    public Set<String> getPrimaryContentTypePlusOnline(final Record record)
    {
        Set<String> format = new LinkedHashSet<String>();

        // get primary material type

        String primaryType = getPrimaryContentType(record);
        format.add(primaryType);
        
        format = addOnlineTypes(record, format, false);
        
        return format;
    }

    /**
     * Parse out content types from record
     * 
     * @param record MARC Record
     * @return       <code>List</code> of material types
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
     * @param record MARC Record
     * @return       <code>List</code> of material types
     */

    public Set<String> getContentTypes(final Record record)
    {
        Set<String> contentTypes = new LinkedHashSet<String>(); // the list of material types

        // // Leader ////

        String leader = record.getLeader().toString();

        // get main type and profile from leader/06

        ContentType leaderType = extractType(leader, "leader"); // main material type, based on leader
        ProfileType leaderProfile = extractProfile(leader, "leader"); // 008 profile to use

        // // 008 & 006 ////

        // take both the 008 and 006 (which use the same structure, just at
        // different positions)
        // so we can iterate over them both

        String[] formatTags = { "008", "006" };
        ControlField field008 = (ControlField)record.getVariableField("008");
        List<VariableField> fields006 = (List<VariableField>)record.getVariableFields("006");

        if (field008 != null)
        {
            getContentTypeFromFixedField(contentTypes, record, field008, leaderProfile, leaderType, offsetForProfile008(leaderProfile));
        }
        for (VariableField field006v : fields006)
        {
            ControlField field006 = (ControlField)field006v;
            ProfileType profile = extractProfile(field006.getData(), "006");
            ContentType type = extractType(field006.getData(), "006");
            if (profile != ProfileType.NoneDefined)
            {
                getContentTypeFromFixedField(contentTypes, record, field006, profile, type, offsetForProfile008(profile) - 17);
            }
        }

        // / DATA FIELDS ///
        
        // thesis
        if (!record.getVariableFields("502").isEmpty())
        {
            String value502 =  ((DataField)record.getVariableField("502")).getSubfield('a') != null ? 
                 ((DataField)record.getVariableField("502")).getSubfield('a').getData() : "";
            if (value502.matches(".*[Tt]hesis.*") || value502.matches(".*[Dd]issertation.*") || value502.matches(".*[Hh]abilitation.*"))
            {
                // set the first (primary) type as thesis
                contentTypes = addToTop(contentTypes, ContentType.Thesis.toString());
    
                // nix manuscript so we can distinguish actual manuscripts
                contentTypes.remove(ContentType.Manuscript.toString());
            }
            else
            {
                contentTypes = addToTop(contentTypes, ContentType.Thesis.toString());
            }
        }

        // nothing worked?
        if (contentTypes.isEmpty())
        {
            // record must have very little data, so we'll take whatever we can
            // get isbn
            if (!record.getVariableFields("020").isEmpty())
            {
                contentTypes.add(ContentType.Book.toString());
            }

            // only type from leader was available
            else if (leaderType != ContentType.NoneDefined)
            {
                contentTypes.add(leaderType.toString());
            }
        }
        return contentTypes;
    }
    
    private void getContentTypeFromFixedField(Set<String> contentTypesStr, Record record, ControlField field, ProfileType profile, ContentType defaultType, int offsetInField)
    {
        ContentType typeToAdd = null;
        if (field.getData().length()-1 < offsetInField)
        {
            typeToAdd = defaultType;
            addFormatError(record.getControlNumber(), field.getTag(), "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+field.getTag()+" is shorter than it ought to be");                
        }
        else 
        {
            char subContentType = field.getData().charAt(offsetInField);
            switch (profile)
            {
                case Books:
                {
                    typeToAdd = defaultType;
                    break;
                }
                case Computers: 
                {
                    typeToAdd = lookupType(computersSubTypes, subContentType, defaultType); 
                    break;
                }
                case Maps:
                {
                    typeToAdd = lookupType(mapsSubTypes, subContentType, defaultType);                         
                    break;
                }
                case Music:
                {
                    typeToAdd = defaultType;
                    break;
                }
                case Serial:
                {
                    typeToAdd = lookupType(serialsSubTypes, subContentType, defaultType); 
                    break;
                }
                case Visual:
                {
                    ContentType type = lookupType(visualSubTypes, subContentType, defaultType);
                    typeToAdd = type; 
                    if (visualValidSubTypes.containsKey(subContentType))
                    {
                        String validValues = visualValidSubTypes.get(subContentType);
                        boolean isValid = false;
                        for (char c : validValues.toCharArray())
                        {
                            if (mainTypeMap.get(c).equals(defaultType)) isValid = true;
                        }
                        if (!isValid)
                        {
                            addFormatError(record.getControlNumber(), field.getTag(), "n/a", ErrorHandler.ERROR_TYPO, "Visual subtype is "+type+" which is probably not valid for type "+defaultType);
                        } 
                    }
                    break;
                }
                case Mixed:
                {
                    typeToAdd = defaultType;
                    break;
                }
            }
        }
        String field245h = indexer.getFirstFieldVal(record, null, "245h");
        if (field245h != null ) 
        {
            field245h = field245h.replaceFirst(".*?\\[([a-zA-Z ]*).*\\].*", "$1").trim();
            if (field245hTypeMap.containsKey(field245h))
            {
                boolean isValid = false;
                ContentType valid[] = field245hTypeMap.get(field245h);
                for (ContentType validType : valid)
                {
                    if (validType == typeToAdd) isValid = true;
                }
                if (isValid)
                {
                    contentTypesStr.add(typeToAdd.toString());
                }
                else
                {
                    if (!isSuperTypeOf(typeToAdd, valid[0]) || ( (field.getId() != null && (field.getId() & (long)2) == (long)2)))
                    {
                        if (typeToAdd != null)  contentTypesStr.add(typeToAdd.toString());
                    }
                    contentTypesStr.add(valid[0].toString());
                    addFormatError(record.getControlNumber(), field.getTag(), "n/a", ErrorHandler.MINOR_ERROR, "ContentType as specified in the leader/008 field conflicts with that specified in the 245h subfield");                
                }
            }
            else
            {
                if (typeToAdd != null)  contentTypesStr.add(typeToAdd.toString());
            }
        }
        else
        {
            if (typeToAdd != null)  contentTypesStr.add(typeToAdd.toString());
        }
        if (defaultType == ContentType.MapManuscript && Utils.setItemContains(contentTypesStr, "ContentType\\.Map.*"))
        {
            contentTypesStr.add(defaultType.toString());
        }
        ContentType govDocType = null;
        if (/*(profile == ProfileType.Books || profile == ProfileType.Serial ) && */(govDocType = isGovDoc(field, record)) != null)
        {
            contentTypesStr.add(govDocType.toString());
        }
        Set<String> holdings = SolrIndexer.getAllSubfields(record, "999t", "");
        for (String holding : holdings)
        {
            if (holding.equals("EQUIPMENT") || holding.equals("HS-DVDPLYR") || holding.equals("EQUIP-3DAY") || holding.equals("CELLPHONE") ||  
                    holding.equals("CALCULATOR") ||  holding.equals("LCDPANEL") ||  holding.equals("HSLAPTOP") ||  holding.equals("PROJSYSTEM") ||  
                    holding.equals("HSWIRELESS") ||  holding.equals("EQUIP-2HR") ||  holding.equals("DIGITALCAM") ||  holding.equals("AUDIO-VIS") ||  
                    holding.equals("LAPTOP") ||  holding.equals("EQUIP-3HR") ||  holding.equals("CAMCORDER"))
            {
                contentTypesStr.clear();
                contentTypesStr.add(ContentType.Equipment.toString());
            }

        }

    }
    
    static String govDocLetters = "acfilmoz";
    private ContentType isGovDoc(ControlField field, Record record)
    {
        ContentType toReturn = null;
        int offsetForGovDoc = (field.getTag().equals("008")) ? 28 : 11;
        if (field != null && field.getData().length() > offsetForGovDoc)
        {
            char govdoc = field.getData().toLowerCase().charAt(offsetForGovDoc);
            if (govDocLetters.indexOf(govdoc) != -1)
            {
                toReturn = govDocTypes.get(govdoc);
                return(toReturn);
            }
            else if (govdoc == 's')
            {
                DataField pubInfo260 = ((DataField)(record.getVariableField("260")));
                if (pubInfo260 != null)
                {
                    Subfield sfb = pubInfo260.getSubfield('b');
                    if (sfb != null && !sfb.getData().contains("Universit"))
                    {
                        return(ContentType.GovernmentDocumentState);
                    }
                    else
                    {
                        return(ContentType.GovernmentDocumentStateUniversity);
                    }
                }
            }
        }
        return null;
    }

    private boolean isSuperTypeOf(ContentType typeToAdd, ContentType contentTypeFrom245h)
    {
        if (typeToAdd == ContentType.ProjectedMedium && contentTypeFrom245h == ContentType.Video) 
            return(true);
        return false;
    }

    private ContentType lookupType(LinkedHashMap<Character, ContentType> subTypeMap, char subContentType, ContentType defaultType)
    {
        if (subTypeMap.containsKey(subContentType))
        {
            return(subTypeMap.get(subContentType));
        }
        else if (subTypeMap.containsKey(' ')) //  key not found use default value, if defined
        {
            return(subTypeMap.get(' '));
        }
        else
        {
            return(defaultType);
        }
    }

    private int offsetForProfile008(ProfileType profile)
    {
        switch (profile)
        {
            case Computers:  return(26);
            case Visual:     return(33);
            case Maps:       return(25);
            case Serial:     return(21);
            case Mixed:
            case Books:
            case Music:      return(23);
        }
        return 00;
    }

    
    
    /**
     * Parse out media / carrier types from record
     * 
     * @param record MARC Record
     * @return       <code>List</code> of material types
     */

    public Set<String> getMediaTypes(final Record record)
    {
        Set<MediaType> form = new LinkedHashSet<MediaType>(); // the list of form
                                                        // types
        ContentType leaderType = extractType(record.getLeader().toString(), "leader"); // main material type, based on leader
        ProfileType profileType = extractProfile(record.getLeader().toString(), "leader");
        // // Data Fields ////
        // electronic resource from title

        DataField title = (DataField) record.getVariableField("245");

        if (title != null && title.getSubfield('h') != null)
        {
            // general material designator in title 245|h
            if (title.getSubfield('h').getData().toLowerCase().contains("[electronic resource]"))
            {
                form.add(MediaType.Electronic245);
            }
        }

        // // 007 ////

        List<VariableField> fields007 = (List<VariableField>)record.getVariableFields("007");

        for (VariableField field007v : fields007)
        {
            ControlField field007 = (ControlField)field007v;
            // first, check to make sure this is a post-1981 007 by looking at
            // position 2, which should be undefined
            String field007Str = validate007Field(record, profileType, leaderType, field007);
            
            if (field007Str == null) continue;
            char materialGeneral =  field007Str.charAt(0);
            String materialFirst =  "" + field007Str.charAt(0) + "?";
            String materialFirstTwo = field007Str.substring(0, 2);
            String key = materialFirstTwo;
            if (materialGeneral == 'v')
            {
                key = "" + field007Str.charAt(0) + "..." + field007Str.charAt(4);
            }
            else if (materialGeneral == 's' && key.equals("sd") && mediaTypeMap.containsKey(key + "." + field007Str.charAt(3)))
            {
                key = key + "." + field007Str.charAt(3);
            }
            if (key.equals("v...z"))  // Special handling for Video Other
            {
                if (this.hasFullText(record))
                    form.add(MediaType.VideoOnline);
                else
                    form.add(MediaType.VideoOther);
            }
            else if (key.equals("sz"))  // Special handling for Sound Other Media
            {
                if (this.hasFullText(record))
                    form.add(MediaType.SoundRecordingOnline);
                else
                    form.add(MediaType.SoundRecordingOther);
            }
            else if (!mediaTypeMap.containsKey(key)) 
            {
                key = materialFirst;
            }
            // look up value in the media type map which maps the initial characters of an 007 field to a media type
            if (mediaTypeMap.containsKey(key))
            {
                MediaType result = mediaTypeMap.get(key);
                if (result == MediaType.TypeObsolete)
                {
                    addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "007 field specifies "+field007Str+ " which uses an obsolete encoding");
                }
                else if (result == MediaType.Online) 
                {
                    // Skip it?
                    result = null;
                }
                else if (result != null)
                {
                    form.add(result);
                }
                else
                {
                    result = null;
                }
            }
            else
            {
                addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "007 Format code '"+field007Str+"' is undefined, looking at other fields");
            }
            if (materialGeneral == 'v') // validate form of video (disc, reel, cassette with the format of the video.  ie. You probably don't have a VHS video disc
            {
                if (videoFormMap.containsKey(key) && videoFormMap.get(key) != field007Str.charAt(1))
                {
                    String errMsg = "Mismatch between form of video (007/01)" + field007Str.charAt(1) + " and type of video (007/04)" + key.charAt(4);
                    addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.ERROR_TYPO, errMsg);
                }
            }
        } // done with 007 fields
//        BinaryHeapPriorityQueue bestAnswers = new BinaryHeapPriorityQueue<MediaType>();
//        for (MediaType mt : form)
//        {
//            bestAnswers.add(mt, mt.priority);
//        }
        MediaTypeHeuristic type = getMediaTypeHeuristically(record, leaderType);
        if (type != null)
        {
            if (form.isEmpty())
            {
                form.add(type.mapsTo);
                String errMsg = "Media type not specified determining it heuristically " + type.mapsTo + "based on fields: " + type.fromFields;
                addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.INFO, errMsg);
            }
            else if (form.size() == 1)
            {
                MediaType specifiedForm = form.toArray(new MediaType[0])[0];
                MediaType heuristicFormMapsTo = type.mapsTo;
                if (!heuristicFormMapsTo.toString().equals(specifiedForm.toString()))
                {
                    MediaType finalAnswer = (type.sigmoidProb() > specifiedForm.sigmoidProb()) ? type.mapsTo : specifiedForm;
                    if (indexer != null)
                    {
                        String errMsg = "Mismatch between specified media type" + specifiedForm + " and heuristically determined one " + heuristicFormMapsTo + " based on fields: "+ type.fromFields;
                        addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.INFO, errMsg);
                        if (finalAnswer != specifiedForm)
                        {
                            errMsg = "Overriding specified form " + specifiedForm + " with heuristically determined one " + heuristicFormMapsTo;
                            addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, errMsg);
                        }
                    }
                    if (finalAnswer != specifiedForm)
                    {
                        form.remove(specifiedForm);
                        form.add(finalAnswer);
                    }
                }
            }
        }
        // // 008 & 006 ////

        // parse the form of item indicator from 008 and 006
        Set<String> formStr = new LinkedHashSet<String>();
        for (MediaType mt : form)
        {
            formStr.add(mt.toString());
        }
        
        String[] formatTags = { "008", "006" };
        List<VariableField> fieldsFormat = record.getVariableFields(formatTags);

        for (VariableField fieldFormatv : fieldsFormat)
        {
            ControlField fieldFormat = (ControlField)fieldFormatv;
            ProfileType profile;
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

            if (profile == ProfileType.Books || profile == ProfileType.Computers ||
                profile == ProfileType.Mixed || profile == ProfileType.Music ||
                profile == ProfileType.Serial)
            {
                position = 23;
            }
            else if (profile == ProfileType.Maps || profile == ProfileType.Visual)
            {
                position = 29;
            }
            else
            {
                continue; // bad profile?
            }
            int raw_position = position;
            // 006 follows same positions as 008, only shifted down seven spots

            if (tag.equals("006"))
            {
                position = position - 17;
            }

            String field = fieldFormat.getData();

            // make sure field has sufficient length

            if (field.length() - 1 < position)
            {
                addFormatError(record.getControlNumber(), tag, "n/a", ErrorHandler.MINOR_ERROR, "Fixed field "+tag+" is shorter than it ought to be");
                continue;
            }

            char code = field.toLowerCase().charAt(position);

            switch (code) // form of item
            {
                case 'a': // a - Microfilm

                    formStr.add(FormOfItem.Microfilm.toString());
                    break;

                case 'b': // b - Microfiche

                    formStr.add(FormOfItem.Microfiche.toString());
                    break;

                case 'c': // c - Microopaque

                    formStr.add(FormOfItem.Microopaque.toString());
                    break;

                case 'd': // d - Large print

                    formStr.add(FormOfItem.PrintLarge.toString());
                    break;

                case 'f': // f - Braille

                    formStr.add(FormOfItem.Braille.toString());
                    break;

                case 'o': // o - Online

                    formStr.add(FormOfItem.Online.toString());
                    break;

                case 'q': // q - Direct electronic

                    formStr.add(FormOfItem.ElectronicDirect.toString());
                    break;

                case 's': // s - Electronic

                    formStr.add(FormOfItem.Electronic.toString());
                    break;

                case 'r': // r - Regular print reproduction

                    formStr.add(FormOfItem.Print.toString());
                    break;
            }
        }

        return formStr;
    }

    private void addPossibleForm(LinkedHashMap<MediaType, MediaTypeHeuristic> possibleForms, MediaType key, MediaTypeHeuristic value)
    {
        if (possibleForms.containsKey(key))
        {
            MediaTypeHeuristic oldVal = possibleForms.get(key);
            oldVal.combine(value);
            possibleForms.put(key, oldVal);
        }
        else
        {
            possibleForms.put(key, value);
        }
    }
    
    private MediaTypeHeuristic getMediaTypeHeuristically(Record record, ContentType leaderType)
    {
        LinkedHashMap<MediaType, MediaTypeHeuristic> possibleForms = new LinkedHashMap<MediaType, MediaTypeHeuristic>(); 
        List<VariableField> notes = (List<VariableField>)record.getVariableFields(new String[] {"538"});
        for (VariableField notev : notes)
        {
            DataField note = (DataField)notev;
            if (note.getSubfield('a') == null) continue;
            String noteData = note.getSubfield('a').getData();
            if (noteData.matches(".*Blu[e]?-[Rr]ay.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoBluRay, new MediaTypeHeuristic(MediaType.VideoBluRay,  0.8, note.getTag()));
            }
            else if (noteData.contains("DVD") && !noteData.matches(".*[Aa]lso.*DVD.*") && !noteData.matches(".*DVD-ROM.*") && !noteData.matches(".*DVD [Dd]rive.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoDVD, new MediaTypeHeuristic(MediaType.VideoDVD,  0.8, note.getTag()));
            }
            if (noteData.matches(".*Laser[ ]?[Dd]isc.*") ||
                    noteData.matches(".*\\bCLV\\b.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoLaserdisc, new MediaTypeHeuristic(MediaType.VideoLaserdisc,  0.8, note.getTag()));
            }
            if (noteData.matches(".*[Cc]ompact [Dd]isc.*") )
            {
                addPossibleForm( possibleForms, MediaType.SoundDiscCD, new MediaTypeHeuristic(MediaType.SoundDiscCD,  0.8, note.getTag()));
            }
            if (noteData.matches(".*[Vv]ideodisc.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoLaserdisc, new MediaTypeHeuristic(MediaType.VideoLaserdisc,  0.6, note.getTag()));
                addPossibleForm( possibleForms, MediaType.VideoDVD, new MediaTypeHeuristic(MediaType.VideoDVD,  0.6, note.getTag()));
            }
            if (noteData.contains("VHS") && !noteData.matches(".*[Aa]lso.*VHS.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoVHS, new MediaTypeHeuristic(MediaType.VideoVHS,  0.7, note.getTag()));
            }
            else if (leaderType == ContentType.ProjectedMedium && noteData.matches(".*Beta\\b SP.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoBetacamSP, new MediaTypeHeuristic(MediaType.VideoBetacamSP,  0.7, note.getTag()));
            }
            else if (leaderType == ContentType.ProjectedMedium && noteData.matches(".*Beta\\b.*") && !noteData.matches(".*[Aa]lso.*Beta\\b.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoBeta, new MediaTypeHeuristic(MediaType.VideoBeta,  0.7, note.getTag()));
            }
        }
        Set<String> forms = SolrIndexer.getAllSubfields(record, "300[abc]", "--");
        for (String form : forms)
        {
            if (form.matches(".*[Vv]ideo[ ]?disc.*--.*--.*12 cm.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoLaserdisc, new MediaTypeHeuristic(MediaType.VideoLaserdisc,  0.1, "300"));
                addPossibleForm( possibleForms, MediaType.VideoDVD, new MediaTypeHeuristic(MediaType.VideoDVD,  0.8, "300"));
            }
            else if (form.matches(".*[Vv]ideo[ ]?disc.*--.*--.*12.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoLaserdisc, new MediaTypeHeuristic(MediaType.VideoLaserdisc,  0.8, "300"));
                addPossibleForm( possibleForms, MediaType.VideoDVD, new MediaTypeHeuristic(MediaType.VideoDVD,  0.1, "300"));
            }
            else if (form.matches(".*[Vv]ideo[ ]?disc.*--.*--.*4 3/4.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoLaserdisc, new MediaTypeHeuristic(MediaType.VideoLaserdisc,  0.1, "300"));
                addPossibleForm( possibleForms, MediaType.VideoDVD, new MediaTypeHeuristic(MediaType.VideoDVD,  0.7, "300"));
                addPossibleForm( possibleForms, MediaType.VideoBluRay, new MediaTypeHeuristic(MediaType.VideoBluRay,  0.7, "300"));
            }
            else if (form.matches(".*[Vv]ideo[ ]?cassette.*--.*--.*[1\u00B9]/[2\u2082].*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoVHS, new MediaTypeHeuristic(MediaType.VideoVHS,  0.7, "300"));
                addPossibleForm( possibleForms, MediaType.VideoBeta, new MediaTypeHeuristic(MediaType.VideoBeta,  0.55, "300"));
            }
            else if (leaderType == ContentType.ProjectedMedium && form.matches(".*cassette.*--.*--.*[Uu][-]?[Mm]atic.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoUMatic, new MediaTypeHeuristic(MediaType.VideoUMatic,  0.75, "300"));
                addPossibleForm( possibleForms, MediaType.VideoBeta, new MediaTypeHeuristic(MediaType.VideoBeta,  0.3, "300"));
                addPossibleForm( possibleForms, MediaType.VideoVHS, new MediaTypeHeuristic(MediaType.VideoVHS,  0.4, "300"));
            }
            else if (form.matches(".*[Vv]ideo[ ]?cassette.*--.*--.*3/4.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoUMatic, new MediaTypeHeuristic(MediaType.VideoUMatic,  0.7, "300"));
                addPossibleForm( possibleForms, MediaType.VideoBeta, new MediaTypeHeuristic(MediaType.VideoBeta,  0.3, "300"));
                addPossibleForm( possibleForms, MediaType.VideoVHS, new MediaTypeHeuristic(MediaType.VideoVHS,  0.4, "300"));
            }
            else if (leaderType == ContentType.ProjectedMedium && form.matches(".*cassette.*--.*--.*3/4.*"))
            {
                addPossibleForm( possibleForms, MediaType.VideoUMatic, new MediaTypeHeuristic(MediaType.VideoUMatic,  0.65, "300"));
                addPossibleForm( possibleForms, MediaType.VideoBeta, new MediaTypeHeuristic(MediaType.VideoBeta,  0.3, "300"));
                addPossibleForm( possibleForms, MediaType.VideoVHS, new MediaTypeHeuristic(MediaType.VideoVHS,  0.4, "300"));
            }
            else if (form.matches(".*[Ss]ound [Dd]is[ck].*--.*--.*4 3/4.*"))
            {
                addPossibleForm( possibleForms, MediaType.SoundDiscCD, new MediaTypeHeuristic(MediaType.SoundDiscCD,  0.75, "300"));
            }
            else if (form.matches(".*[Ss]ound [Dd]is[ck].*--.*33 1/3.*--.*12.*"))
            {
                addPossibleForm( possibleForms, MediaType.SoundDiscLP, new MediaTypeHeuristic(MediaType.SoundDiscLP,  0.75, "300"));
            }
            else if ((leaderType == ContentType.SoundRecording || leaderType == ContentType.MusicRecording)
                    && form.matches(".*[Dd]is[ck].*--.*33 1/3.*--.*12.*"))
            {
                addPossibleForm( possibleForms, MediaType.SoundDiscLP, new MediaTypeHeuristic(MediaType.SoundDiscLP,  0.75, "300"));
            }
            else if (form.matches(".*[Ss]ound [Tt]ape [Rr]eel.*"))
            {
                addPossibleForm( possibleForms, MediaType.SoundTapeReel, new MediaTypeHeuristic(MediaType.SoundTapeReel,  0.75, "300"));
            }
            else if (form.matches(".*[Ss]ound [Cc]assette.*"))
            {
                addPossibleForm( possibleForms, MediaType.SoundCassette, new MediaTypeHeuristic(MediaType.SoundCassette,  0.75, "300"));
            }
            else if (leaderType == ContentType.Book && (form.matches(".*broadside.*--.*") || form.matches(".*sheet\\b.*--.*")))
            {
                addPossibleForm( possibleForms, MediaType.Broadside, new MediaTypeHeuristic(MediaType.Broadside,  0.75, "300"));
            }
        }

        double maxPriority = 0.0;
        MediaTypeHeuristic maxMth = null;
        for (MediaType mt : possibleForms.keySet())
        {
            MediaTypeHeuristic mth = possibleForms.get(mt);
            if (mth.priority > maxPriority) 
            {
                maxMth = mth;
                maxPriority = mth.priority;
            }
        }
        return(maxMth);
    }

    private String validate007Field(Record record, ProfileType profileType, ContentType leaderType, ControlField field007)
    {
        char field007_02 = '?';
        if (field007.getData().matches(".*[bde][a-z][^a-z]{1,2}[cdef][a-z][^a-z]{1,2}[defgh][a-z].*"))
        {
            // catch the really wackadoodle 007 fields like this:   v|bd|dc|ev|fa|gi|hz|iu
            // and fix them (in this case the answer should be: vd cvaizu
            boolean showError = false;
            if (indexer != null && indexer.errors != null && (field007.getId() == null || (field007.getId() & (long)1) == (long)0))
            {
                /// set id on field to prevent multiple error messages for the same error
                field007.setId(field007.getId() == null ? (long)1 : field007.getId() | (long)1);
                showError = true;
            }
            String subfields[] = field007.getData().split("[^a-z]{1,2}");
            char[] new007Val = "                                        ".toCharArray();
            if (subfields[0].length() == 0) 
            {
                String newsf[] = Arrays.copyOfRange(subfields, 1, subfields.length);
                subfields = newsf;
            }
            for (int i = 0; i < subfields.length; i++)
            {
                if (i == 0 && (subfields[i].length() == 1 || subfields[i].charAt(0) != 'a')) 
                {
                    new007Val[i] = subfields[i].charAt(0);
                }
                else if (subfields[i].length() > 1 )
                {
                    int offset = (int)(subfields[i].charAt(0) - 'a');
                    if (new007Val[0] == 'h' && offset > 5) offset += 3;
                    new007Val[offset] = subfields[i].charAt(1);
                }
            }
            new007Val[2] = ' ';  // make sure character 2 of new field is blank
            String newValue = new String(new007Val);
            newValue = newValue.trim();
            if (showError) addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "totally whackadoodle 007 field found \"Its got subfields\" changing it to \'"+ newValue+ "\'");
            return(newValue);
        }
        else if (field007.getData().length() <= 2 || 
            (field007_02 = field007.getData().toLowerCase().charAt(2)) != ' ' && field007_02 != '|' && field007_02 != '-' && field007_02 != '*')
        { 
            {
                boolean showError = false;
                if (indexer != null)
                {
                    /// set id on field to prevent multiple error messages for the same error
                    showError = true;
                }
                if (profileType == profileType.Visual && leaderType == ContentType.ProjectedMedium && 
                    ((field007.getData().length() % 6) == 0 || field007.getData().replaceFirst("-*$", "").length() == 6))
                {
                    String newValue = field007.getData().replaceFirst("([a-z])([-a-z][-a-z][-a-z][-a-z][-a-z]).*", "v$1 $2");
                    if (showError) addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "Old 007 visual material fixed field (pre-1981) mapping it from "+field007+ " to "+ newValue);
                    return(newValue);
                }
                else if (profileType == profileType.Music && (field007.getData().matches("^sl..j.*") || field007.getData().matches("^d[abcd].[ms][cde].*") || field007.getData().matches("^de.g.*")) )
                {
                    String newValue = field007.getData().replaceFirst("([a-z])([-a-z][-a-z][-a-z][-a-z].*)", "s$1 $2");
                    if (showError) addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "Old 007 music fixed field (pre 1981)");
                    return(newValue);
                }
                else if ( field007_02 == 'r' || field007_02 == 'o')
                {
                    if (showError) addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.ERROR_TYPO, "Old 007 fixed field (post-1981), character 2 is '"+field007_02+"' it should be undefined.");
                }
                else if (field007.getData().length() <= 2)
                {
                    if (showError) addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "Malformed 007 fixed field, field too short");
                    return (field007.getData() + "        ");
                }
                else
                {
                    if (showError) addFormatError(record.getControlNumber(), "007", "n/a", ErrorHandler.MINOR_ERROR, "Malformed 007 fixed field, character 02 should be blank");
                }
            }
        }

        return(field007.getData());
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

    protected ContentType extractType(String field, String source)
    {
        char recordType = ' ';

        if (source.equals("leader"))
        {
            recordType = field.toLowerCase().charAt(6);
        }
        else if (source.equals("006"))
        {
            recordType = field.toLowerCase().charAt(0);
        }
        if (recordType == 'a' && !source.equals("006")) 
        {
            char subKey = field.toLowerCase().charAt(7);
            if (mainSubTypeMap.containsKey(subKey))
            {
                return(mainSubTypeMap.get(subKey));
            }
        }
        if (mainTypeMap.containsKey(recordType))  // not else if on purpose
        {
            return(mainTypeMap.get(recordType));
        }
        return ContentType.NoneDefined; 
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

    protected ProfileType extractProfile(String field, String source)
    {
        char recordType = ' ';

        if (source.equals("leader"))
        {
            recordType = field.toLowerCase().charAt(6);
        }
        else if (source.equals("006"))
        {
            recordType = field.toLowerCase().charAt(0);
        }
        if (recordType == 'a' && !source.equals("006")) 
        {
            char subKey = field.toLowerCase().charAt(7);
            if (mainSubTypeMap.containsKey(subKey))
            {
                return(mainSubProfileMap.get(subKey));
            }
        }
        if (mainTypeMap.containsKey(recordType))  // not else if on purpose
        {
            return(mainProfileMap.get(recordType));
        }
        return ProfileType.NoneDefined; 
    }

    /**
     * Whether the record contains a full-text link
     * 
     * @param record MARC Record
     * @return       <code>true</code> if record contains a full-text link
     */

    public Boolean hasFullText(final Record record)
    {
        Set<String> urls = indexer.getFullTextUrls(record);

        return(urls.size() != 0 ? true : false);
    }

    /**
     * Whether the record contains a full-text link
     * 
     * @param record MARC Record
     * @return       <code>true</code> if record contains a full-text link
     */

    public Boolean hasSupplText(final Record record)
    {
        Set<String> urls = indexer.getSupplUrls(record);

        return(urls.size() != 0 ? true : false);
    }
    
    /**
     * Shift (or add) element to the front of our list
     *
     * @param formats list
     * @param add     item to add
     * @return        new <code>Set</code> with item <code>add</code> at the front
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

