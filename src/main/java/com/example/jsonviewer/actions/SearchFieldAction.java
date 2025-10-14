package com.example.jsonviewer.actions;

import com.example.jsonviewer.JsonTreePanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.JComponent;

/**
 * Action that hosts the search text field inside the toolbar.
 */
public class SearchFieldAction extends DumbAwareAction implements CustomComponentAction {
    private final JsonTreePanel panel;

    public SearchFieldAction(JsonTreePanel panel) {
        super("Search", "Enter text to search", null);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // No direct action is needed for the text field host.
    }

    @Override
    public void update(AnActionEvent e) {
        boolean enabled = panel.areSearchControlsEnabled();
        e.getPresentation().setEnabled(enabled);
        panel.getSearchField().setEnabled(enabled);
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation, String place) {
        return panel.getSearchField();
    }

    @Override
    public void updateCustomComponent(JComponent component, Presentation presentation) {
        component.setEnabled(panel.areSearchControlsEnabled());
    }
}
