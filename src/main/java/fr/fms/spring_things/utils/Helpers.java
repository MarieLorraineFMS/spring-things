package fr.fms.spring_things.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

import ch.qos.logback.classic.Level;

/**
 * Utility helpers.
 *
 * Contains:
 * - CLI colors and printing helpers
 * - input helpers
 * - text wrapping
 * - pagination with selection
 * - small time / SQL conversion helper
 *
 * Swiss Army knife
 */
public class Helpers {

    /** Prevent instantiation. */
    private Helpers() {
    }

    // ///////////////////////////
    // SLEEP / UX
    // ///////////////////////////

    /**
     * Sleeps the current thread for a given duration (in milliseconds).
     * If ms <= 0, does nothing.
     *
     * @param ms time to sleep in milliseconds
     */
    public static void pause(int ms) {
        if (ms <= 0)
            return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ///////////////////////////
    // CLI
    // ///////////////////////////

    /** ANSI reset code */
    public static final String RESET = "\u001B[0m";
    /** ANSI yellow code */
    public static final String YELLOW = "\u001B[33m";
    /** ANSI red code */
    public static final String RED = "\u001B[31m";
    /** ANSI cyan code */
    public static final String CYAN = "\u001B[36m";
    /** ANSI green code */
    public static final String GREEN = "\u001B[32m";
    /** ANSI purple code */
    public static final String PURPLE = "\u001B[35m";

    /**
     * Prints an empty line.
     */
    public static void spacer() {
        System.out.println();
    }

    /**
     * Prints a styled title.
     *
     * @param text title to display
     */
    public static void title(String text) {
        spacer();
        printlnColor(CYAN, "//////////// " + text + " ///////////");
        spacer();
    }

    /**
     * Prints a line with ANSI color.
     *
     * @param color ANSI color code
     * @param text  text to print
     */
    public static void printlnColor(String color, String text) {
        System.out.println(color + text + RESET);
    }

    /**
     * Prints long text wrapped at a maximum width.
     * Useful when descriptions are too long for CLI.
     *
     * @param text     input text
     * @param maxWidth maximum characters per line
     */
    public static void printWrapped(String text, int maxWidth) {
        if (isNullOrEmpty(text))
            return;

        // Normalize spaces and line breaks
        String cleaned = text.replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = cleaned.split(" ");
        StringBuilder line = new StringBuilder();

        // Wrapping logic (not perfect typography, but good enough for CLI)
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxWidth) {
                System.out.println(line);
                line.setLength(0);
            }
            if (!line.isEmpty())
                line.append(" ");
            line.append(word);
        }

