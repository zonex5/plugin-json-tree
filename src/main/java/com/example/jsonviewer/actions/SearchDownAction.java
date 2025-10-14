package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

/**
 * Action that moves to the next search match.
 */
public class SearchDownAction extends DumbAwareAction {
    private final JsonTreePanel panel;

    public SearchDownAction(JsonTreePanel panel) {
        super("Find Next", "Select next search match", AllIcons.Actions.MoveDown);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        panel.searchNext();
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(panel.areSearchControlsEnabled());
    }
}
