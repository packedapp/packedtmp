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
package packed.internal.lifecycle.phases;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Function;

import app.packed.artifact.ShellDriver;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentModifier;
import app.packed.component.CustomConfigurator;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.lifecycle.AssemblyContext;
import packed.internal.component.BundleConfiguration;
import packed.internal.component.ComponentModifierSet;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedWireableComponentDriver;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.PackedRealm;
import packed.internal.errorhandling.ErrorMessage;
import packed.internal.inject.ConfigSiteInjectOperations;

/** The default implementation of {@link AssemblyContext} */
public final class PackedAssemblyContext implements AssemblyContext {

    /** The build output. */
    final int modifiers;

    /** The thread that is assembling the system. */
    // This should not be permanently..
    // What if we create an image in one thread. Passes it to another thread.

    private final Thread thread = Thread.currentThread();

    // Bruges ikke lige endnu. Ved heller ikke om vi har lyst til at gemme dem permanent...
    private final Wirelet[] wirelets;

    /**
     * Creates a new build context object.
     * 
     * @param modifiers
     *            the output of the build process
     */
    PackedAssemblyContext(int modifiers, Wirelet... wirelets) {
        this.modifiers = modifiers;
        this.wirelets = wirelets;

    }

    /** {@inheritDoc} */
    @Override
    public void addError(ErrorMessage message) {}

    /** {@inheritDoc} */
    @Override
    public Set<ComponentModifier> modifiers() {
        return new ComponentModifierSet(modifiers);
    }

    /**
     * Returns the build output.
     * 
     * @return the build output
     */
    public Thread thread() {
        return thread;
    }

    public Wirelet[] wirelets() {
        return wirelets;
    }

    public static ComponentNodeConfiguration assemble(Bundle<?> bundle, int modifiers, @Nullable ShellDriver<?> shellDriver, Wirelet... wirelets) {
        PackedAssemblyContext assembly = new PackedAssemblyContext(modifiers);
        PackedWireableComponentDriver<?> driver = BundleConfiguration.driverOf(bundle);
        WireletPack wp = WireletPack.from(driver, wirelets);

        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);
        ComponentNodeConfiguration node = ComponentNodeConfiguration.newAssembly(assembly, driver, cs, PackedRealm.fromBundle(bundle), wp);

        Object conf = driver.toConfiguration(node);
        BundleConfiguration.configure(bundle, conf); // in-try-finally. So we can call PAC.fail() and have them run callbacks for dynamic nodes

        return node.closeAssembly();
    }

    public static <C, D> ComponentNodeConfiguration configure(ShellDriver<?> ad, PackedWireableComponentDriver<D> driver, Function<D, C> factory,
            CustomConfigurator<C> consumer, Wirelet... wirelets) {
        WireletPack wp = WireletPack.from(driver, wirelets);
        // Vil gerne parse nogle wirelets some det allerfoerste
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);

        ComponentNodeConfiguration node = ComponentNodeConfiguration.newAssembly(new PackedAssemblyContext(0), driver, cs,
                PackedRealm.fromConfigurator(consumer), wp);

        D conf = driver.toConfiguration(node);
        C cc = requireNonNull(factory.apply(conf));
        consumer.configure(cc);

        return node.closeAssembly();
    }
}
