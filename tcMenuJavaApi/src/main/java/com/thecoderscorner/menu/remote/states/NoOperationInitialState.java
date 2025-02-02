/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoOperationInitialState implements RemoteConnectorState {

    private final CountDownLatch latch = new CountDownLatch(1);
    private AtomicBoolean exited = new AtomicBoolean(false);

    public NoOperationInitialState(RemoteConnectorContext context) {
    }

    @Override
    public void enterState() {
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        latch.countDown();
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.NOT_STARTED;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return false;
    }

    @Override
    public void runLoop() throws Exception {
        if(exited.get()) return;

        exited.set(latch.await(500, TimeUnit.MILLISECONDS));
    }
}
