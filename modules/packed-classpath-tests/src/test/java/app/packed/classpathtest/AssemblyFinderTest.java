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
package app.packed.classpathtest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import app.packed.assembly.AssemblyFinder;
import app.packed.build.BuildException;

/** Tests for {@link AssemblyFinder} on the classpath. */
public class AssemblyFinderTest {

    @Test
    void ofClasspath_returnsNonNull() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());
        assertNotNull(finder);
    }

    @Test
    void findOne_existingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        var assembly = finder.findOne("app.packed.classpathtest.TestAssembly");

        assertInstanceOf(TestAssembly.class, assembly);
    }

    @Test
    void findOne_nonExistingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        assertThrows(BuildException.class, () -> finder.findOne("com.nonexistent.Assembly"));
    }

    @Test
    void findOne_notAnAssembly() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        BuildException e = assertThrows(BuildException.class, () -> finder.findOne("java.lang.String"));
        assertTrue(e.getMessage().contains("not an Assembly"));
    }

    @Test
    void findOptional_existingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        var result = finder.findOptional("app.packed.classpathtest.TestAssembly");

        assertTrue(result.isPresent());
        assertInstanceOf(TestAssembly.class, result.get());
    }

    @Test
    void findOptional_nonExistingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        var result = finder.findOptional("com.nonexistent.Assembly");

        assertTrue(result.isEmpty());
    }

    @Test
    void findOptional_notAnAssembly() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        // Should throw, not return empty, because the class exists but isn't an Assembly
        assertThrows(BuildException.class, () -> finder.findOptional("java.lang.String"));
    }
}
