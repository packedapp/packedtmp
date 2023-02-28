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
package app.packed.container;

import java.util.ArrayList;
import java.util.List;

import app.packed.application.BuildException;
import internal.app.packed.container.AbstractContainerBuilder;
import internal.app.packed.container.AssemblyModel;
import internal.app.packed.container.AssemblySetup;

/**
 * A special assembly type that delegates all calls to another assembly.
 * <p>
 * Some typical use cases for delegating assembly are: TODO
 * <ul>
 * <li>Hide methods on an original assembly.</li>
 * <li>Custom configuration of an existing assembly, for example, in test scanerioys specified .</li>
 * <li>Finally, {@link ContainerConfiguration#USED} is set to indicate that the composer has been used.</li>
 * </ul>
 * <p>
 * Delegating assemblies cannot use the {@link AssemblyHook} annotation or {@link BeanHook custom bean hooks}. They must
 * always be placed on the assembly being delegated to instead. Failure to do so will result in a {@link BuildException}
 * being thrown.
 * <p>
 * Delegating assemblies are never reported from {@link AssemblyMirror#assemblyClass()}, instead the assembly that was
 * delegated to is reported. The delegating assembly can be obtained from {@link AssemblyMirror#delegatedFrom()}.
 */
public non-sealed abstract class DelegatingAssembly extends Assembly {

    /** {@return the assembly to delegate to.} */
    protected abstract Assembly delegateTo();

    @Override
    AssemblySetup build(AbstractContainerBuilder containerBuilder) {
        // Check that this assembly does not use AssemblyHooks
        AssemblyModel.of(getClass());

        ArrayList<Class<? extends DelegatingAssembly>> delegatingAssemblies = containerBuilder.delegatingAssemblies;
        if (delegatingAssemblies == null) {
            delegatingAssemblies = containerBuilder.delegatingAssemblies = new ArrayList<>(1);
        } else if (delegatingAssemblies.size() == 100) {
            // Maybe stack overflow is fine
            throw new BuildException(
                    "Inifite loop suspected, cannot have more than 100 delegating assemblies, assemblyClass = " + getClass().getCanonicalName());
        }

        Assembly a = delegateTo();
        if (a == null) {
            throw new BuildException(
                    "Delagating assembly: " + getClass() + " cannot return null from " + DelegatingAssembly.class.getSimpleName() + "::delegateTo");
        }
        delegatingAssemblies.add(getClass());

        // Process the assembly that was delegated to
        return a.build(containerBuilder);
    }

    /**
     * Constructs a delegating assembly that will prefix all usage of the specified assembly with specified wirelets
     *
     * @param assembly
     *            the assembly to add wirelets to
     * @param wirelets
     *            the wirelets to add when using the delegated assembly
     * @return the delegating assembly
     */
    // Maybe on Wirelet?
    public static Assembly wireWith(Assembly assembly, Wirelet... wirelets) {
        return new WireletDelegatingAssembly(assembly, wirelets);
    }

    private static class WireletDelegatingAssembly extends DelegatingAssembly {
        private final Assembly assembly;
        private final List<Wirelet> wirelets;

        private WireletDelegatingAssembly(Assembly assembly, Wirelet[] wirelets) {
            this.assembly = assembly;
            this.wirelets = List.of(wirelets);
        }

        /** {@inheritDoc} */
        @Override
        protected Assembly delegateTo() {
            return assembly;
        }

        @Override
        AssemblySetup build(AbstractContainerBuilder containerBuilder) {
            for (Wirelet w : wirelets) {
                containerBuilder.processWirelet(w);
            }
            return assembly.build(containerBuilder);
        }
    }
}
