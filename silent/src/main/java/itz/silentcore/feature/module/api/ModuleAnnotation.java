package itz.silentcore.feature.module.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleAnnotation {
    String name();
    Category category();
    String description() default "This module has no description.";
}