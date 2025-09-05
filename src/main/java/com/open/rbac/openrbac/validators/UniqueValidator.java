package com.open.rbac.openrbac.validators;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UniqueValidator implements ConstraintValidator<Unique, Object> {

    private final EntityManager entityManager;

    private Class<?> entityClass;
    private String fieldName;
    private String whereClause;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        this.fieldName = constraintAnnotation.field();
        this.whereClause = constraintAnnotation.where();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            String value;
            if (obj instanceof String) {
                value = (String) obj; // Field-level validation
            } else {
                // Class-level validation
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                value = (String) field.get(obj);
            }
            
            if (value == null || value.isBlank()) return true;

            StringBuilder queryBuilder = new StringBuilder(
                    String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :value",
                            entityClass.getSimpleName(), fieldName)
            );

            String processedWhereClause = whereClause;
            if (!whereClause.isBlank()) {
                // Only apply parameter substitution for class-level validation
                if (!(obj instanceof String)) {
                    processedWhereClause = replaceParameters(whereClause, obj);
                }
                queryBuilder.append(" AND ").append(processedWhereClause);
            }

            var query = entityManager.createQuery(queryBuilder.toString(), Long.class)
                    .setParameter("value", value);

            Long count = query.getSingleResult();
            boolean isValid = count == 0;
            
            // For class-level validation, set the property node to the specific field
            if (!isValid && !(obj instanceof String)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(fieldName)
                        .addConstraintViolation();
            }
            
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

    private String replaceParameters(String whereClause, Object obj) throws Exception {
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(whereClause);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            matcher.appendReplacement(result, fieldValue != null ? fieldValue.toString() : "");
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
