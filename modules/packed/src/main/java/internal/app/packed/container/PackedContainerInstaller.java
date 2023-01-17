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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtensionPoint.ContainerInstaller;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public class PackedContainerInstaller implements ContainerInstaller {

    /** A handle that can invoke {@link ComposerAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_NEW_CONTAINER_HANDLE = LookupUtil.findConstructor(MethodHandles.lookup(), ContainerHandle.class,
            ContainerSetup.class);

    final ContainerSetup parent;

    public PackedContainerInstaller(ContainerSetup parent) {
        this.parent = requireNonNull(parent);
    }

    @Override
    public ContainerInstaller allowRuntimeWirelets() {
        throw new UnsupportedOperationException();
    }

    private ContainerHandle from(ContainerSetup bs) {
        try {
            return (ContainerHandle) MH_NEW_CONTAINER_HANDLE.invokeExact(bs);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    @Override
    public ContainerHandle link(Assembly assembly, Wirelet... wirelets) {
        // Check that the assembly is still configurable
        // parent.isConfigurable();

        // Create a new assembly
        AssemblySetup as = new AssemblySetup(null, null, parent, assembly, wirelets);

        // Build the assembly
        as.build();

        return from(as.container);
    }

    @Override
    public ContainerHandle newContainer(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContainerInstaller newLifetime() {
        throw new UnsupportedOperationException();
    }
}
