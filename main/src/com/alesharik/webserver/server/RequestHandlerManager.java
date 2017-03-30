package com.alesharik.webserver.server;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.server.api.RequestHandler;
import lombok.experimental.UtilityClass;
import org.glassfish.grizzly.http.server.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class hold all {@link RequestHandler}s. Every handler instantiated by empty constructor. Every handler instance
 * stored in set
 */
@ClassPathScanner
@UtilityClass
public class RequestHandlerManager {
    private static final Set<RequestHandler> handlers = new CopyOnWriteArraySet<>();

    @ListenInterface(RequestHandler.class)
    public static void listenRequestHandler(@Nonnull Class<?> requestHandlerClazz) {
        try {
//            Constructor<?> constructor = requestHandlerClazz.getConstructor();
//            constructor.setAccessible(true);
//            handlers.add((RequestHandler) constructor.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Request {@link RequestHandler} for request
     *
     * @param request http server request
     * @return request handler or null if all handlers can't handle request
     */
    @Nullable
    public static RequestHandler getRequestHandler(Request request) throws IOException {
        for(RequestHandler handler : handlers) {
            if(handler.canHandleRequest(request)) {
                return handler;
            }
        }
        return null;
    }

    public static Set<RequestHandler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }
}