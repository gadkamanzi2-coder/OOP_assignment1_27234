package com.auca.main;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ValidationUtil {

    public static final BigDecimal MAX_MONEY = new BigDecimal("999999999999.99");

    public static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public static void requireText(String value, String fieldName, int min, int max)
            throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        int length = value.trim().length();
        if (length < min || length > max) {
            throw new ValidationException(fieldName + " must be between " + min
                    + " and " + max + " characters.");
        }
    }

    public static void requireDigits(String value, String fieldName, int exactLength)
            throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        if (value.length() != exactLength || !isAllDigits(value)) {
            throw new ValidationException(fieldName + " must be exactly "
                    + exactLength + " digits.");
        }
    }

    public static void requireCode(String value, String fieldName, int min, int max)
            throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        if (value.length() < min || value.length() > max) {
            throw new ValidationException(fieldName + " must be between " + min
                    + " and " + max + " characters.");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean upper = c >= 'A' && c <= 'Z';
            boolean digit = c >= '0' && c <= '9';
            if (!upper && !digit && c != '-') {
                throw new ValidationException(fieldName
                        + " may contain only uppercase letters, digits and hyphens.");
            }
        }
    }

    public static void requireEmail(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Email is required.");
        }
        if (value.length() > 120) {
            throw new ValidationException("Email must not exceed 120 characters.");
        }
        if (!value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Email format is not valid (example: name@mail.com).");
        }
    }

    public static void requirePhone(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }
        String digits = value;
        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        }
        if (digits.length() < 10 || digits.length() > 15 || !isAllDigits(digits)) {
            throw new ValidationException(
                    "Phone number must have 10-15 digits with an optional leading +.");
        }
    }

    public static void requireDate(LocalDate date, String fieldName) throws ValidationException {
        if (date == null) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static void requireNotFuture(LocalDate date, String fieldName)
            throws ValidationException {
        requireDate(date, fieldName);
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(fieldName + " cannot be in the future.");
        }
    }

    public static void requireBefore(LocalDate earlier, String earlierName,
                                     LocalDate later, String laterName)
            throws ValidationException {
        requireDate(earlier, earlierName);
        requireDate(later, laterName);
        if (!earlier.isBefore(later)) {
            throw new ValidationException(earlierName + " must be before " + laterName + ".");
        }
    }

    public static void requireMoney(BigDecimal amount, String fieldName)
            throws ValidationException {
        if (amount == null) {
            throw new ValidationException(fieldName + " is required.");
        }
        if (amount.signum() < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(fieldName + " can have at most 2 decimal places.");
        }
        if (amount.compareTo(MAX_MONEY) > 0) {
            throw new ValidationException(fieldName + " cannot exceed 999999999999.99.");
        }
    }

    public static void requireSignedMoney(BigDecimal amount, String fieldName)
            throws ValidationException {
        if (amount == null) {
            throw new ValidationException(fieldName + " is required.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(fieldName + " can have at most 2 decimal places.");
        }
        if (amount.abs().compareTo(MAX_MONEY) > 0) {
            throw new ValidationException(fieldName + " cannot exceed 999999999999.99.");
        }
    }

    public static void requirePositiveMoney(BigDecimal amount, String fieldName)
            throws ValidationException {
        requireMoney(amount, fieldName);
        if (amount.signum() == 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void requireRate(BigDecimal rate, String fieldName)
            throws ValidationException {
        if (rate == null) {
            throw new ValidationException(fieldName + " is required.");
        }
        if (rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException(fieldName + " must be between 0 and 100.");
        }
        if (rate.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(fieldName + " can have at most 2 decimal places.");
        }
    }

    public static void requireChoice(String value, String fieldName, String[] allowed)
            throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i].equals(value)) {
                return;
            }
        }
        String list = "";
        for (int i = 0; i < allowed.length; i++) {
            list = list + allowed[i];
            if (i < allowed.length - 1) {
                list = list + ", ";
            }
        }
        throw new ValidationException(fieldName + " must be one of: " + list + ".");
    }

    public static String describe(Identifiable item) {
        if (item == null) {
            return "(none)";
        }
        return item.getId() + " - " + item.getDisplayName();
    }

    public static void printField(String label, Object value) {
        System.out.println(String.format("  %-26s: %s", label, value));
    }

    private static boolean isAllDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}