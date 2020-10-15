package io.github.spencerpark.jupyter.ipywidgets.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonInline {
    String value() default "{0}";

    String prefix() default "";
}