        if (!line.isEmpty()) {
            System.out.println(line);
        }
    }

    // ///////////////////////////
    // STRINGS
    // ///////////////////////////

    /**
     * Checks if a string is null or blank.
     *
     * @param s input string
     * @return true if null or blank, false otherwise
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isBlank();
    }

    // ///////////////////////////
    // O/I
    // ///////////////////////////

    /**
     * Asks the user to confirm.
     *
     * @param sc      scanner
     * @param message message displayed before "(o/n) :"
     * @return true if user answers "o" or "oui", false otherwise
     */
    public static boolean confirm(Scanner sc, String message) {
        System.out.print(message + " (o/n) : ");
        String ans = sc.nextLine().trim().toLowerCase();
        return ans.equals("o") || ans.equals("oui");
    }

    /**
     * Asks the user to enter an integer value.
     *
     * Keeps asking until the user provides a valid integer.
     * Dodge NPE
     *
     * @param sc    Scanner
     * @param label prompt label
     * @return a valid integer entered by the user
     */
    public static int askInt(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                // Invalid input: Nan
                printlnColor(YELLOW, "Saisie invalide.");
                pause(300);
            }
        }
    }

    /**
     * Asks the user to enter an integer value or go back.
     *
     * @param sc    scanner
     * @param label prompt label
     * @return entered integer, or null if the user chooses to go back
     */
    public static Integer askIntOrBack(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String input = sc.nextLine().trim();

            // User chooses to go back
            if ("0".equals(input)) {
                return null;
            }

            try {
                // Valid integer entered
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                // Invalid input: not a number
                printlnColor(YELLOW, "Saisie invalide.");
                pause(300);
            }
        }
    }

    // ///////////////////////////
    // PAGINATION
    // ///////////////////////////

    /**
     * Displays a paginated list & allows the user to select an id(optionnal).
     *
     * Navigation keys:
     * - "s": next page
     * - "p": previous page
     * - "0": back
     * Or the user can directly type an ID.
     *
     * @param sc             scanner used to read user input
     * @param title          title displayed in the CLI
     * @param items          items to paginate
     * @param pageSize       number of items per page
     * @param pagePrinter    function used to display the current page
     * @param idExists       predicate to validate the selected id
     * @param showSelectHint if true, allows user to select an id
     * @param <T>            item type
     * @return selected id, or null if user chooses "back" or if list is empty
     */
    public static <T> Integer paginateWithSelection(
            Scanner sc,
            String title,
            List<T> items,
            int pageSize,
            Consumer<List<T>> pagePrinter,
            IntPredicate idExists,
            Boolean showSelectHint) {

        if (items == null || items.isEmpty()) {
            printlnColor(YELLOW, "Aucun résultat.");
            return null;
        }

        int total = items.size();
        int page = 0;
        int pageCount = (total + pageSize - 1) / pageSize;

        while (true) {
            int from = page * pageSize;
            int to = Math.min(from + pageSize, total);

            Helpers.title(title + " Page " + (page + 1) + " sur " + pageCount);

            pagePrinter.accept(items.subList(from, to));
            Helpers.spacer();
            System.out.println("s) Page suivante");
            System.out.println("p) Page précédente");
            System.out.println("0) Retour");
            // Explicitly enabled (to dodge NPE)
            if (Boolean.TRUE.equals(showSelectHint)) {
                printlnColor(YELLOW, "Renseigner un ID");
            }
            spacer();

            System.out.print("Choix : ");
            String input = sc.nextLine().trim().toLowerCase();

            if ("s".equals(input)) {
                if (page >= pageCount - 1) {
                    printlnColor(YELLOW, "Déjà à la dernière page.");
                    pause(300);
                } else {
                    page++;
                }
                continue;
            }

            if ("p".equals(input)) {
                if (page <= 0) {
                    printlnColor(YELLOW, "Déjà à la première page.");
                    pause(300);
                } else {
                    page--;
                }
                continue;
            }

            if ("0".equals(input)) {
                return null;
            }

            try {
                int id = Integer.parseInt(input);
                if (idExists.test(id)) {
                    return id;
                }
                printlnColor(YELLOW, "ID introuvable.");
                pause(300);
            } catch (NumberFormatException e) {
                printlnColor(YELLOW, "Choix invalide.");
                pause(300);
            }
        }
    }

    // ///////////////////////////
    // DATE / TIME
    // ///////////////////////////

    /**
     * Converts SQL Timestamp to LocalDateTime.
     *
     * @param ts SQL timestamp
     * @return LocalDateTime value, or null if ts is null
     */
    public static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    // ///////////////////////////
    // MONEY
    // ///////////////////////////

    /**
     * Formats a monetary amount with 2 decimals.
     *
     * @param amount the amount to format
     * @return formatted string with 2 decimals
     */
    public static String formatMoney(java.math.BigDecimal amount) {
        // Null-safe formatting
        java.math.BigDecimal safe = (amount == null) ? java.math.BigDecimal.ZERO : amount;
        return String.format("%.2f", safe);
    }

    // ///////////////////////////
    // UI ERRORS/WARNINGS/INFO
    // ///////////////////////////

    /**
     * Prints user-friendly error message.
     *
     * @param context short context label
     * @param message error message
     */
    public static void uiError(String context, String message) {
        // Error prefix for CLI
        printlnColor(RED, "[" + context + "] " + message);
    }

    /**
     * Prints user-friendly warning message.
     *
     * @param context short context label
     * @param message warning message
     */
    public static void uiWarn(String context, String message) {
        printlnColor(YELLOW, "[" + context + "] " + message);
    }

    /**
     * Prints user-friendly info message.
     *
     * @param context short context label
     * @param message info message
     */
    public static void uiInfo(String context, String message) {
        printlnColor(CYAN, "[" + context + "] " + message);
    }

    // ///////////////////////////
    // LOG LEVELS
    // ///////////////////////////

    public enum LogLevel {
        ERROR(1, RED),
        WARN(2, YELLOW),
        INFO(3, CYAN),
        DEBUG(4, PURPLE);

        private final int id;
        private final String color;

        LogLevel(int id, String color) {
            this.id = id;
            this.color = color;
        }

        // Nom à partir du lvl
        public static LogLevel fromId(int id) {
            for (LogLevel level : values()) {
                if (level.id == id) {
                    return level;
                }
            }
            return INFO;
        }

        // Coloration à partir du lvl
        public String getColorByLevel() {
            return color + this.name() + RESET;
        }
    }

    /**
     * Colore un texte suivant un nom du level de log.
     *
     * @param level Nom du lvl (Level ou String)
     * @param text  Texte à colorer
     * @return Le texte entouré des codes ANSI
     */
    public static String formatWithLevelColors(Level level, String text) {

        if (level == null) {
            return text;
        }
        return formatWithLevelColors(level.toString(), text);
    }

    public static String formatWithLevelColors(String levelName, String text) {
        if (levelName == null) {
            return text;
        }

        return switch (levelName.toUpperCase()) {
            case "ERROR" -> RED + text + RESET;
            case "WARN" -> YELLOW + text + RESET;
            case "INFO" -> CYAN + text + RESET;
            case "DEBUG" -> PURPLE + text + RESET;
            default -> text;
        };
    }
}
