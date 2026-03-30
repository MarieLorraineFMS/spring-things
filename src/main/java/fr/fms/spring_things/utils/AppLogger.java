package fr.fms.spring_things.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple console logger.
 *
 * Provides a few log levels with timestamps & colors:
 * - info
 * - error
 * - ok
 * - rocket (because why not 🚀)
 *
 * This is intentionally lightweight: no frameworks, no configuration,
 * just logs that help you understand what you do.
 */
public final class AppLogger {

    /** Prevent instantiation. */
    private AppLogger() {
    }

    /** Time format for log prefix. */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Verbose mode flag. */
    private static boolean verbose = false;

    /**
     * Enables or disables verbose logging.
     *
     * @param enabled true to enable verbose logs, false to disable
     */
    public static void setVerbose(boolean enabled) {
        verbose = enabled;
    }

    /**
     * @return true if verbose logs are enabled
     */
    public static boolean isVerbose() {
        return verbose;
    }

    public enum AppLogLevel {
        ERROR(Helpers.RED, "❌"),
        WARN(Helpers.YELLOW, "⚠️"),
        SUCCESS(Helpers.GREEN, "✅"),
        INFO(Helpers.CYAN, "ℹ️"),
        DEBUG(Helpers.PURPLE, "🔍"),
        ROCKET(Helpers.CYAN, "🚀");

        private final String color;
        private final String icon;

        AppLogLevel(String color, String icon) {
            this.color = color;
            this.icon = icon;
        }

        public String getColor() {
            return color;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static void log(AppLogLevel level, String msg) {
        if (!verbose && level != AppLogLevel.ERROR)
            return;

        String time = LocalTime.now().format(FMT);
        String color = level.getColor();
        String icon = level.getIcon();
        String levelName = String.format("%-7s", level.name());
        String levelDisplay = (level == AppLogLevel.ROCKET) ? "" : String.format("%-7s", levelName) + " : ";

        System.out.println(
                "[" + time + "] " +
                        color +
                        icon + "  "
                        + levelDisplay +
                        msg +
                        Helpers.RESET);
    }

    // Raccourcis
    public static void info(String msg) {
        log(AppLogLevel.INFO, msg);
    }

    public static void error(String msg) {
        log(AppLogLevel.ERROR, msg);
    }

    public static void rocket(String msg) {
        log(AppLogLevel.ROCKET, msg);
    }

    public static void warn(String msg) {
        log(AppLogLevel.WARN, msg);
    }

    public static void success(String msg) {
        log(AppLogLevel.SUCCESS, msg);
    }

    /**
     * Logs an exception with its type and message.
     * To keeps console output readable while still useful.
     *
     * @param msg context message
     * @param e   exception to log
     */
    public static void exception(String msg, Exception e) {
        error(msg + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
    }
}
