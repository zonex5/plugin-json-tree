package com.example.jsonviewer;

import com.intellij.icons.AllIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Custom renderer that sets icons and bolds key part.
 */
public class JsonTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Icon objIcon;
    private final Icon arrIcon;
    private final Icon valIcon;

    public JsonTreeCellRenderer() {
        objIcon = AllIcons.Json.Object;
        arrIcon = AllIcons.Json.Array;
        valIcon = AllIcons.Nodes.Field;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            Object uo = ((DefaultMutableTreeNode) value).getUserObject();
            if (uo instanceof NodeInfo) {
                NodeInfo info = (NodeInfo) uo;
                // HTML to bold the key before " : "
                String label = info.toString();
                int idx = label.indexOf(" : ");
                if (idx >= 0) {
                    String k = label.substring(0, idx);
                    String rest = label.substring(idx); // includes leading " : "
                    setText("<html><b>" + escape(k) + "</b>" + escape(rest) + "</html>");
                } else {
                    setText("<html><b>" + escape(label) + "</b></html>");
                }
                // Icon by kind
                switch (info.getKind()) {
                    case OBJECT:
                        setIcon(objIcon);
                        break;
                    case ARRAY:
                        setIcon(arrIcon);
                        break;
                    default:
                        setIcon(valIcon);
                }
            }
        }
        return this;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
