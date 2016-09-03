package org.solrmarc.debug;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.miginfocom.swing.MigLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JPanel;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.marc4j.MarcError;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.driver.BootableMain;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.marc.MarcReaderFactory;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SolrMarcDebug extends BootableMain
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
    //undo helpers
    protected Action undoAction;
    protected Action redoAction;
    protected CompoundUndoManager undo = null;
    HashMap<Object, Action> actions;
    String previousConfigText = "";
    List<AbstractValueIndexer<?>> indexers = null;
    /**
     * Launch the application.
     */
    public static void main(final String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    SolrMarcDebug window = new SolrMarcDebug(args);
                    window.frmSolrmarcIndexSpecification.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

//    static ConditionalParser parser = null;
//    static boolean do_debug_parse = true;

//    public static Specification buildSpecificationFromString(String conditional)
//    {
////        if (parser == null) parser = new ConditionalParser(do_debug_parse);
//        Specification result = null;
//        result = parser.parse(conditional, do_debug_parse);
//        result.setSpecLabel(conditional);
//        return (result);
//    }

    /**
     * Create the application.
     */
    public SolrMarcDebug(String args[])
    {
        super.processArgs(args, false);
        initialize();
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
//        // You must set the HomeDir before instantiating the ValueIndexerFactory
//        // since that directory is used as the location to look for java source files to compile and include
//        // If it is unspecified, the program looks in 
//        ValueIndexerFactory.setHomeDirs(homeDirStrs);

        indexerFactory = ValueIndexerFactory.initialize(homeDirStrs);

        recordMap = new LinkedHashMap<String, Record>();

        frmSolrmarcIndexSpecification = new JFrame();
        frmSolrmarcIndexSpecification.setTitle("SolrMarc Index Specification Debugger");
        frmSolrmarcIndexSpecification.setBounds(100, 100, 1024, 828);
        frmSolrmarcIndexSpecification.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        frmSolrmarcIndexSpecification.getContentPane().setLayout(
                new MigLayout("", "[512px,grow][512px,grow]", "[361.00px,grow][::35.00px][141.00px,grow][grow]"));

        JScrollPane scrollPane = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane, "cell 0 0,grow");

        recordPane = new JTextPane();
        Font currFont = recordPane.getFont();
        recordPane.setFont(new Font("Courier New", currFont.getStyle(), currFont.getSize()));
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
        currFont = configPane.getFont();
        configPane.setFont(new Font("Courier New", currFont.getStyle(), currFont.getSize()));
        scrollPane_1.setViewportView(configPane);
       // configPane.getDocument().
        undo = new CompoundUndoManager(configPane);
        configPane.getDocument().addUndoableEditListener(undo);
        
        JScrollPane scrollPane_2 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_2, "cell 1 0,grow");

        outputPane = new JTextPane();
        currFont = outputPane.getFont();
        outputPane.setFont(new Font("Courier New", currFont.getStyle(), currFont.getSize()));
        outputPane.setEditable(false);
        scrollPane_2.setViewportView(outputPane);

        JScrollPane scrollPane_3 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_3, "cell 0 3 2 1,grow");

        errorPane = new JTextPane();
        currFont = errorPane.getFont();
        errorPane.setFont(new Font("Courier New", currFont.getStyle(), currFont.getSize()));
        errorPane.setEditable(false);
        scrollPane_3.setViewportView(errorPane);

        //Set up the menu bar.
        actions=createActionTable(configPane);
        
        JMenuBar menuBar = new JMenuBar();
        frmSolrmarcIndexSpecification.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmOpenConfig = new JMenuItem("Open Config...");
        mnNewMenu.add(mntmOpenConfig);

        JMenuItem mntmOpenMarcRecord = new JMenuItem("Open Marc Record ...");
        mnNewMenu.add(mntmOpenMarcRecord);

        JMenu mnEdit = createEditMenu();
        menuBar.add(mnEdit);

        
        marcIdentifier.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                @SuppressWarnings("unchecked")
                JComboBox<String> source = ((JComboBox<String>) e.getSource());
                Object selected = source.getSelectedItem();
                if (selected != null)
                {
                    String fKey = selected.toString();
                    Record rec = recordMap.get(fKey);
                    recordPane.setText(rec.toString());
                    recordPane.setCaretPosition(0);
                    // String fieldNameStr = fieldName.getText();
                    processRecordToOutput(rec);
                }
            }

        });

        mntmOpenConfig.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                File f = null; // new File("resources/testSpec.properties");
                JFileChooser chooser = new JFileChooser(homeDirStrs[0] + File.separator + "resources");
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
                    configPane.getDocument().addUndoableEditListener(undo);
                    undo.discardAllEdits();
                    undoAction.setEnabled(false);
                    redoAction.setEnabled(false);
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
                File f1 = new File(homeDirStrs[0],  "resources/marcreader.properties");
                Properties readerProps = new Properties();
                try
                {
                    readerProps.load(new FileInputStream(f1));
                }
                catch (FileNotFoundException e2)
                {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                catch (IOException e2)
                {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                
                // File f = new File("resources/specTestRecs.mrc");
                File f = null; // new File("resources/testSpec.properties");
                JFileChooser chooser = new JFileChooser(homeDirStrs[0] + File.separator + "resources");
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

                MarcReader reader;
                String firstId = null;
                try
                {
                    reader = MarcReaderFactory.instance().makeReader(readerProps, ValueIndexerFactory.instance().getHomeDirs(), new FileInputStream(f));
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

    }
    
    //Create the edit menu.
    protected JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");

        //Undo and redo are actions of our own creation.
        undoAction = undo.getUndoAction();
        menu.add(undoAction);

        redoAction = undo.getRedoAction();
        menu.add(redoAction);

        menu.addSeparator();

        //These actions come from the default editor kit.
        //Get the ones we want and stick them in the menu.
        menu.add(getActionByName(DefaultEditorKit.cutAction, "Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK)));
        menu.add(getActionByName(DefaultEditorKit.copyAction, "Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK)));
        menu.add(getActionByName(DefaultEditorKit.pasteAction, "Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK)));

 //       menu.addSeparator();

 //       menu.add(getActionByName(DefaultEditorKit.selectAllAction));
        return menu;
    }

    //The following two methods allow us to find an
    //action provided by the editor kit by its name.
    private HashMap<Object, Action> createActionTable(JTextComponent textComponent)
    {
        HashMap<Object, Action> actions = new HashMap<Object, Action>();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++)
        {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
        return actions;
    }

    private Action getActionByName(String name, String label, KeyStroke keyStroke)
    {
        Action action = actions.get(name);
        action.putValue(Action.NAME, label);
        if (keyStroke != null) action.putValue(Action.ACCELERATOR_KEY, keyStroke);
        return action;
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
        
        String currentConfigText = configPane.getText();
        if (! currentConfigText.equals(previousConfigText) || indexers == null)
        {
            try
            {
                indexers = indexerFactory.createValueIndexers(currentConfigText.split("\n"));
                previousConfigText = currentConfigText;
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
        }
        List<IndexerSpecException> exceptions = indexerFactory.getValidationExceptions();
        errorPane.setText(getTextForExceptions(exceptions));
        indexerFactory.clearPerRecordErrors();
        outputPane.setText("");
        SimpleAttributeSet attributesErr = new SimpleAttributeSet();
        attributesErr = new SimpleAttributeSet();
        attributesErr.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        attributesErr.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        attributesErr.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED);
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
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                String outLine = "marc_error : " + indexer.getSolrFieldNames().toString() + wrapped.getMessage() + "\n";
                try
                {
                    doc.insertString(doc.getLength(), outLine, attributesErr);
                }
                catch (BadLocationException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            catch (IllegalArgumentException e)
            {
                String outLine = "marc_error : " + indexer.getSolrFieldNames().toString() + e.getMessage() + "\n";
                try
                {
                    doc.insertString(doc.getLength(), outLine, attributesErr);
                }
                catch (BadLocationException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            catch (IndexerSpecException e)
            {
                String outLine = "marc_error : " + indexer.getSolrFieldNames().toString() + e.getMessage() + "\n";
                try
                {
                    doc.insertString(doc.getLength(), outLine, attributesErr);
                }
                catch (BadLocationException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            catch (Exception e)
            {
                String outLine = "marc_error : " + indexer.getSolrFieldNames().toString() + e.getMessage() + "\n";
                try
                {
                    doc.insertString(doc.getLength(), outLine, attributesErr);
                }
                catch (BadLocationException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
//
//            if (rec.hasErrors())
//            {
//                for (MarcError error : rec.getErrors())
//                {
//                    try
//                    {
//                        doc.insertString(doc.getLength(), "marc_error : "+error.toString()+"\n", attributesErr);
//                    }
//                    catch (BadLocationException e)
//                    {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }

        }
        List<IndexerSpecException> perRecordExceptions = indexerFactory.getPerRecordErrors();
        try
        {
            doc.insertString(doc.getLength(), getTextForMarcErrorsAndExceptions(rec, perRecordExceptions), attributesErr);
        }
        catch (BadLocationException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private String getTextForMarcErrorsAndExceptions(Record rec, List<IndexerSpecException> exceptions)
    {
        StringBuilder text = new StringBuilder();
//        String lastSpec = "";
        if (rec.hasErrors())
        {
            for (MarcError err : rec.getErrors())
            {
                text.append("Marc Record Error: ").append(err.toString()).append("\n");
            }
        }
        if (exceptions != null)
        {
            for (IndexerSpecException e : exceptions)
            {
                if (e.getSolrField() == null) e.setSolrFieldAndSpec("marc_error", null);
//                String specMessage = e.getSpecMessage();
//                if (!specMessage.equals(lastSpec))
//                {
//                    text.append(specMessage);
//                }
//                lastSpec = specMessage;
                text.append(e.getMessage()).append("\n");
                for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
                {
                    text.append(e.getSolrField()).append(" : ").append(cause.getMessage()).append("\n");
                }
            }
        }
        return(text.toString());
    }

    private String getTextForExceptions(List<IndexerSpecException> exceptions)
    {
        StringBuilder text = new StringBuilder();
        String lastSpec = "";
        for (IndexerSpecException e : exceptions)
        {
            String specMessage = e.getSpecMessage();
            if (!specMessage.equals(lastSpec))
            {
                text.append(specMessage).append("\n");
            }
            lastSpec = specMessage;
            text.append(e.getMessage()).append("\n");
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
            {
                text.append(e.getSolrField()).append(" : ").append(cause.getMessage()).append("\n");
            }
        }
        return (text.toString());
    }
}
