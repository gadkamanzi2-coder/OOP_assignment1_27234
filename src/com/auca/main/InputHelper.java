package com.auca.main;

import com.auca.exceptions.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputHelper {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println("Input has ended. The program will stop.");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    public static boolean isBack(String text) {
        return text != null && text.trim().equalsIgnoreCase("BACK");
    }

    public static boolean isCancel(String text) {
        return text != null && text.trim().equalsIgnoreCase("CANCEL");
    }

    public static boolean isNext(String text) {
        return text != null && text.trim().equalsIgnoreCase("NEXT");
    }

    public static int parseMenuNumber(String text) {
        if (text == null) {
            return -1;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static LocalDate parseDate(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        String clean = text.trim();
        if (!clean.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ValidationException(fieldName + " must use the format YYYY-MM-DD.");
        }
        try {
            return LocalDate.parse(clean);
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName + " is not a real date (example: 2025-01-31).");
        }
    }

    public static BigDecimal parseDecimal(String text, String fieldName)
            throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        String clean = text.trim();
        if (clean.length() > 40 || !clean.matches("[+-]?\\d+(\\.\\d+)?")) {
            throw new ValidationException(fieldName + " must be a valid number (example: 1500.50).");
        }
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid number (example: 1500.50).");
        }
    }
    public static BigDecimal parseMoney(String text, String fieldName) throws ValidationException {
        BigDecimal amount = parseDecimal(text, fieldName);
        ValidationUtil.requireMoney(amount, fieldName);
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }


    public static BigDecimal parseSignedMoney(String text, String fieldName)
            throws ValidationException {
        BigDecimal amount = parseDecimal(text, fieldName);
        ValidationUtil.requireSignedMoney(amount, fieldName);
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }


    public static BigDecimal parseRate(String text, String fieldName) throws ValidationException {
        BigDecimal rate = parseDecimal(text, fieldName);
        ValidationUtil.requireRate(rate, fieldName);
        return rate;
    }

    public static boolean parseYesNo(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required (YES or NO).");
        }
        String clean = text.trim();
        if (clean.equalsIgnoreCase("YES") || clean.equalsIgnoreCase("Y")) {
            return true;
        }
        if (clean.equalsIgnoreCase("NO") || clean.equalsIgnoreCase("N")) {
            return false;
        }
        throw new ValidationException(fieldName + " must be YES or NO.");
    }
}