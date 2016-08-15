package com.alesharik.webserver.logger.handlers;

import java.util.logging.Level;

/**
 * This class used for process warnings
 */
public class WarningConsoleHandler extends ConsoleHandler {

    public WarningConsoleHandler() {
        super();
    }

    @Override
    protected Level getLevelProperty() {
        return Level.WARNING;
    }
}
