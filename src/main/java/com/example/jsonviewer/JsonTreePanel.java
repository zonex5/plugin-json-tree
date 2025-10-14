package com.example.jsonviewer;

import com.example.jsonviewer.actions.CollapseAllAction;
import com.example.jsonviewer.actions.ExpandAllAction;
import com.example.jsonviewer.actions.RefreshAction;
import com.example.jsonviewer.actions.SearchDownAction;
import com.example.jsonviewer.actions.SearchFieldAction;
import com.example.jsonviewer.actions.SearchUpAction;
import com.example.jsonviewer.actions.ToggleWholeWordAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main panel for the JSON tree tool window. All comments are in English.
 */
public class JsonTreePanel {
    private final Project project;
    private final ToolWindow toolWindow;

    private final JPanel root;
    private final JPanel cards;
    private final JPanel treeCard;
    private final JPanel messageCard;
    private final JBLabel messageLabel;

    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final JBTextField searchField;
    private final ActionToolbar toolbar;
    private boolean searchControlsEnabled;
    private boolean wholeWordOnly;

    private final ObjectMapper mapper = new ObjectMapper();

    private List<String> lastSelectedPath = new ArrayList<>();
    private final Disposable disposable = Disposer.newDisposable("JsonTreePanelDisposable");

    public JsonTreePanel(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;

        root = new JBPanel<>(new BorderLayout());
        root.setBorder(JBUI.Borders.empty());

        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new ExpandAllAction(this));
        group.add(new CollapseAllAction(this));
        group.add(new RefreshAction(this));

        searchField = new JBTextField();
        searchField.setColumns(20);
        searchField.getEmptyText().setText("Search...");
        Dimension preferredSearchSize = searchField.getPreferredSize();
        searchField.setMaximumSize(preferredSearchSize);
        searchField.addActionListener(e -> searchNext());
        group.addSeparator();
        group.add(new SearchFieldAction(this));
        group.add(new SearchUpAction(this));
        group.add(new SearchDownAction(this));
        group.add(new ToggleWholeWordAction(this));

        toolbar = ActionManager.getInstance().createActionToolbar("JsonTreeToolbar", group, true);
        toolbar.setTargetComponent(root);
        root.add(toolbar.getComponent(), BorderLayout.NORTH);

