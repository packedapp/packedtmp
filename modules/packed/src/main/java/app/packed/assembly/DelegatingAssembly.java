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
package app.packed.assembly;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.build.BuildException;
import app.packed.build.BuildTransformer;
import app.packed.util.Nullable;
import internal.app.packed.container.AssemblyModel;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.PackedContainerBuilder;

/**
 * A special assembly type that delegates all calls to another assembly.
 * <p>
 * Some typical use cases for delegating assembly are:
 * <ul>
 * <li>Hide methods on an original assembly.</li>
 * <li>Custom configuration of an existing assembly, for example, in test scanerioys specified .</li>
 * </ul>
 * <p>
 * Delegating assemblies cannot use the {@link AssemblyHook} annotation or {@link BeanHook custom bean hooks}. They must
 * always be placed on the assembly being delegated to. Failure to follow this rule will result in a
 * {@link BuildException} being thrown.
 * <p>
 * Delegating assemblies are never reported from {@link AssemblyMirror#assemblyClass()}, instead the assembly that was
 * delegated to is reported. The delegating assembly(s) can be obtained by calling
 * {@link AssemblyMirror#delegatedFrom()}, which lists any delegating assemblies in order.
 */
public non-sealed abstract class DelegatingAssembly extends Assembly {

    /** {@inheritDoc} */
    @Override
    AssemblySetup build(PackedContainerBuilder containerBuilder) {
        AssemblyModel.of(getClass()); // Check that this assembly does not use AssemblyHooks

        // Problem with relying on StackOverflowException is that you cannot really see what assembly
        // is causing the problems
        // Honestly, maybe just say we cannot multiple assemblies of the same type.
        // But then again we could multiple wirelet delegating assemblies
        if (containerBuilder.delegatingAssemblies.size() == 99) {
            throw new BuildException("Inifite loop suspected, cannot have more than " + containerBuilder.delegatingAssemblies.size()
                    + " delegating assemblies, assemblyClass = " + getClass().getCanonicalName());
        }

        Assembly assembly = delegateTo();
        if (assembly == null) {
            throw new BuildException(
                    "Delagating assembly: " + getClass() + " cannot return null from " + DelegatingAssembly.class.getSimpleName() + "::delegateTo");
        } else if (assembly == this) {
            throw new BuildException("Delegating assembly: " + getClass() + " cannot return this");
        }

        // Add this assembly to the list of delegating assemblies
        containerBuilder.delegatingAssemblies.add(getClass());

        // Process the assembly that was delegated to
        return assembly.build(containerBuilder);
    }

    /** {@return the assembly to delegate to.} */
    // If the returned assembly has Transformers. We must have access via lookup
    // If you want to apply a transformation to an assembly. You must have open access
    protected abstract Assembly delegateTo();

    Assembly extractAssembly(PackedContainerBuilder containerBuilder) {
        AssemblyModel.of(getClass()); // Check that this assembly does not use AssemblyHooks

        // Problem with relying on StackOverflowException is that you cannot really what assembly
        // is causing the problems
        if (containerBuilder.delegatingAssemblies.size() == 99) {
            throw new BuildException("Inifite loop suspected, cannot have more than " + containerBuilder.delegatingAssemblies.size()
                    + " delegating assemblies, assemblyClass = " + getClass().getCanonicalName());
        }

        Assembly assembly = delegateTo();
        if (assembly == null) {
            throw new BuildException(
                    "Delagating assembly: " + getClass() + " cannot return null from " + DelegatingAssembly.class.getSimpleName() + "::delegateTo");
        }

        // Add this assembly to the list of delegating assemblies
        containerBuilder.delegatingAssemblies.add(getClass());

        if (assembly instanceof DelegatingAssembly da) {
            return da.extractAssembly(containerBuilder);
        }
        return assembly;
    }

    // Do we need to be dynamic here???
    // IDeen er lidt at at fx for AssemblyHook er det interesant.
    /// Fx InModule er det delegating assembly eller target assembly
    protected boolean isSynthetic() {
        return false;
    }


    protected @Nullable Lookup lookup() {
        return null;
    }

    protected final Assembly transform(Assembly assembly, BuildTransformer... transformers) {
        return transformRecursively(assembly, AssemblyPropagator.LOCAL, transformers);
    }

    protected final Assembly transformRecursively(Assembly assembly, AssemblyPropagator propagator, BuildTransformer... transformers) {
        // uses lookup if available, otherwise .getClass()
        throw new UnsupportedOperationException();
    }

}
//// Can call build in the specified assembly, to complicated with hooks probably
//protected final Runnable callBuildRunnable(Assembly assembly) {
//    // uses lookup if available, otherwise .getClass()
//    throw new UnsupportedOperationException();
//}
//
