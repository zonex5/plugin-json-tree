package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Collapses all nodes in the JSON tree.
 */
public class CollapseAllAction extends AnAction {
    private final JsonTreePanel panel;

    public CollapseAllAction(JsonTreePanel panel) {
        super("Collapse All", "Collapse all nodes in the JSON tree", AllIcons.Actions.Collapseall);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        panel.collapseAll();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(panel.isJsonEditorActive());
    }
}
