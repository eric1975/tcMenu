package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.remote.RemoteMenuController;

public abstract class BaseBoolEditorComponent extends BaseEditorComponent {
    protected boolean currentVal = false;

    protected BaseBoolEditorComponent(RemoteMenuController controller, ComponentSettings settings,
                                      MenuItem item, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
    }

    @Override
    public void onItemUpdated(MenuState<?> newState) {
        if (newState.getValue() != null && newState.getValue() instanceof Boolean)
        {
            currentVal = (boolean) newState.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }

    @Override
    public String getControlText() {
        String prefix = "";
        if (controlTextIncludesName()) prefix = item.getName();

        if (!controlTextIncludesValue()) return prefix;

        return prefix + " " + MenuItemFormatter.formatForDisplay(item, currentVal);
    }

    protected void toggleState() {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;

        try {
            var correlation =  remoteController.sendAbsoluteUpdate(item, !currentVal);
            editStarted(correlation);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to send message", ex);
        }
    }
}
