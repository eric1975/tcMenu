/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.state.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

public class MenuScrollChoiceBootCommand extends BootItemMenuCommand<ScrollChoiceMenuItem, CurrentScrollPosition> {

    public MenuScrollChoiceBootCommand(int subMenuId, ScrollChoiceMenuItem menuItem, CurrentScrollPosition currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.BOOT_SCROLL_CHOICE;
    }

    @Override
    public AnyMenuState internalNewMenuState(AnyMenuState oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return MenuItemHelper.stateForMenuItem(getMenuItem(), getCurrentValue(), changed, oldState.isActive());
    }
}
