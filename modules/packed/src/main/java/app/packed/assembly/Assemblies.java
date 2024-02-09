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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Consumer;

import app.packed.build.BuildTransformer;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.PackedContainerBuilder;

/**
 *
 */
public final class Assemblies {

    private Assemblies() {}

    public static void main(Assembly a) {
        verifiable(a, ContainerMirror.class, t -> System.out.println(t.componentPath() + " Number of beans = " + t.beans().count()), AssemblyPropagator.ALL);
    }

    /**
     * @param caller
     * @param assembly
     * @return
     * @throws IllegalArgumentException
     *             if the specified assembly is in use or has already been used
     */
    // Writes
    // We don't actually transform anything, just returns a new assembly that will do it
    // For assemblies, we do not need the lookup caller
    public static Assembly transform(MethodHandles.Lookup caller, Assembly assembly, BuildTransformer... transformers) {
        return assembly;
    }

    public static Assembly transform(MethodHandles.Lookup caller, Assembly assembly, AssemblyPropagator ap, BuildTransformer... transformer) {
        return assembly;
    }

    // Read only

    // This could just be a wirelet...If it is readable...
    // Wirelet.verify(Class<? extends T> mirrorType, Consumer<T> verifier) <--- Applies to the whole Assembly
    // Wirelet.verify(Class<? extends T> mirrorType, Consumer<T> verifier, AssemblyPropagator ap)
    public static <T extends ComponentMirror> Assembly verifiable(Assembly assembly, Class<? extends T> mirrorType, Consumer<T> verifier,
            AssemblyPropagator ap) {
        return assembly;
    }

    // We probably want some steps after having run the verifiers.
    // For example, dump some structures to the console
    public static <T extends ComponentMirror> Assembly verifiable(Assembly assembly, Class<T> mirrorType, Consumer<? super T> verifier) {
        return assembly;
    }

    /**
     * Constructs a delegating assembly that will prefix all usage of the specified assembly with the specified wirelets
     *
     * @param assembly
     *            the assembly to add wirelets to
     * @param wirelets
     *            the wirelets to add when using the delegated assembly
     * @return the delegating assembly
     */
    public static Assembly wireWith(Assembly assembly, Wirelet... wirelets) {
        return new WireletPrefixDelegatingAssembly(assembly, wirelets);
    }

    /** A delegating assembly that allows to prefix wirelets. */
    private static class WireletPrefixDelegatingAssembly extends DelegatingAssembly {

        /** The assembly to delegate to. */
        private final Assembly assembly;

        /** Wirelets that should be processed. */
        private final Wirelet[] wirelets;

        private WireletPrefixDelegatingAssembly(Assembly assembly, Wirelet[] wirelets) {
            this.assembly = requireNonNull(assembly);
            this.wirelets = List.of(wirelets).toArray(i -> new Wirelet[i]); // checks for null
        }

        /** {@inheritDoc} */
        @Override
        AssemblySetup build(PackedContainerBuilder containerBuilder) {
            containerBuilder.processBuildWirelets(wirelets);
            return assembly.build(containerBuilder);
        }

        /** {@inheritDoc} */
        @Override
        protected Assembly delegateTo() {
            return assembly;
        }

        /** {@inheritDoc} */
        @Override
        Assembly extractAssembly(PackedContainerBuilder containerBuilder) {
            containerBuilder.processBuildWirelets(wirelets);
            return super.extractAssembly(containerBuilder);
        }
    }
}
