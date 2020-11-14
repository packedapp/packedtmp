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
import app.packed.component.Assembler;
import app.packed.component.Assembly;
import app.packed.component.BuildContext;
import app.packed.component.Component;
import app.packed.component.ComponentAnalyzer;
import app.packed.component.ComponentDelegate;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentSubSystem;
import app.packed.component.CustomConfigurator;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import packed.internal.component.source.RealmBuild;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.config.ConfigSiteInjectOperations;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.errorhandling.ErrorMessage;

/** The default implementation of {@link BuildContext} */
public final class PackedBuildContext implements BuildContext {

    /** The build output. */
    final int modifiers;

    ComponentBuild root;

    /** Any shell driver that initiated the build process. */
    @Nullable
    private final PackedShellDriver<?> shellDriver;

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
    private PackedBuildContext(int modifiers, @Nullable PackedShellDriver<?> shellDriver, Wirelet... wirelets) {
        this.modifiers = modifiers + PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
        this.shellDriver = shellDriver;
        this.wirelets = wirelets;
    }

    /**
     * @return comp
     */
    public Component asComponent() {
        return root.adaptToComponent();
    }

    /** {@inheritDoc} */
    @Override
    public void addError(ErrorMessage message) {}

    public boolean isAnalysis() {
        return (modifiers & PackedComponentModifierSet.I_ANALYSIS) != 0;
    }

    public boolean isImage() {
        return (modifiers & PackedComponentModifierSet.I_IMAGE) != 0;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    public PackedInitializationContext process(Wirelet[] imageWirelets) {
        return PackedInitializationContext.process(root, imageWirelets);
    }

    /**
     * Returns any shell driver that initiated the build process.
     * 
     * @return any shell driver that initiated the build process
     */
    @Nullable
    public PackedShellDriver<?> shellDriver() {
        return shellDriver;
    }

    /**
     * Returns the thread that is used for build process.
     * 
     * @return the thread that is used for build process
     */
    public Thread thread() {
        return thread;
    }

    public Wirelet[] wirelets() {
        return wirelets;
    }

    /**
     * Returns a
     * 
     * @param system
     *            the assembly to analyse
     * @return a component adaptor
     * @see ComponentAnalyzer#analyze(app.packed.component.ComponentSubSystem)
     */
    public static Component forAnalysis(ComponentSubSystem system) {
        // PROBLEMET er her at vi ikke kan se om vi er statefull eller stateless
        // Vil mene det er en ret stor forskel...
        // Maaske vi skal til at have det paa driveren...
        // Men saa kan vi jo stadig ikke se det f.eks. med
        // analyze(new DooAssembly());
        // Maaske vi skal explicit angive det naar den er stateless.

        requireNonNull(system, "system is null");
        if (system instanceof Component) {
            return (Component) system;
        } else if (system instanceof ComponentDelegate) {
            return ((ComponentDelegate) system).component();
        } else {
            Assembly<?> assembly = (Assembly<?>) system;
            return build(assembly, false, false, null).asComponent();
        }
    }

    /**
     * Builds a system.
     * 
     * @param assembly
     *            the root bundle
     * @param shellDriver
     *            if the component is to be run in a shell
     * @param wirelets
     *            optional wirelets
     * @return the root component configuration node
     */
    public static PackedBuildContext build(Assembly<?> assembly, boolean isAnalysis, boolean isImage, @Nullable PackedShellDriver<?> shellDriver,
            Wirelet... wirelets) {
        int modifiers = 0;
        if (shellDriver != null) {
            modifiers += PackedComponentModifierSet.I_ANALYSIS;
            if (shellDriver.isStateful()) {
                modifiers += PackedComponentModifierSet.I_CONTAINER;
            }
        } else if (isAnalysis) {
            modifiers += PackedComponentModifierSet.I_ANALYSIS;
        } else { // execute
            modifiers += PackedComponentModifierSet.I_CONTAINER;
        }
        if (isImage) {
            modifiers += PackedComponentModifierSet.I_IMAGE;
        }

        // First we extract the component driver from the assembly
        PackedComponentDriver<?> componentDriver = AssemblyHelper.getDriver(assembly);

        // Create a new build context that we passe around
        PackedBuildContext pac = new PackedBuildContext(modifiers, shellDriver, wirelets);

        WireletPack wp = WireletPack.from(componentDriver, wirelets);

        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);

        // Create the root component
        ComponentBuild compConf = pac.root = new ComponentBuild(pac, new RealmBuild(assembly.getClass()), componentDriver, cs, null, wp);
        Object conf = componentDriver.toConfiguration(compConf);
        AssemblyHelper.configure(assembly, conf); // in-try-finally. So we can call PAC.fail() and have them run callbacks for dynamic nodes

        compConf.close();
        return pac;
    }

    public static <C extends Assembler, D> PackedBuildContext configure(PackedShellDriver<?> ad, PackedComponentDriver<D> driver, Function<D, C> factory,
            CustomConfigurator<C> consumer, Wirelet... wirelets) {
        WireletPack wp = WireletPack.from(driver, wirelets);
        // Vil gerne parse nogle wirelets some det allerfoerste
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);

        PackedBuildContext pac = new PackedBuildContext(0, ad);

        ComponentBuild compConf = pac.root = new ComponentBuild(pac, new RealmBuild(consumer.getClass()), driver, cs, null, wp);

        D conf = driver.toConfiguration(compConf);
        C cc = requireNonNull(factory.apply(conf));
        consumer.configure(cc);

        compConf.close();
        return pac;
    }
}
