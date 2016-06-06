package playground.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class StaticMarcTestRecords
{
    public static Record testRecord[] = new Record[1];
    private static MarcFactory factory;

    static {
        factory = MarcFactory.newInstance();
        testRecord[0] = factory.newRecord("00759cam a2200229 a 4500");
        testRecord[0].addVariableField(factory.newControlField("001", "11939876"));
        testRecord[0].addVariableField(factory.newControlField("005", "20041229190604.0"));
        testRecord[0].addVariableField(factory.newControlField("008", "000313s2000    nyu           000 1 eng  "));
        testRecord[0].addVariableField(factory.newDataField("020", ' ', ' ', "a", "0679450041 (acid-free paper)"));
        testRecord[0].addVariableField(factory.newDataField("040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC"));
        testRecord[0].addVariableField(factory.newDataField("100", '1', ' ', "a", "Chabon, Michael."));
        testRecord[0].addVariableField(factory.newDataField("245", '1', '4', "a", "The amazing adventures of Kavalier and Clay :", "b", "a novel /", "c", "Michael Chabon."));
        testRecord[0].addVariableField(factory.newDataField("260", ' ', ' ', "a", "New York :", "b", "Random House,", "c", "c2000."));
        testRecord[0].addVariableField(factory.newDataField("300", ' ', ' ', "a", "639 p. ;", "c", "25 cm."));
        testRecord[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Comic books, strips, etc.", "x", "Authorship", "v", "Fiction."));
        testRecord[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Heroes in mass media", "v", "Fiction."));
        testRecord[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Czech Americans", "v", "Fiction."));
        testRecord[0].addVariableField(factory.newDataField("651", ' ', '0', "a", "New York (N.Y.)", "v", "Fiction."));
        testRecord[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Young men", "v", "Fiction."));
        testRecord[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Cartoonists", "v", "Fiction."));
        testRecord[0].addVariableField(factory.newDataField("655", ' ', '7', "a", "Humorous stories.", "2", "gsafd"));
        testRecord[0].addVariableField(factory.newDataField("655", ' ', '7', "a", "Bildungsromane.", "2", "gsafd"));
    }
}
