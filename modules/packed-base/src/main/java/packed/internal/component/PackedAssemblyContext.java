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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.component.AssemblyContext;
import app.packed.component.Bundle;
import app.packed.component.ComponentModifierSet;
import app.packed.component.CustomConfigurator;
import app.packed.component.ShellDriver;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.PackedRealm;
import packed.internal.errorhandling.ErrorMessage;
import packed.internal.inject.ConfigSiteInjectOperations;

/** The default implementation of {@link AssemblyContext} */
public final class PackedAssemblyContext implements AssemblyContext {

    /** The build output. */
    final int modifiers;

    @Nullable
    private final ShellDriver<?> shellDriver;

    /** The thread that is assembling the system. */
    // This should not be permanently..
    // What if we create an image in one thread. Passes it to another thread.

    // Skal bruges
    private final Thread thread = Thread.currentThread();

    // Bruges ikke lige endnu. Ved heller ikke om vi har lyst til at gemme dem permanent...
    private final Wirelet[] wirelets;

    /**
     * Creates a new build context object.
     * 
     * @param modifiers
     *            the output of the build process
     */
    PackedAssemblyContext(int modifiers, @Nullable ShellDriver<?> shellDriver, Wirelet... wirelets) {
        this.modifiers = modifiers + PackedComponentModifierSet.I_ASSEMBLY; // we use + to make sure others don't provide ASSEMBLY
        this.shellDriver = shellDriver;
        this.wirelets = wirelets;
    }

    /** {@inheritDoc} */
    @Override
    public void addError(ErrorMessage message) {}

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    @Nullable
    public ShellDriver<?> shellDriver() {
        return shellDriver;
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

        // First we extract the component driver from the bundle
        PackedComponentDriver<?> componentDriver = BundleHelper.getDriver(bundle);

        // Create a new assembly context that we passe around
        PackedAssemblyContext assembly = new PackedAssemblyContext(modifiers, shellDriver, wirelets);

        WireletPack wp = WireletPack.from(componentDriver, wirelets);

        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);
        ComponentNodeConfiguration node = ComponentNodeConfiguration.newAssembly(assembly, componentDriver, cs, PackedRealm.fromBundle(bundle), wp);

        Object conf = componentDriver.toConfiguration(node);
        BundleHelper.configure(bundle, conf); // in-try-finally. So we can call PAC.fail() and have them run callbacks for dynamic nodes

        return node.assembledSuccesfully();
    }

    public static <C, D> ComponentNodeConfiguration configure(ShellDriver<?> ad, PackedComponentDriver<D> driver, Function<D, C> factory,
            CustomConfigurator<C> consumer, Wirelet... wirelets) {
        WireletPack wp = WireletPack.from(driver, wirelets);
        // Vil gerne parse nogle wirelets some det allerfoerste
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);

        ComponentNodeConfiguration node = ComponentNodeConfiguration.newAssembly(new PackedAssemblyContext(0, ad), driver, cs,
                PackedRealm.fromConfigurator(consumer), wp);

        D conf = driver.toConfiguration(node);
        C cc = requireNonNull(factory.apply(conf));
        consumer.configure(cc);

        return node.assembledSuccesfully();
    }
}
