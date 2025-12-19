package com.open.rbac.openrbac.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlexibleDateDeserializer extends JsonDeserializer<LocalDateTime> implements ContextualDeserializer {

    private DateStrategy strategy = DateStrategy.START_OF_DAY;

    public FlexibleDateDeserializer() {
    }

    public FlexibleDateDeserializer(DateStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        FlexibleDate annotation = property.getAnnotation(FlexibleDate.class);
        if (annotation != null) {
            return new FlexibleDateDeserializer(annotation.strategy());
        }
        return this;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            try {
                LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
                if (strategy == DateStrategy.END_OF_DAY) {
                    return date.atTime(LocalTime.MAX);
                } else {
                    return date.atStartOfDay();
                }
            } catch (Exception ex) {
                throw new IOException("Unable to parse date: " + text);
            }
        }
    }
}
