package it.agrimontana.salesforce;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Utils {
    static final DateTimeFormatter AS400_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String trimString(String value) {
        return value != null ? value.trim() : null;
    }

    public static LocalDateTime parseDate(String dtmncaRaw) {
        if (dtmncaRaw != null && !dtmncaRaw.trim().isEmpty()) {
            try {
                return (LocalDate.parse(dtmncaRaw.trim(), AS400_DATE_FORMAT).atStartOfDay());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static String convertAs400DateToIso(String as400Date) {
        if (as400Date != null && !as400Date.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(as400Date.trim(), AS400_DATE_FORMAT);
                return date.format(ISO_DATE_FORMAT);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }
}
