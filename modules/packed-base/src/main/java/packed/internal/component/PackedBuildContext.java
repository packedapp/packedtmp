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
import app.packed.component.Assembly;
import app.packed.component.BuildInfo;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Composable;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import packed.internal.component.source.RealmBuild;
import packed.internal.component.wirelet.WireletPack;

/** The build context. */
public final class PackedBuildContext implements BuildInfo {

    /** The artifact driver used in the build process. */
    private final PackedArtifactDriver<?> artifactDriver;

    /** The build output. */
    final int modifiers;

    /** The root component, set in build. */
    ComponentBuild root;

    /** The thread that is assembling the system. */
    // We need WeakReference, or some try-final
    // This should not be permanently..
    // What if we create an image in one thread. Passes it to another thread.
    @Nullable
    private Thread thread = Thread.currentThread();

    /**
     * Creates a new build context object.
     * 
     * @param modifiers
     *            the output of the build process
     */
    private PackedBuildContext(PackedArtifactDriver<?> artifactDriver, int modifiers, WireletPack wirelets) {
        this.artifactDriver = artifactDriver;
        this.modifiers = modifiers + PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
    }

    /**
     * Returns the artifact driver that initiated the build process.
     * 
     * @return the artifact driver that initiated the build process
     */
    public PackedArtifactDriver<?> artifactDriver() {
        return artifactDriver;
    }

    /**
     * @return comp
     */
    public Component asComponent() {
        return root.adaptToComponent();
    }

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

    // returnere null if void...
    @Nullable
    public PackedInitializationContext process() {
        return PackedInitializationContext.process(root, null);
    }

    /**
     * Returns the thread that is used for build process.
     * 
     * @return the thread that is used for build process
     */
    public Thread thread() {
        return thread;
    }

    /**
     * @param artifactDriver
     *            the artifact driver used for building the system
     * @param assembly
     *            the root assembly
     * @param wirelets
     *            optional wirelets
     * @param isAnalysis
     *            is it an analysis
     * @param isImage
     *            is it an image
     * @return a build context
     */
    public static PackedBuildContext build(PackedArtifactDriver<?> artifactDriver, Assembly<?> assembly, Wirelet[] wirelets, boolean isAnalysis,
            boolean isImage) {
        // Extract the component driver from the assembly
        PackedComponentDriver<?> componentDriver = AssemblyHelper.getDriver(assembly);

        // Process all wirelets
        WireletPack wp = WireletPack.ofRoot(artifactDriver, componentDriver, wirelets);

        int modifiers = 0;
        if (artifactDriver != null) {
            modifiers += PackedComponentModifierSet.I_ANALYSIS;
            if (artifactDriver.isStateful()) {
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

        // Create a new build context that we passe around
        PackedBuildContext pac = new PackedBuildContext(artifactDriver, modifiers, wp);

        // Create the root component
        ComponentBuild compConf = pac.root = new ComponentBuild(pac, new RealmBuild(assembly.getClass()), componentDriver, null, wp);
        Object conf = componentDriver.toConfiguration(compConf);
        AssemblyHelper.invokeBuild(assembly, conf); // in-try-finally. So we can call PAC.fail() and have them run callbacks for dynamic nodes

        compConf.close();
        return pac;
    }

    public static <C extends Composer<?>, D extends ComponentConfiguration> PackedBuildContext compose(PackedArtifactDriver<?> artifactDriver, PackedComponentDriver<D> componentDriver,
            Function<? super D, ? extends C> factory, Composable<? super C> consumer, Wirelet... wirelets) {
        WireletPack wp = WireletPack.ofRoot(artifactDriver, componentDriver, wirelets);

        PackedBuildContext pac = new PackedBuildContext(artifactDriver, 0, wp);

        ComponentBuild compConf = pac.root = new ComponentBuild(pac, new RealmBuild(consumer.getClass()), componentDriver, null, wp);

        D conf = componentDriver.toConfiguration(compConf);
        C cc = requireNonNull(factory.apply(conf));
        consumer.compose(cc);

        compConf.close();
        return pac;
    }

//    /**
//     * Returns a
//     * 
//     * @param system
//     *            the assembly to analyse
//     * @return a component adaptor
//     */
//    public static Component forAnalysis(ComponentSystem system) {
//        // PROBLEMET er her at vi ikke kan se om vi er statefull eller stateless
//        // Vil mene det er en ret stor forskel...
//        // Maaske vi skal til at have det paa driveren...
//        // Men saa kan vi jo stadig ikke se det f.eks. med
//        // analyze(new DooAssembly());
//        // Maaske vi skal explicit angive det naar den er stateless.
//
//        requireNonNull(system, "system is null");
//        if (system instanceof Component) {
//            return (Component) system;
//        } else if (system instanceof ComponentDelegate) {
//            return ((ComponentDelegate) system).component();
//        } else {
//            Assembly<?> assembly = (Assembly<?>) system;
//            return build(PackedArtifactDriver.DAEMON, assembly, new Wirelet[0], false, false).asComponent();
//        }
//    }
}
