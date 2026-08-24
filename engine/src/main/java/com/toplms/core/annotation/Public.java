package com.toplms.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // Can be placed on methods or whole classes
@Retention(RetentionPolicy.RUNTIME)
public @interface Public {
}
