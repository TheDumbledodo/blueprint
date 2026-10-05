package com.github.thedumbledodo.blueprint.command.annotation;

import java.lang.annotation.*;

@Repeatable(CommandPermissions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CommandPermission {

    String value();
}
