/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.internals;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * This class wrap {@link Shutdown} class
 */
public enum ShutdownState {
    /**
     * VM is running normally
     */
    RUNNING,
    /**
     * VM is handling Shutdown hooks
     */
    HOOKS,
    /**
     * VM is handling finalizers
     */
    FINALIZERS;

    private static final Field stateField;

    static {
        try {
            Class<?> clazz = Class.forName("java.lang.Shutdown");
            stateField = clazz.getDeclaredField("state");
            stateField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new InternalHackingError("Can't get access to Shutdown.state field!", e);
        }
    }

    /**
     * Return true if VM is running, overwise false
     */
    public boolean isRunning() {
        return this == RUNNING;
    }

    /**
     * Return true if VM is in shutdown sequence, overwise false
     */
    public boolean isStopping() {
        return this != RUNNING;
    }

    /**
     * Return current VM state
     */
    @Nonnull
    public static ShutdownState getCurrentState() {
        int state = getFieldValue();
        if(state == 0)
            return RUNNING;
        else if(state == 1)
            return HOOKS;
        else if(state == 2)
            return FINALIZERS;
        else
            throw new RuntimeException();
    }

    private static int getFieldValue() {
        try {
            return stateField.getInt(null);
        } catch (IllegalAccessException e) {
            throw new InternalHackingError("Access to Shutdown.state denied!", e);
        }
    }
}
