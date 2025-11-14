package com.github.dumbledodo.blueprint.loader;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class BlueprintScanner {

    public static List<Class<?>> scan(Class<?> mainClass, Predicate<Class<?>> filter) {
        final String basePackage = mainClass.getPackage().getName();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(basePackage)
                .scan()) {

            return scanResult.getAllClasses().stream()
                    .map(ClassInfo::loadClass)
                    .filter(filter)
                    .collect(Collectors.toList());
        }
    }
}