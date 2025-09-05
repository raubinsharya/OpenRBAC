package com.open.rbac.openrbac.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidator.class)
public @interface Unique {
    String message() default "Already taken";

    Class<?> entity();          // e.g., User.class
    String field();             // e.g., "mobile"
    String where() default "";  // e.g., "e.email != :email"

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