        cards = new JPanel(new CardLayout());
        treeCard = new JBPanel<>(new BorderLayout());
        messageCard = new JBPanel<>(new BorderLayout());
        messageLabel = new JBLabel("", SwingConstants.CENTER);
        messageCard.add(messageLabel, BorderLayout.CENTER);
        cards.add(treeCard, "TREE");
        cards.add(messageCard, "MSG");
        root.add(cards, BorderLayout.CENTER);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new NodeInfo("No data", mapper.nullNode()));
        treeModel = new DefaultTreeModel(rootNode);
        tree = new Tree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new JsonTreeCellRenderer());
        new TreeSpeedSearch(tree);

        JScrollPane scroll = ScrollPaneFactory.createScrollPane(tree);
        treeCard.add(scroll, BorderLayout.CENTER);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                lastSelectedPath = toLogicalPath(e.getPath());
            }
        });

        root.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                refresh();
            }
        });

        refresh();
    }

    private boolean matches(String label, String query) {
        if (query == null || query.isEmpty()) return false;
        if (!wholeWordOnly) {
            return label.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
        }
        // Whole-word match using case-insensitive word boundaries
        String pattern = "\\b" + Pattern.quote(query) + "\\b";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
        Matcher m = p.matcher(label);
        return m.find();
    }

    public JComponent getComponent() {
        return root;
    }

    public void collapseAll() {
        for (int i = tree.getRowCount() - 1; i >= 1; i--) tree.collapseRow(i);
    }

    public void expandAll() {
        int i = 0;
        while (i < tree.getRowCount()) {
            tree.expandRow(i);
            i++;
        }
    }

    public void refresh() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            showMessage("No editor is active.");
            setControlsEnabled(false);
            return;
        }
        VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (vf == null) {
            showMessage("No file is associated with the current editor.");
            setControlsEnabled(false);
            return;
        }
        if (!(vf.getFileType() instanceof JsonFileType)) {
            showMessage("Current file is not JSON. Actions are disabled.");
            setControlsEnabled(false);
            return;
        }

        String text = editor.getDocument().getText();
        try {
            JsonNode parsed = mapper.readTree(text);
            DefaultMutableTreeNode rootNode = buildTree(parsed, vf.getName());
            treeModel.setRoot(rootNode);
            treeModel.reload();
            showTree();
            setControlsEnabled(true);
            if (!lastSelectedPath.isEmpty()) {
                TreePath restored = findPathByLogical(lastSelectedPath);
                if (restored != null) {
                    tree.setSelectionPath(restored);
                    tree.scrollPathToVisible(restored);
                }
            } else {
                TreePath rootPath = new TreePath(((DefaultMutableTreeNode) treeModel.getRoot()).getPath());
                tree.setSelectionPath(rootPath);
            }
        } catch (Exception ex) {
            showMessage("Failed to parse JSON: " + ex.getMessage());
            setControlsEnabled(false);
        }
    }

    private DefaultMutableTreeNode buildTree(JsonNode node, String key) {
        DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new NodeInfo(key, node));
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> dmtn.add(buildTree(entry.getValue(), entry.getKey())));
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) dmtn.add(buildTree(node.get(i), "[" + i + "]"));
        }
        return dmtn;
    }

    private void showMessage(String msg) {
        messageLabel.setText(msg);
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "MSG");
    }

    private void showTree() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "TREE");
    }

    private void setControlsEnabled(boolean enabled) {
        searchControlsEnabled = enabled;
        searchField.setEnabled(enabled);
        toolbar.updateActionsImmediately();
    }

    private void findMatch(boolean searchUp) {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) return;

        TreePath startPath = tree.getSelectionPath();
        List<TreePath> all = getAllPaths();
        if (all.isEmpty()) return;

        int startIndex = 0;
        if (startPath != null) {
            for (int i = 0; i < all.size(); i++)
                if (all.get(i).equals(startPath)) {
                    startIndex = i;
                    break;
                }
        }

        int n = all.size();
        for (int step = 1; step <= n; step++) {
            int next = searchUp ? (startIndex - step) : (startIndex + step);
            if (next < 0) next += n;
            if (next >= n) next -= n;
            TreePath candidate = all.get(next);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) candidate.getLastPathComponent();
            Object uo = node.getUserObject();
            String s = String.valueOf(uo);
            if (matches(s, query)) {
                tree.setSelectionPath(candidate);
                tree.scrollPathToVisible(candidate);
                break;
            }
        }
    }

    public void searchPrevious() {
        findMatch(true);
    }

    public void searchNext() {
        findMatch(false);
    }

    public boolean areSearchControlsEnabled() {
        return searchControlsEnabled;
    }

    public boolean isWholeWordOnly() {
        return wholeWordOnly;
    }

    public void setWholeWordOnly(boolean wholeWordOnly) {
        this.wholeWordOnly = wholeWordOnly;
    }

    public JBTextField getSearchField() {
        return searchField;
    }

    private List<TreePath> getAllPaths() {
        List<TreePath> result = new ArrayList<>();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        if (rootNode == null) return result;
        Deque<TreePath> stack = new ArrayDeque<>();
        stack.push(new TreePath(rootNode.getPath()));
        while (!stack.isEmpty()) {
            TreePath p = stack.pop();
            result.add(p);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
            for (int i = node.getChildCount() - 1; i >= 0; i--) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                stack.push(p.pathByAddingChild(child));
            }
        }
        return result;
    }

    private List<String> toLogicalPath(TreePath path) {
        List<String> list = new ArrayList<>();
        if (path == null) return list;
        Object[] comps = path.getPath();
        for (Object o : comps) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
            Object uo = node.getUserObject();
            if (uo instanceof NodeInfo) list.add(((NodeInfo) uo).getKey());
            else {
                String s = uo == null ? "" : uo.toString();
                int idx = s.indexOf(" : ");
                list.add(idx >= 0 ? s.substring(0, idx) : s);
            }
        }
        return list;
    }

    private TreePath findPathByLogical(List<String> logical) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        if (rootNode == null || logical.isEmpty()) return null;
        if (!matchesLogical(rootNode, logical.get(0))) return null;

        TreePath path = new TreePath(rootNode.getPath());
        DefaultMutableTreeNode current = rootNode;
        for (int i = 1; i < logical.size(); i++) {
            String target = logical.get(i);
            DefaultMutableTreeNode next = null;
            for (int j = 0; j < current.getChildCount(); j++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) current.getChildAt(j);
                String key;
                Object uo = child.getUserObject();
                if (uo instanceof NodeInfo) key = ((NodeInfo) uo).getKey();
                else {
                    String s = String.valueOf(uo);
                    int idx = s.indexOf(" : ");
                    key = idx >= 0 ? s.substring(0, idx) : s;
                }
                if (Objects.equals(key, target)) {
                    next = child;
                    break;
                }
            }
            if (next == null) return null;
            current = next;
            path = path.pathByAddingChild(current);
        }
        return path;
    }

    private boolean matchesLogical(DefaultMutableTreeNode node, String logical) {
        Object uo = node.getUserObject();
        String key;
        if (uo instanceof NodeInfo) key = ((NodeInfo) uo).getKey();
        else {
            String s = String.valueOf(uo);
            int idx = s.indexOf(" : ");
            key = idx >= 0 ? s.substring(0, idx) : s;
        }
        return Objects.equals(key, logical);
    }

    public boolean isJsonEditorActive() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return false;
        VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
        return vf != null && (vf.getFileType() instanceof JsonFileType);
    }

    public JTree getTree() {
        return tree;
    }

    public void requestRefresh() {
        refresh();
    }

    public Disposable getDisposable() {
        return disposable;
    }
}
