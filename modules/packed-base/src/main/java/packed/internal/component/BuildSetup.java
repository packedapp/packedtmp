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

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.BuildInfo;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Composer;
import app.packed.component.Wirelet;

/** A setup class for a build. */
public final class BuildSetup implements BuildInfo {

    /** The artifact driver used for the build process. */
    private final PackedApplicationDriver<?> artifactDriver;

    /** The root component. */
    final ComponentSetup component;

    /** The build output. */
    final int modifiers;

    /**
     * Creates a new build setup.
     * 
     * @param modifiers
     *            the output of the build process
     */
    private BuildSetup(PackedApplicationDriver<?> artifactDriver, int modifiers, RealmSetup realm, PackedComponentDriver<?> driver,
            @Nullable WireletWrapper wirelets) {
        this.artifactDriver = artifactDriver;
        this.modifiers = modifiers + PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
        this.component = new ComponentSetup(this, realm, driver, null, wirelets);
    }

    /**
     * Returns the artifact driver that initiated the build process.
     * 
     * @return the artifact driver that initiated the build process
     */
    public PackedApplicationDriver<?> artifactDriver() {
        return artifactDriver;
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
        return PackedInitializationContext.process(component, null);
    }

    /**
     * @param assembly
     *            the root assembly
     * @param wirelets
     *            optional wirelets
     * @param isAnalysis
     *            is it an analysis
     * @param isImage
     *            is it an image
     * @return a build setup
     */
    static BuildSetup buildFromAssembly(PackedApplicationDriver<?> driver, Assembly<?> assembly, Wirelet[] wirelets, boolean isAnalysis, boolean isImage) {
        // Extract the component driver from the assembly
        PackedComponentDriver<?> componentDriver = AssemblyHelper.getDriver(assembly);

        // Process all wirelets
        WireletWrapper wp = WireletWrapper.forApplication(driver, componentDriver, wirelets);

        int modifiers = 0;

        if (driver != null) {
            modifiers += PackedComponentModifierSet.I_ANALYSIS;
            if (driver.isStateful()) {
                modifiers += PackedComponentModifierSet.I_CONTAINER;
            }
        }

        if (isImage) {
            modifiers += PackedComponentModifierSet.I_IMAGE;
        }

        // Create a new build context that we passe around
        BuildSetup build = new BuildSetup(driver, modifiers, new RealmSetup(assembly), componentDriver, wp);

        // Invoke Assembly.build()
        AssemblyHelper.invokeBuild(assembly, componentDriver.toConfiguration(build.component));

        build.component.close();

        return build;
    }

    static <CO extends Composer<?>, CC extends ComponentConfiguration> BuildSetup buildFromComposer(PackedApplicationDriver<?> driver,
            PackedComponentDriver<CC> componentDriver, Function<? super CC, ? extends CO> composerFactory, Consumer<? super CO> consumer, Wirelet... wirelets) {
        WireletWrapper wp = WireletWrapper.forApplication(driver, componentDriver, wirelets);

        BuildSetup build = new BuildSetup(driver, 0, new RealmSetup(consumer), componentDriver, wp);

        CC componentConfiguration = componentDriver.toConfiguration(build.component);

        // Used the supplied composer factory to create a composer from a component configuration instance
        CO composer = requireNonNull(composerFactory.apply(componentConfiguration), "composerFactory.apply() returned null");

        // Invoked the consumer supplied by the end-user
        consumer.accept(composer);

        build.component.close();
        return build;
    }
}
// Build setup does not maintain what thread is building the system.
// If we want to have dynamically recomposable systems...