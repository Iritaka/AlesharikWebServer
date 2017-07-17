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

package com.alesharik.webserver.api.server.wrapper.http.header;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntHeaderTest {
    private IntHeader header;

    @Before
    public void setUp() throws Exception {
        header = new IntHeader("Test");
    }

    @Test
    public void getValueTest() throws Exception {
        assertEquals(Integer.valueOf(3), header.getValue("Test: 3"));
    }

    @Test
    public void buildTest() throws Exception {
        assertEquals("Test: 3", header.build(3));
    }
}