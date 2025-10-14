package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareToggleAction;

/**
 * Toggle action that switches whole-word search mode.
 */
public class ToggleWholeWordAction extends DumbAwareToggleAction {
    private final JsonTreePanel panel;

    public ToggleWholeWordAction(JsonTreePanel panel) {
        super("Whole Word", "Search whole word only", AllIcons.Actions.Words);
        this.panel = panel;
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        return panel.isWholeWordOnly();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        panel.setWholeWordOnly(state);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(panel.areSearchControlsEnabled());
    }
}
