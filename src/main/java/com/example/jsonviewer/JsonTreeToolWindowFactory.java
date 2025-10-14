package com.example.jsonviewer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class JsonTreeToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JsonTreePanel panel = new JsonTreePanel(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel.getComponent(), "", false);
        content.setDisposer(panel.getDisposable());
        toolWindow.getContentManager().addContent(content);

        project.getMessageBus()
                .connect(panel.getDisposable())
                .subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
                    @Override
                    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
                        ToolWindow window = toolWindowManager.getToolWindow(toolWindow.getId());
                        if (window != null && window.isVisible()) {
                            panel.requestRefresh();
                        }
                    }
                });
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) { return true; }
}
