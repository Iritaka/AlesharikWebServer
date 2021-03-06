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

import com.alesharik.webserver.api.server.wrapper.http.Header;
import com.alesharik.webserver.api.server.wrapper.http.data.Cookie;

public class CookieHeader extends Header<Cookie[]> {
    public CookieHeader() {
        super("Cookie: ");
    }

    @Override
    public Cookie[] getValue(String str) {
        return Cookie.parseCookies(str);
    }

    @Override
    public String build(Cookie[] value) {
        StringBuilder stringBuilder = new StringBuilder("Cookie: ");

        boolean notFirst = false;
        for(Cookie cookie : value) {
            if(notFirst)
                stringBuilder.append("; ");
            else
                notFirst = true;
            stringBuilder.append(cookie.getName()).append('=').append(cookie.getValue());
        }

        return stringBuilder.toString();
    }
}
