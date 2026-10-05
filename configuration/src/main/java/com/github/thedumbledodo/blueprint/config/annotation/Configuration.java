package com.github.thedumbledodo.blueprint.config.annotation;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;

import java.lang.annotation.*;

@Inherited
@BlueprintComponent
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {

    String value();

    String path() default "";
}
