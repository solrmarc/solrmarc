package org.solrmarc.debug;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.miginfocom.swing.MigLayout;
import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.index.indexer.IndexerSpecException;
import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.index.specification.Specification;
import playground.solrmarc.index.specification.conditional.ConditionalParser;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.marc.Record;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SolrMarcDebug
{

    private JFrame frmSolrmarcIndexSpecification;
    // private final Action action = new SwingAction();
    private Map<String, Record> recordMap;
    private JTextPane configPane;
    private JTextPane outputPane;
    private JTextPane errorPane;
    private JTextPane recordPane;
    ValueIndexerFactory indexerFactory = null;
    JComboBox<String> marcIdentifier = null;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    SolrMarcDebug window = new SolrMarcDebug();
                    window.frmSolrmarcIndexSpecification.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    static ConditionalParser parser = null;
    static boolean do_debug_parse = true;

    public static Specification buildSpecificationFromString(String conditional)
    {
        if (parser == null) parser = new ConditionalParser(do_debug_parse);
        Specification result = null;
        result = parser.parse(conditional, do_debug_parse);
        result.setSpecLabel(conditional);
        return (result);
    }

    /**
     * Create the application.
     */
    public SolrMarcDebug()
    {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        indexerFactory = ValueIndexerFactory.instance();

        recordMap = new LinkedHashMap<String, Record>();

        frmSolrmarcIndexSpecification = new JFrame();
        frmSolrmarcIndexSpecification.setTitle("SolrMarc Index Specification Debugger");
        frmSolrmarcIndexSpecification.setBounds(100, 100, 1024, 828);
        frmSolrmarcIndexSpecification.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frmSolrmarcIndexSpecification.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmOpenConfig = new JMenuItem("Open Config...");
        mnNewMenu.add(mntmOpenConfig);

        JMenuItem mntmOpenMarcRecord = new JMenuItem("Open Marc Record ...");
        mnNewMenu.add(mntmOpenMarcRecord);

        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);

        JMenuItem mntmCut = new JMenuItem("Cut");
        mnEdit.add(mntmCut);

        JMenuItem mntmCopy = new JMenuItem("Copy");
        mnEdit.add(mntmCopy);

        JMenuItem mntmPaste = new JMenuItem("Paste");
        mnEdit.add(mntmPaste);
        frmSolrmarcIndexSpecification.getContentPane().setLayout(
                new MigLayout("", "[512px,grow][512px,grow]", "[361.00px,grow][::35.00px][141.00px,grow][grow]"));

        JScrollPane scrollPane = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane, "cell 0 0,grow");

        recordPane = new JTextPane();
        recordPane.setEditable(false);
        scrollPane.setViewportView(recordPane);

        JPanel panel_1 = new JPanel();
        frmSolrmarcIndexSpecification.getContentPane().add(panel_1, "cell 0 1 2 1,grow");
        panel_1.setLayout(new MigLayout("", "[grow][][]", "[][grow][]"));

        marcIdentifier = new JComboBox<String>();

        JButton btnPrevRecord = new JButton("< Prev");
        btnPrevRecord.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = marcIdentifier.getSelectedIndex();
                if (index > 0) marcIdentifier.setSelectedIndex(index - 1);
            }
        });
        panel_1.add(btnPrevRecord, "flowx,cell 0 0,alignx left");
        btnPrevRecord.setMnemonic('<');

        panel_1.add(marcIdentifier, "flowx,cell 0 0,grow");

        JButton btnNextRecord = new JButton("Next >");
        btnNextRecord.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = marcIdentifier.getSelectedIndex();
                int cnt = marcIdentifier.getItemCount();
                if (index >= 0 && index < cnt - 1) marcIdentifier.setSelectedIndex(index + 1);
            }
        });

        JButton btnApply = new JButton("Apply");
        btnApply.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = marcIdentifier.getSelectedIndex();
                marcIdentifier.setSelectedIndex(index);
            }
        });
        panel_1.add(btnApply, "cell 1 0");
        panel_1.add(btnNextRecord, "cell 2 0");
        btnNextRecord.setMnemonic('>');

        JScrollPane scrollPane_1 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_1, "cell 0 2 2 1,grow");

        configPane = new JTextPane();
        scrollPane_1.setViewportView(configPane);

        JScrollPane scrollPane_2 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_2, "cell 1 0,grow");

        outputPane = new JTextPane();
        outputPane.setEditable(false);
        scrollPane_2.setViewportView(outputPane);

        JScrollPane scrollPane_3 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_3, "cell 0 3 2 1,grow");

        errorPane = new JTextPane();
        errorPane.setEditable(false);
        scrollPane_3.setViewportView(errorPane);

        marcIdentifier.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                @SuppressWarnings("unchecked")
                String fKey = ((JComboBox<String>) e.getSource()).getSelectedItem().toString();
                Record rec = recordMap.get(fKey);
                recordPane.setText(rec.toString());
                recordPane.setCaretPosition(0);
                // String fieldNameStr = fieldName.getText();
                processRecordToOutput(rec);
            }

        });

        mntmOpenConfig.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                File f = null; // new File("resources/testSpec.properties");
                JFileChooser chooser = new JFileChooser("resources");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Index Property Files", "properties");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(frmSolrmarcIndexSpecification);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    f = chooser.getSelectedFile();
                }
                else
                {
                    return;
                }

                FileReader reader = null;
                try
                {
                    configPane.read(new FileReader(f), null);
                }
                catch (FileNotFoundException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                finally
                {
                    if (reader != null)
                    {
                        try
                        {
                            reader.close();
                        }
                        catch (IOException e1)
                        {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        mntmOpenMarcRecord.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // File f = new File("resources/specTestRecs.mrc");
                File f = null; // new File("resources/testSpec.properties");
                JFileChooser chooser = new JFileChooser("resources");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("MARC Record Files", "mrc", "xml");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(frmSolrmarcIndexSpecification);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    f = chooser.getSelectedFile();
                }
                else
                {
                    return;
                }

                MarcPermissiveStreamReader reader;
                String firstId = null;
                try
                {
                    reader = new MarcPermissiveStreamReader(new FileInputStream(f), true, true, "BESTGUESS");
                    while (reader.hasNext())
                    {
                        Record record = reader.next();
                        String id = record.getControlNumber();
                        if (firstId == null) firstId = id;
                        if (!recordMap.containsKey(id))
                        {
                            recordMap.put(id, record);
                            marcIdentifier.addItem(id);
                        }
                        else
                        {
                            recordMap.put(id, record);
                        }
                    }
                    marcIdentifier.setSelectedItem(firstId);
                }
                catch (FileNotFoundException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        // marcFileName.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        // File f = (File)(((JComboBox<File>)e.getSource()).getSelectedItem());
        // }
        // });

    }

    // private Collection<String> processRecord(Record rec)
    // {
    // String solrFieldName = fieldName.getText();
    // String fieldSpecStr = fieldSpec.getText();
    // String formatSpecStr = formatSpec.getText();
    // if (fieldSpecStr.length() == 0 || formatSpecStr.length() == 0)
    // return(null);
    //
    // MultiValueIndexer indexer =
    // (MultiValueIndexer)indexerFactory.createValueIndexer(solrFieldName,
    // fieldSpecStr);
    // //MultiValueFieldMatchCollector fmc = new
    // MultiValueFieldMatchCollector();
    // Collection<String> result = null;
    // try {
    // result = indexer.getFieldData(rec);
    // }
    // catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //// if (formatSpecStr.contains("unique"))
    //// {
    //// result = new LinkedHashSet<String>();
    //// }
    //// else
    //// {
    //// result = new ArrayList<String>();
    //// }
    //// theSpec.setFormatter(new FieldFormatterJoin(new
    // FieldFormatterBase(true), " -- "));
    //// theSpec.setFormatter(new FieldFormatterPatternMapped(new
    // FieldFormatterBase(true),
    // "(^|.*[^0-9])((20|1[5-9])[0-9][0-9])([^0-9]|$)=>$2||.*[^0-9].*=>"));
    //// Collection<FieldMatch> values = theSpec.getFieldMatches(rec);
    //// for (FieldMatch fm : values)
    //// {
    //// SingleSpecification spec = fm.getSpec();
    //// VariableField vf = fm.getVf();
    //// spec.addFieldValues(result, vf);
    //// }
    // return(result);
    // }

    private void processRecordToOutput(Record rec)
    {
        // String solrFieldName = fieldName.getText();
        // String fieldSpecStr = fieldSpec.getText();
        // String formatSpecStr = formatSpec.getText();
        // if (fieldSpecStr.length() == 0 || formatSpecStr.length() == 0)
        // return(null);

        List<AbstractValueIndexer<?>> indexers = null;
        try
        {
            indexers = indexerFactory.createValueIndexers(configPane.getText().split("\n"));
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<IndexerSpecException> exceptions = indexerFactory.getValidationExceptions();
        errorPane.setText(getTextForExceptions(exceptions));

        // MultiValueIndexer indexer =
        // (MultiValueIndexer)indexerFactory.createValueIndexer(solrFieldName,
        // fieldSpecStr);
        // MultiValueFieldMatchCollector fmc = new
        // MultiValueFieldMatchCollector();
        outputPane.setText("");
        Document doc = outputPane.getDocument();
        for (AbstractValueIndexer<?> indexer : indexers)
        {
            Collection<String> fieldNameList = indexer.getSolrFieldNames();
            Collection<String> results = null;
            try
            {
                results = indexer.getFieldData(rec);
                for (String fieldName : fieldNameList)
                {
                    for (String result : results)
                    {
                        String outLine = fieldName + " : " + result + "\n";
                        try
                        {
                            doc.insertString(doc.getLength(), outLine, null);
                        }
                        catch (BadLocationException exc)
                        {
                            exc.printStackTrace();
                        }

                    }
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // if (formatSpecStr.contains("unique"))
        // {
        // result = new LinkedHashSet<String>();
        // }
        // else
        // {
        // result = new ArrayList<String>();
        // }
        // theSpec.setFormatter(new FieldFormatterJoin(new
        // FieldFormatterBase(true), " -- "));
        // theSpec.setFormatter(new FieldFormatterPatternMapped(new
        // FieldFormatterBase(true),
        // "(^|.*[^0-9])((20|1[5-9])[0-9][0-9])([^0-9]|$)=>$2||.*[^0-9].*=>"));
        // Collection<FieldMatch> values = theSpec.getFieldMatches(rec);
        // for (FieldMatch fm : values)
        // {
        // SingleSpecification spec = fm.getSpec();
        // VariableField vf = fm.getVf();
        // spec.addFieldValues(result, vf);
        // }
    }

    private String getTextForExceptions(List<IndexerSpecException> exceptions)
    {
        StringBuilder text = new StringBuilder();
        for (IndexerSpecException e : exceptions)
        {
            text.append(e.getMessage());
        }
        return (text.toString());
    }

    // private class SwingAction extends AbstractAction {
    // public SwingAction() {
    // putValue(NAME, "SwingAction");
    // putValue(SHORT_DESCRIPTION, "Some short description");
    // }
    // public void actionPerformed(ActionEvent e) {
    // }
    // }
}
