package com.alesharik.webserver.control;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.collections.ConcurrentLiveArrayList;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.main.Helpers;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.UUID;

@Prefix("[ServerControl]")
public class ControlRequestHandler implements RequestHandler {
    private final ConcurrentLiveArrayList<UUID> sessions = new ConcurrentLiveArrayList<>();
    private final AdminDataStorageImpl holder;
    private final FileManager fileManager;

    public ControlRequestHandler(FileManager fileManager, AdminDataStorageImpl holder) {
        this.holder = holder;
        this.fileManager = fileManager;
    }

    @Override
    public boolean canHandleRequest(Request request) throws IOException {
        String command = request.getDecodedRequestURI();
        return command.equals("/login") || command.equals("/changeLoginPassword");
    }

    @Override
    public void handleRequest(Request request, Response response) throws Exception {
        String command = request.getDecodedRequestURI();
        switch (command) {
            case "/login":
                handleLoginCommand(request, response);
                break;
            case "/changeLoginPassword":
                handleLoginPasswordChangeCommand(request, response);
                break;
            default:
                Logger.log("Oops! We have unexpected request!" + command);
                break;
        }
    }

    public boolean isSessionValid(UUID sessionID) {
        return sessions.contains(sessionID);
    }

    private void handleLoginPasswordChangeCommand(Request request, Response response) throws IOException {
        Cookie uuidCookie = Helpers.getCookieForName("UUID", request.getCookies());
        if(uuidCookie == null || !isSessionValid(UUID.fromString(uuidCookie.getValue()))) {
            response.setStatus(HttpStatus.FORBIDDEN_403);
            return;
        }
        if(holder.check(request.getParameter("oldLogin"), request.getParameter("oldPassword"))) {
            holder.updateLoginPassword(request.getParameter("oldLogin"), request.getParameter("oldPassword"),
                    request.getParameter("newLogin"), request.getParameter("newPassword"));

            response.setStatus(HttpStatus.OK_200);
            response.setContentType("text/plain");
            response.setContentLength("ok".length());
            response.getWriter().write("ok");
        } else {
            response.setStatus(HttpStatus.OK_200);
            response.setContentType("text/plain");
            response.setContentLength("oldLogPassError".length());
            response.getWriter().write("oldLogPassError");
        }
    }

    private void handleLoginCommand(Request request, Response response) throws IOException {
        String logpass = request.getParameter("logpass");
        boolean remember = Boolean.parseBoolean(request.getParameter("remember"));
        if(logpass != null && !logpass.isEmpty()) {
            if(holder.check(logpass)) {
                loginSuccess(response, remember, logpass);
                return;
            }
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        if(login == null || password == null) {
            loginFailed(response);
            return;
        }
        String loginPassword = LoginPasswordCoder.encode(login, password);
        if(holder.check(loginPassword)) {
            loginSuccess(response, remember, loginPassword);
        } else {
            loginFailed(response);
        }
    }

    private void loginSuccess(Response response, boolean remember, String logpass) throws IOException {
//        response.setStatus(HttpStatus.FOUND_302);
//        response.addHeader(Header.Location, "/dashboard.html");
        UUID uuid = UUID.randomUUID();
        Cookie uuidCookie = new Cookie("UUID", uuid.toString());
        uuidCookie.setHttpOnly(true);
        uuidCookie.setPath("/");
        uuidCookie.setMaxAge(3600);
        sessions.add(uuid, 3600L);
        response.addCookie(uuidCookie);
        response.sendRedirect("/dashboard.html");
        if(remember) {
            Cookie logpassCookie = new Cookie("Logpass", logpass);
            logpassCookie.setHttpOnly(false);
            logpassCookie.setPath("/");
            logpassCookie.setMaxAge(120);
            response.addCookie(logpassCookie);
        }
    }

    private void loginFailed(Response response) throws IOException {
        response.sendRedirect("/index.html?incorrect=true");
    }
}
