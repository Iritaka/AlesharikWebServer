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
import com.alesharik.webserver.api.server.wrapper.http.data.WeightMimeType;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class AcceptHeader extends Header<WeightMimeType[]> {
    private final Pattern pattern;

    public AcceptHeader() {
        super("Accept");
        pattern = Pattern.compile(name + ": ");
    }

    /**
     * Ignore all broken types
     *
     * @param str header line
     */
    @Override
    public WeightMimeType[] getValue(String str) {
        String types = pattern.matcher(str).replaceFirst("");

        String[] parts = types.split(", ");
        int partsLength = parts.length;
        WeightMimeType[] ret = new WeightMimeType[partsLength];
        int j = 0;

        for(String part : parts) {
            ret[j] = WeightMimeType.parseTypeNullUnsafe(part);
            if(ret[j] == null)
                continue;
            j++;
        }

        if(partsLength != j) {
            ret = Arrays.copyOf(ret, j);
        }
        return ret;
    }

    @Override
    public String build(WeightMimeType[] value) {
        StringBuilder stringBuilder = new StringBuilder("Accept: ");
        for(int i = 0; i < value.length; i++) {
            WeightMimeType val = value[i];

            if(i > 0)
                stringBuilder.append(", ");

            stringBuilder.append(val.toMimeType());
        }
        return stringBuilder.toString();
    }
}
