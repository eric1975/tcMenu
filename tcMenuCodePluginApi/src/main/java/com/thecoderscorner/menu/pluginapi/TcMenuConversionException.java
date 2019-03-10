/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

/**
 * An exception that expresses an error that has occurred during code conversion
 */
public class TcMenuConversionException extends Exception {
    public TcMenuConversionException(String message) {
        super(message);
    }

    public TcMenuConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
