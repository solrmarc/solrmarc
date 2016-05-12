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
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.marc.Record;
import org.solrmarc.debug.CompoundUndoManager.RedoAction;
import org.solrmarc.debug.CompoundUndoManager.UndoAction;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    //undo helpers
    protected Action undoAction;
    protected Action redoAction;
    protected CompoundUndoManager undo = null;
    HashMap<Object, Action> actions;

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
       // configPane.getDocument().
        undo = new CompoundUndoManager(configPane);
        configPane.getDocument().addUndoableEditListener(undo);
        
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

/*    protected class MyCompoundEdit extends CompoundEdit
    {
        boolean isUnDone = false;

        public int getLength()
        {
            return edits.size();
        }

        public void undo() throws CannotUndoException
        {
            super.undo();
            isUnDone = true;
        }

        public void redo() throws CannotUndoException
        {
            super.redo();
            isUnDone = false;
        }

        public boolean canUndo()
        {
            return edits.size() > 0 && !isUnDone;
        }

        public boolean canRedo()
        {
            return edits.size() > 0 && isUnDone;
        }

    }

    //This one listens for edits that can be undone.
    protected class MyUndoableEditListener implements UndoableEditListener
    {
        String lastEditName = null;
        ArrayList<MyCompoundEdit> edits = new ArrayList<MyCompoundEdit>();
        MyCompoundEdit current;
        int pointer = -1;

        public void undoableEditHappened(UndoableEditEvent e)
        {
            UndoableEdit edit = e.getEdit();
            if (edit instanceof AbstractDocument.DefaultDocumentEvent)
            {
                try
                {
                    AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
                    int start = event.getOffset();
                    int len = event.getLength();
                    String text = event.getDocument().getText(start, len);
                    boolean isNeedStart = false;
                    if (current == null)
                    {
                        isNeedStart = true;
                    }
                    else if (text.contains("\n"))
                    {
                        isNeedStart = true;
                    }
                    else if (lastEditName == null || !lastEditName.equals(edit.getPresentationName()))
                    {
                        isNeedStart = true;
                    }

                    while (pointer < edits.size() - 1)
                    {
                        edits.remove(edits.size() - 1);
                        isNeedStart = true;
                    }
                    if (isNeedStart)
                    {
                        createCompoundEdit();
                    }

                    current.addEdit(edit);
                    lastEditName = edit.getPresentationName();

                    refreshControls();
                }
                catch (BadLocationException e1)
                {
                    e1.printStackTrace();
                }
            }
        }

        public void discardAllEdits()
        {
            edits = new ArrayList<MyCompoundEdit>();
            current = null;
            pointer = -1;
            refreshControls();
        }

        public void createCompoundEdit()
        {
            if (current == null)
            {
                current = new MyCompoundEdit();
            }
            else if (current.getLength() > 0)
            {
                current = new MyCompoundEdit();
            }

            edits.add(current);
            pointer++;
        }

        public void undo() throws CannotUndoException
        {
            if (!canUndo())
            {
                throw new CannotUndoException();
            }

            MyCompoundEdit u = edits.get(pointer);
            u.undo();
            pointer--;

            refreshControls();
        }

        public void redo() throws CannotUndoException
        {
            if (!canRedo())
            {
                throw new CannotUndoException();
            }

            pointer++;
            MyCompoundEdit u = edits.get(pointer);
            u.redo();

            refreshControls();
        }

        public boolean canUndo()
        {
            return pointer >= 0;
        }

        public boolean canRedo()
        {
            return edits.size() > 0 && pointer < edits.size() - 1;
        }

        public void refreshControls()
        {
            undoAction.setEnabled(canUndo());
            redoAction.setEnabled(canRedo());
        }

        public String getUndoPresentationName()
        {
            if (current == null) return(null);
            return current.getUndoPresentationName();
        }

        public Object getRedoPresentationName()
        {
            if (current == null) return(null);
            return current.getRedoPresentationName();
        }
    }

    class UndoAction extends AbstractAction
    {
        public UndoAction()
        {
            super("Undo");
            setEnabled(false);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undo.undo();
            }
            catch (CannotUndoException ex)
            {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState()
        {
            if (undo.canUndo())
            {
                setEnabled(true);
 //               putValue(Action.NAME, undo.getUndoPresentationName());
            }
            else
            {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
 //               putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }
*/
    // private class SwingAction extends AbstractAction {
    // public SwingAction() {
    // putValue(NAME, "SwingAction");
    // putValue(SHORT_DESCRIPTION, "Some short description");
    // }
    // public void actionPerformed(ActionEvent e) {
    // }
    // }
}
