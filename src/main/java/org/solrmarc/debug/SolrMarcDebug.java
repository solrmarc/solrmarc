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
import org.marc4j.MarcReaderConfig;
import org.marc4j.MarcReaderFactory;
import org.marc4j.marc.Record;
import org.solrmarc.driver.BootableMain;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
//import org.solrmarc.marc.MarcReaderFactory;
import org.solrmarc.tools.PropertyUtils;
import org.solrmarc.tools.SolrMarcIndexerException;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JSeparator;
import java.awt.event.InputEvent;

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
    Properties readerProps = new Properties();
    MarcReaderConfig readerConfig;
    static int[] fontSizeArray = { 8, 10, 12, 14, 18, 22, 28, 36, 42 };
    /**
     * Launch the application.
     *
     * @param args command line arguments
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

    /**
     * Create the application.
     *
     * @param args command line arguments
     */
    public SolrMarcDebug(String args[])
    {
        super.processArgs(args, false);
        initialize();
    }

    @Override
    protected boolean needsSolrJ()
    {
        return false;
    }


    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        indexerFactory = ValueIndexerFactory.initialize(homeDirStrs);
        System.setProperty("org.solrmarc.indexer.test.fire.method", "true");
        String inputSource[] = new String[1];
        String propertyFileAsURLStr = PropertyUtils.getPropertyFileAbsoluteURL(homeDirStrs, options.valueOf(readOpts), true, inputSource);
        try
        {
            readerProps.load(PropertyUtils.getPropertyFileInputStream(propertyFileAsURLStr));
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
        try {
            readerConfig = new MarcReaderConfig(readerProps);
        }
        catch (NoClassDefFoundError cnf)
        {
            readerConfig = null;
        }

        recordMap = new LinkedHashMap<String, Record>();

        frmSolrmarcIndexSpecification = new JFrame();
        frmSolrmarcIndexSpecification.setTitle("SolrMarc Index Specification Debugger");
        frmSolrmarcIndexSpecification.setBounds(100, 100, 1024, 828);
        frmSolrmarcIndexSpecification.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        frmSolrmarcIndexSpecification.getContentPane().setLayout(
                new MigLayout("", "[512px,grow][][512px,grow]", "[42.00][361.00px,grow][::-2.00px][141.00px,grow][][100.00px,grow]"));

                JPanel panel_1 = new JPanel();
                frmSolrmarcIndexSpecification.getContentPane().add(panel_1, "cell 0 0 3 1,grow");
                panel_1.setLayout(new MigLayout("", "[grow][][][]", "[][grow][]"));

                marcIdentifier = new JComboBox<String>();

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
                panel_1.add(btnPrevRecord, "cell 2 0,alignx left");
                btnPrevRecord.setMnemonic('<');
                panel_1.add(btnNextRecord, "cell 3 0");
                btnNextRecord.setMnemonic('>');


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

        JScrollPane scrollPane = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane, "cell 0 1,grow");

        recordPane = new JTextPane();
        recordPane.setEditable(false);
        scrollPane.setViewportView(recordPane);

        JScrollPane scrollPane_1 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_1, "cell 0 3 3 1,grow");

        configPane = new JTextPane();
        scrollPane_1.setViewportView(configPane);
       // configPane.getDocument().
        undo = new CompoundUndoManager(configPane);
        configPane.getDocument().addUndoableEditListener(undo);

        JScrollPane scrollPane_2 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_2, "cell 2 1,grow");

        outputPane = new JTextPane();
        outputPane.setEditable(false);
        scrollPane_2.setViewportView(outputPane);

        JSeparator separator_1 = new JSeparator();
        frmSolrmarcIndexSpecification.getContentPane().add(separator_1, "cell 0 4 3 1");

        JScrollPane scrollPane_3 = new JScrollPane();
        frmSolrmarcIndexSpecification.getContentPane().add(scrollPane_3, "cell 0 5 3 1,grow");

        errorPane = new JTextPane();
        errorPane.setEditable(false);
        scrollPane_3.setViewportView(errorPane);

        setFontSize(getCurFontSize());

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

        JMenu mnViewMenu = new JMenu("View");
        menuBar.add(mnViewMenu);

        JMenuItem mntmFontPlus = new JMenuItem("Increase Fontsize");
        mntmFontPlus.setEnabled(true);
        mntmFontPlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK));
        mnViewMenu.add(mntmFontPlus);
        mntmFontPlus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                increaseFontSize();
            }
        });

        JMenuItem mntmFontMinus = new JMenuItem("Decrease Fontsize");
        mntmFontMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
        mnViewMenu.add(mntmFontMinus);
        mntmFontMinus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                decreaseFontSize();
            }
        });

        mntmOpenConfig.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                File f = null; // new File("resources/testSpec.properties");
                JFileChooser chooser = new JFileChooser(homeDirStrs[0]);
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
                openSpecifiedConfig(f, true);
            }
        });

        mntmOpenMarcRecord.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                // File f = new File("resources/specTestRecs.mrc");
                File f = null; // new File("resources/testSpec.properties");
                JFileChooser chooser = new JFileChooser(homeDirStrs[0]);
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
                openSpecifiedMarcFile(f, true);
            }
        });

        // read command line arguments to initialize windows 
        String specs = options.valueOf(configSpecs);
        if (specs != null)  configureIndexer(specs);

        List<String> inputFiles = options.valuesOf(files);
        boolean first = true;
        for (String inputFile : inputFiles)
        {
            File marcFile = new File(inputFile);
            openSpecifiedMarcFile(marcFile, first);
            first = false;
        }
    }

    public void configureIndexer(String indexSpecifications)
    {
        String[] indexSpecs = indexSpecifications.split("[ ]*,[ ]*");
        File[] specFiles = new File[indexSpecs.length];
        int i = 0;
        // currently only reads the first one specified!
        for (String indexSpec : indexSpecs)
        {
            File specFile = new File(indexSpec);
            if (!specFile.isAbsolute()) specFile = PropertyUtils.findFirstExistingFile(homeDirStrs, indexSpec);
            specFiles[i++] = specFile;
            break;   // if there is more than one, ignore the rest!
        }

        if (specFiles.length > 0 && specFiles[0].exists() && specFiles[0].canRead())
            openSpecifiedConfig(specFiles[0], true);
    }

    private int getCurFontSize()
    {
        Font currFont = recordPane.getFont();
        int fontSize = currFont.getSize();
        return(fontSize);
    }

    private void increaseFontSize()
    {
        int fontSize = getCurFontSize();
        int fontSizeIndex = getFontSizeIndex(fontSizeArray, fontSize);
        if (fontSizeIndex < fontSizeArray.length-2) 
        {
            fontSizeIndex++; 
            setFontSize(fontSizeArray[fontSizeIndex]);
        }
    }

    private void decreaseFontSize()
    {
        int fontSize = getCurFontSize();
        int fontSizeIndex = getFontSizeIndex(fontSizeArray, fontSize);
        if (fontSizeIndex > 0) 
        {
            fontSizeIndex--; 
            setFontSize(fontSizeArray[fontSizeIndex]);
        }
    }

    private int getFontSizeIndex(int[] fontSizeArray, int fontSize)
    {
        for (int i = 0; i < fontSizeArray.length; i++)
        {
            if (fontSize >= fontSizeArray[i] && (i < fontSizeArray.length-1 ? fontSize < fontSizeArray[i+1] : true))
                return(i);
        }
        return(1);
    }

    private void setFontSize(int fontSize)
    {

        Font currFont = recordPane.getFont();
        recordPane.setFont(new Font("Courier New", currFont.getStyle(), fontSize));
        currFont = configPane.getFont();
        configPane.setFont(new Font("Courier New", currFont.getStyle(), fontSize));
        currFont = outputPane.getFont();
        outputPane.setFont(new Font("Courier New", currFont.getStyle(), fontSize));
        currFont = errorPane.getFont();
        errorPane.setFont(new Font("Courier New", currFont.getStyle(), fontSize));
    }

    private void openSpecifiedConfig(File f, boolean clear)
    {
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

    private void openSpecifiedMarcFile(File marcFile, boolean pointToFirst)
    {
        MarcReader reader;
        String firstId = null;
        try
        {
            reader = MarcReaderFactory.makeReader(readerConfig, new FileInputStream(marcFile));

            while (reader.hasNext())
            {
                Record record = reader.next();
                String id = record.getControlNumber();
                if (pointToFirst && firstId == null) firstId = id;
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
            if (pointToFirst) marcIdentifier.setSelectedItem(firstId);
        }
        catch (FileNotFoundException fnfe)
        {
            errorPane.setText("Error: Cannot find the specified file: "+ marcFile.getAbsolutePath());
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
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


    private void processRecordToOutput(Record rec)
    {
        String currentConfigText = configPane.getText();
        if (! currentConfigText.equals(previousConfigText) || indexers == null)
        {
            try
            {
                currentConfigText = currentConfigText.replaceAll(",[ \t]*(\r)?\n[ \t]+", ",");
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
        Map<String, List<String>> hasValueForField = new LinkedHashMap<String, List<String>>();
        for (AbstractValueIndexer<?> indexer : indexers)
        {
            Collection<String> fieldNameList = indexer.getSolrFieldNames();
            Collection<String> results = null;
            try
            {
                if (indexer.getOnlyIfEmpty())
                {
                    if (indexer.getSolrFieldNames().size() == 1 && hasValueForField.containsKey(indexer.getSolrFieldNames().iterator().next())) 
                        continue;
                }
                results = indexer.getFieldData(rec);
                for (String fieldName : fieldNameList)
                {
                    for (String result : results)
                    {
                        String outLine = fieldName + " : " + result + "\n";
                        if (indexer.getOnlyIfEmpty() && hasValueForField.containsKey(fieldName)) 
                            continue;
                        List<String> resultList; 
                        if (hasValueForField.containsKey(fieldName)) 
                            resultList = hasValueForField.get(fieldName);
                        else
                            resultList = new ArrayList<String>();
                        if (indexer.getOnlyIfUnique())
                        {
                            if (resultList.contains(result)) continue;
                        }
                        resultList.add(result);
                        hasValueForField.put(fieldName, resultList);
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
                if (wrapped instanceof SolrMarcIndexerException)
                {
                    SolrMarcIndexerException smie = (SolrMarcIndexerException)wrapped;
                    handleSolrMarcIndexerException(indexer, doc, attributesErr, smie);
                }
                else
                {
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
            catch (SolrMarcIndexerException smie)
            {
                handleSolrMarcIndexerException(indexer, doc, attributesErr, smie);
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
        }
        Collection<IndexerSpecException> perRecordExceptions = indexerFactory.getPerRecordErrors();
        try
        {
            doc.insertString(doc.getLength(), getTextForMarcErrorsAndExceptions(rec, perRecordExceptions), attributesErr);
        }
        catch (BadLocationException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        outputPane.setCaretPosition(0);
    }

    private void handleSolrMarcIndexerException(AbstractValueIndexer<?> indexer, Document doc, SimpleAttributeSet attributesErr, SolrMarcIndexerException smie)
    {
        String outLine = "";
        if (smie.getLevel() == SolrMarcIndexerException.IGNORE)
        {
            outLine = indexer.getSolrFieldNames().toString() + "throws exception  Record would be Ignored \n";
        }
        else if (smie.getLevel() == SolrMarcIndexerException.DELETE)
        {
            outLine = indexer.getSolrFieldNames().toString() + "throws exception  Record would be Deleted \n";
        }
        else if (smie.getLevel() == SolrMarcIndexerException.EXIT)
        {
            outLine = indexer.getSolrFieldNames().toString() + "throws exception  Record would be Terminate Indexing \n";
        }

        try
        {
            doc.insertString(0, outLine, attributesErr);
        }
        catch (BadLocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String getTextForMarcErrorsAndExceptions(Record rec, Collection<IndexerSpecException> exceptions)
    {
        StringBuilder text = new StringBuilder();
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
