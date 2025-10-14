package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Reloads JSON from the current editor and rebuilds the tree.
 */
public class RefreshAction extends AnAction {
    private final JsonTreePanel panel;

    public RefreshAction(JsonTreePanel panel) {
        super("Refresh", "Reload JSON from the current editor and rebuild the tree", AllIcons.Actions.Refresh);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        panel.requestRefresh();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(panel.isJsonEditorActive());
    }
}
