/*
 * Copyright (c) 2008 Kasper Nielsen.
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
package internal.app.packed.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyMirror;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.PackedContainerInstaller;

/**
 *
 */
public class AssemblyHandlers extends Handlers {

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_ASSEMBLY_MIRROR = constructor(MethodHandles.lookup(), AssemblyMirror.class, AssemblySetup.class);

    public static AssemblyMirror newAssemblyMirror(AssemblySetup assembly) {
        try {
            return (AssemblyMirror) MH_NEW_ASSEMBLY_MIRROR.invokeExact(assembly);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** A handle that can invoke {@link BuildableAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_ASSEMBLY_BUILD =method(MethodHandles.lookup(), Assembly.class, "build", AssemblySetup.class,
            PackedContainerInstaller.class);

    public static AssemblySetup invokeAssemblyBuild(Assembly assembly, PackedContainerInstaller installer) {
        try {
            // Call package private method Assembly#build(PackedContainerBuilder builder)
            return (AssemblySetup) MH_ASSEMBLY_BUILD.invokeExact(assembly, installer);
        } catch (Throwable e) {
            throw throwIt(e);
        }
    }
}
