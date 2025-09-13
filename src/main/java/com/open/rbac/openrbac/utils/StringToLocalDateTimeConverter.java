package com.open.rbac.openrbac.utils;

import jakarta.annotation.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(@Nullable String source) {
        if (source == null || source.isBlank()) return null;

        try {
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                LocalDate date = LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid date format, expected yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss");
            }
        }
    }
}
