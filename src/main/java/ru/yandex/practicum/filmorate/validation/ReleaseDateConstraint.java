package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;


import jakarta.validation.Payload;


@Documented
@Constraint(validatedBy = ReleaseDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDateConstraint {
    String message() default "Дата релиза не может быть раньше 28 декабря 1895";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

