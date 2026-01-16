package com.open.rbac.openrbac.config;

import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.lang.NonNull;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FlexibleDateFormatterFactory implements AnnotationFormatterFactory<FlexibleDate> {

    @Override
    @NonNull
    public Set<Class<?>> getFieldTypes() {
        return new HashSet<>(Collections.singletonList(LocalDateTime.class));
    }

    @Override
    @NonNull
    public Printer<?> getPrinter(@NonNull FlexibleDate annotation, @NonNull Class<?> fieldType) {
        return (date, locale) -> ((LocalDateTime) date).toString();
    }

    @Override
    @NonNull
    public Parser<?> getParser(@NonNull FlexibleDate annotation, @NonNull Class<?> fieldType) {
        return new FlexibleDateParser(annotation.strategy());
    }

    private record FlexibleDateParser(DateStrategy strategy) implements Parser<LocalDateTime> {

        @Override
        @NonNull
        @SuppressWarnings("null")
        public LocalDateTime parse(@NonNull String text, @NonNull Locale locale) throws ParseException {
            try {
                // Try parsing full ISO date-time
                return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // Fallback to LocalDate
                try {
                    LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
                    if (strategy == DateStrategy.END_OF_DAY) {
                        return date.atTime(LocalTime.of(23, 59, 59));
                    } else {
                        return date.atStartOfDay();
                    }
                } catch (Exception ex) {
                    throw new ParseException("Unable to parse date: " + text, 0);
                }
            }
        }
    }
}
