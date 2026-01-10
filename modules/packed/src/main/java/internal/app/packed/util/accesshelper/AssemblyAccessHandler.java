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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyMirror;
import app.packed.util.Nullable;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.PackedContainerInstaller;

/**
 * Access helper for Assembly and related classes.
 */
public abstract class AssemblyAccessHandler extends AccessHelper {

    private static final Supplier<AssemblyAccessHandler> CONSTANT = StableValue.supplier(() -> init(AssemblyAccessHandler.class, Assembly.class));

    public static AssemblyAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Creates a new AssemblyMirror from an AssemblySetup.
     *
     * @param assembly the assembly setup
     * @return the assembly mirror
     */
    public abstract AssemblyMirror newAssemblyMirror(AssemblySetup assembly);

    /**
     * Invokes the protected build method on an Assembly.
     *
     * @param assembly the assembly
     * @param applicationInstaller the application installer (nullable)
     * @param installer the container installer
     * @return the assembly setup
     */
    public abstract AssemblySetup invokeAssemblyBuild(Assembly assembly, @Nullable PackedApplicationInstaller<?> applicationInstaller,
            PackedContainerInstaller<?> installer);
}
