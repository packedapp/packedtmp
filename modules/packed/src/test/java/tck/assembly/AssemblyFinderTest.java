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
package tck.assembly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.assembly.AssemblyFinder;
import app.packed.assembly.AssemblyModulepathFinder;
import app.packed.build.BuildException;
import tck.TckAssemblies.EmptyAssembly;

/** Tests for {@link AssemblyFinder} and {@link AssemblyModulepathFinder}. */
public class AssemblyFinderTest {

    // === Classpath Finder Tests ===

    @Test
    public void classpathFinder_findOne_existingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        var assembly = finder.findOne("tck.TckAssemblies$EmptyAssembly");

        assertThat(assembly).isInstanceOf(EmptyAssembly.class);
    }

    @Test
    public void classpathFinder_findOne_nonExistingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        assertThatThrownBy(() -> finder.findOne("com.nonexistent.Assembly"))
            .isInstanceOf(BuildException.class);
    }

    @Test
    public void classpathFinder_findOptional_existingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        var result = finder.findOptional("tck.TckAssemblies$EmptyAssembly");

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(EmptyAssembly.class);
    }

    @Test
    public void classpathFinder_findOptional_nonExistingClass() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        var result = finder.findOptional("com.nonexistent.Assembly");

        assertThat(result).isEmpty();
    }

    @Test
    public void classpathFinder_findOne_notAnAssembly() {
        AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());

        assertThatThrownBy(() -> finder.findOne("java.lang.String"))
            .isInstanceOf(BuildException.class)
            .hasMessageContaining("not an Assembly");
    }

    // === Modulepath Finder Tests ===
    // Note: When running on classpath, modulepath finder uses boot layer which
    // doesn't include test classes. We test what we can.

    @Test
    public void modulepathFinder_findOne_nonExistingClass() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        assertThatThrownBy(() -> finder.findOne("com.nonexistent.Assembly"))
            .isInstanceOf(BuildException.class);
    }

    @Test
    public void modulepathFinder_findOptional_nonExistingClass() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        var result = finder.findOptional("com.nonexistent.Assembly");

        assertThat(result).isEmpty();
    }

    @Test
    public void modulepathFinder_layer_returnsNonNull() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        assertThat(finder.layer()).isNotNull();
        assertThat(finder.parentLayer()).isNotNull();
    }

    @Test
    public void modulepathFinder_availableModules_withoutPaths() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        // Without paths configured, no modules are "available"
        assertThat(finder.availableModules()).isEmpty();
    }

    @Test
    public void modulepathFinder_loadedModules_returnsModulesInLayer() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        // Should return modules in the current layer (boot layer on classpath)
        assertThat(finder.loadedModules()).isNotNull();
    }

    @Test
    public void modulepathFinder_withPaths_returnsNewFinder() {
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());

        var newFinder = finder.withPaths(); // empty paths

        assertThat(newFinder).isSameAs(finder); // no change with empty paths
    }
}
