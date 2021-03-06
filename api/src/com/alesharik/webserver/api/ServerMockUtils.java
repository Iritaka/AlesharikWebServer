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

package com.alesharik.webserver.api;

import com.alesharik.webserver.api.server.wrapper.http.HttpStatus;
import com.alesharik.webserver.api.server.wrapper.http.HttpVersion;
import com.alesharik.webserver.api.server.wrapper.http.Method;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.server.BatchingRunnableTask;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerMockUtils {
    public static Request.Builder newTestRequest() {
        return newTestRequest("/");
    }

    public static Request.Builder newTestRequest(String path) {
        return newTestRequest(path, Method.GET);
    }

    public static Request.Builder newTestRequest(String path, Method method) {
        return Request.Builder.start(method.name() + ' ' + path + " HTTP/1.1");
    }

    public static Response newResponse() {
        return new ResponseWrapper();
    }

    public static Response validate(Response response) {
        if(!(response instanceof ResponseWrapper))
            throw new Error("Use TestUtils#newResponse for response creation!");
        return new ResponseValidator((ResponseWrapper) response);
    }

    private static final class ResponseWrapper extends Response {
        public HttpVersion getHttpVersion() {
            return version;
        }

        public HttpStatus getHttpStatus() {
            return status;
        }
    }

    private static final class ResponseValidator extends Response {//TODO body support and all other methods!
        private final ResponseWrapper response;

        public ResponseValidator(ResponseWrapper response) {
            this.response = response;
        }

        @Override
        public void setVersion(HttpVersion version) {
            if(response.getHttpVersion() != version)
                throw new AssertionError("Excepted: " + response.getHttpVersion() + ", actual: " + version);
        }

        @Override
        public void respond(HttpStatus status) {
            if(!response.getHttpStatus().equals(status))
                throw new AssertionError("Excepted: " + response.getHttpStatus() + ", actual: " + status);
        }
    }

    /**
     * Simply executes {@link Thread#sleep(long)} in executor
     */
    public static final class WaitTask implements BatchingRunnableTask<Object> {
        private final long time;

        /**
         * @param time time in milliseconds
         */
        public WaitTask(long time) {
            this.time = time;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                //Ok :(
            }
        }

        @Override
        public Object getKey() {
            return new Object();
        }
    }
}
