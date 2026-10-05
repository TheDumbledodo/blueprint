package com.github.thedumbledodo.blueprint.loader;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;
import com.github.thedumbledodo.blueprint.fixture.other.OutsideComponent;
import com.github.thedumbledodo.blueprint.fixture.scan.ScanMain;
import com.github.thedumbledodo.blueprint.fixture.scan.ScannedComponent;
import com.github.thedumbledodo.blueprint.fixture.scan.nested.NestedComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintScannerTest {

    @Test
    void scanFindsClassesInTheMainPackageAndBelow() {
        final List<Class<?>> classes = BlueprintScanner.scan(ScanMain.class, clazz -> true);

        assertTrue(classes.contains(ScanMain.class));
        assertTrue(classes.contains(ScannedComponent.class));
        assertTrue(classes.contains(NestedComponent.class));
    }

    @Test
    void scanStaysInsideTheMainPackage() {
        final List<Class<?>> classes = BlueprintScanner.scan(ScanMain.class, clazz -> true);

        assertFalse(classes.contains(OutsideComponent.class));
    }

    @Test
    void filterIsApplied() {
        final List<Class<?>> classes = BlueprintScanner.scan(ScanMain.class, clazz -> clazz.isAnnotationPresent(BlueprintComponent.class));

        assertEquals(2, classes.size());
        assertFalse(classes.contains(ScanMain.class));
    }
}
