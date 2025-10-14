package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

/**
 * Action that moves to the previous search match.
 */
public class SearchUpAction extends DumbAwareAction {
    private final JsonTreePanel panel;

    public SearchUpAction(JsonTreePanel panel) {
        super("Find Previous", "Select previous search match", AllIcons.Actions.MoveUp);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        panel.searchPrevious();
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(panel.areSearchControlsEnabled());
    }
}
