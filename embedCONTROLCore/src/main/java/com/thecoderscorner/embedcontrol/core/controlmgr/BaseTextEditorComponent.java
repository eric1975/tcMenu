package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.RemoteMenuController;

public abstract class BaseTextEditorComponent<T> extends BaseEditorComponent {
    public T currentVal;

    protected BaseTextEditorComponent(RemoteMenuController controller, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
    }

    protected void validateAndSend(String text) {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;

        try {
            var toSend = MenuItemFormatter.formatToWire(item, text);
            var correlation =  remoteController.sendAbsoluteUpdate(item, toSend);
            editStarted(correlation);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to send message", ex);
            markRecentlyUpdated(RenderingStatus.CORRELATION_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemUpdated(MenuState<?> newValue) {
        if (newValue.getValue() != null)
        {
            MenuState<T> actualState = (MenuState<T>) newValue;
            currentVal = actualState.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }

    @Override
    public String getControlText() {
        String str = "";
        if (controlTextIncludesName())  str = item.getName() + " ";
        if (controlTextIncludesValue()) str += MenuItemFormatter.formatForDisplay(item, currentVal);
        return str;
    }

}