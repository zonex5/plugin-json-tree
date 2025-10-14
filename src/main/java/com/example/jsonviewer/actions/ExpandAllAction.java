package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Expands all nodes in the JSON tree.
 */
public class ExpandAllAction extends AnAction {
    private final JsonTreePanel panel;

    public ExpandAllAction(JsonTreePanel panel) {
        super("Expand All", "Expand all nodes in the JSON tree", AllIcons.Actions.Expandall);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        panel.expandAll();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(panel.isJsonEditorActive());
    }
}
