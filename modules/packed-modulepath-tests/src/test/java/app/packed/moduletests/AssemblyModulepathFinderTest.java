/*
 * Copyright (c) 2026 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.moduletests;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.assembly.AssemblyModulepathFinder;
import app.packed.build.BuildException;
import app.packed.moduletests.isopen.SimpleTestAssembly;

/** Tests for {@link AssemblyModulepathFinder} on the modulepath. */
public class AssemblyModulepathFinderTest {

    @Test
    void of_returnsNonNull() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());
        assertNotNull(finder);
    }

    @Test
    void findOne_existingClass() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        var assembly = finder.findOne("app.packed.moduletests.isopen.SimpleTestAssembly");

        assertInstanceOf(SimpleTestAssembly.class, assembly);
    }

    @Test
    void findOne_nonExistingClass() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        assertThrows(BuildException.class, () -> finder.findOne("com.nonexistent.Assembly"));
    }

    @Test
    void findOptional_existingClass() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        var result = finder.findOptional("app.packed.moduletests.isopen.SimpleTestAssembly");

        assertTrue(result.isPresent());
        assertInstanceOf(SimpleTestAssembly.class, result.get());
    }

    @Test
    void findOptional_nonExistingClass() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        var result = finder.findOptional("com.nonexistent.Assembly");

        assertTrue(result.isEmpty());
    }

    @Test
    void layer_returnsCurrentModuleLayer() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        assertNotNull(finder.layer());
        // Should include our test module
        assertTrue(finder.loadedModules().contains("app.packed.modulepath.tests"));
    }

    @Test
    void parentLayer_returnsNonNull() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        assertNotNull(finder.parentLayer());
    }

    @Test
    void availableModules_withoutPaths_isEmpty() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        // Without paths configured, no external modules are available
        assertTrue(finder.availableModules().isEmpty());
    }

    @Test
    void loadedModules_containsAppPacked() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        // Should have app.packed in the module graph
        assertTrue(finder.loadedModules().contains("app.packed"));
    }
}
