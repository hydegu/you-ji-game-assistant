package com.example.assistant.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * 至少一个字段不为空校验器实现
 */
public class AtLeastOneNotNullValidator implements ConstraintValidator<AtLeastOneNotNull, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        Class<?> clazz = value.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(value);
                if (fieldValue != null) {
                    if (fieldValue instanceof String && !((String) fieldValue).isEmpty()) {
                        return true;
                    }
                    return true;
                }
            } catch (IllegalAccessException e) {
                continue;
            }
        }
        return false;
    }
}