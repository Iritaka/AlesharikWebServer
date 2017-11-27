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

package com.alesharik.webserver.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ExceptionWithoutStacktraceTest {
    @Test
    public void ctor() throws Exception {
        ExceptionWithoutStacktrace exceptionWithoutStacktrace = new ExceptionWithoutStacktrace();
        assertEquals(0, exceptionWithoutStacktrace.getStackTrace().length);
    }

    @Test
    public void ctorWithMessage() throws Exception {
        ExceptionWithoutStacktrace exceptionWithoutStacktrace = new ExceptionWithoutStacktrace("test");
        assertEquals(0, exceptionWithoutStacktrace.getStackTrace().length);
        assertEquals("test", exceptionWithoutStacktrace.getMessage());
    }

    @Test
    public void ctorWithCause() throws Exception {
        Exception cause = new RuntimeException();
        ExceptionWithoutStacktrace exceptionWithoutStacktrace = new ExceptionWithoutStacktrace(cause);
        assertEquals(0, exceptionWithoutStacktrace.getStackTrace().length);
        assertSame(cause, exceptionWithoutStacktrace.getCause());
    }

    @Test
    public void ctorWithCauseAndMessage() throws Exception {
        Exception cause = new RuntimeException();
        ExceptionWithoutStacktrace exceptionWithoutStacktrace = new ExceptionWithoutStacktrace("test", cause);
        assertEquals(0, exceptionWithoutStacktrace.getStackTrace().length);
        assertSame(cause, exceptionWithoutStacktrace.getCause());
        assertEquals("test", exceptionWithoutStacktrace.getMessage());
    }
}