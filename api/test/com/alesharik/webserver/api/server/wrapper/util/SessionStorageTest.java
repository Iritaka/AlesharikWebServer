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

package com.alesharik.webserver.api.server.wrapper.util;

import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.http.data.Cookie;
import lombok.AllArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

import static com.alesharik.webserver.api.server.wrapper.util.SessionStorage.forRequest;
import static org.junit.Assert.*;

public class SessionStorageTest {
    private SessionStorage sessionStorage;

    @Before
    public void setUp() throws Exception {
        sessionStorage = new SessionStorage(request -> new StorageImpl(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void visit() throws Exception {
        Request request = new Request();
        Response response = Response.getResponse();

        sessionStorage.visit(request, response);

        Storage storage = forRequest(request);
        assertNotNull(storage);
        String id = "";
        for(Cookie cookie : response.getCookies()) {
            if(cookie.getName().equals("_sessionID"))
                id = cookie.getValue();
        }
        assertFalse(id.isEmpty());

        Request request1 = new Request1(id);
        sessionStorage.visit(request1, response);

        assertEquals(storage, SessionStorage.forUntrustedRequest(request1));

        Thread.sleep(1010);

        Request request2 = new Request1(id);
        sessionStorage.visit(request2, response);
        assertNotSame(storage, forRequest(request2));
    }

    @Test(expected = IllegalStateException.class)
    public void forRequestWithoutStorage() throws Exception {
        forRequest(new Request());
        fail();
    }

    @Test
    public void forUntrustedRequest() throws Exception {
        assertNull(SessionStorage.forUntrustedRequest(new Request()));
    }

    private static final class StorageImpl implements Storage {
    }

    @AllArgsConstructor
    private static final class Request1 extends Request {
        private final String value;

        @Nonnull
        @Override
        public Cookie[] getCookies() {
            return new Cookie[]{new Cookie("_sessionID", value)};
        }
    }
}