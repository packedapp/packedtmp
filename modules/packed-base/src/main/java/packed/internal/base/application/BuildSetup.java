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
package packed.internal.base.application;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.BuildInfo;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireletWrapper;
import packed.internal.util.ThrowableUtil;

/**
 * A setup class for a build.
 * 
 */
public final class BuildSetup implements BuildInfo {

    /** The artifact driver used for the build process. */
    private final PackedApplicationDriver<?> applicationDriver;

    /** The root component. */
    final ComponentSetup component;

    /** The build output. */
    public final int modifiers;

    // Ideen er at vi validere per built... F.eks Foo bruger @Inject paa et field... // Assembly = sdd, Source = DDD,
    // ruleBroken = FFF
    // Man kan kun validere assemblies...
    // Maaske er det exposed paa BuildInfo...
    // Giver det mening at returnere en component hvis det er fejlet??? InjectionGraph er det eneste jeg kan taenke...
    Object validationErrors;

    /** {@return the root application driver} */
    public PackedApplicationDriver<?> applicationDriver() {
        return applicationDriver;
    }

    public boolean isAnalysis() {
        return (modifiers & PackedComponentModifierSet.I_ANALYSIS) != 0;
    }

    /** {@return whether or not we are creating the root application is part of an image}. */
    public boolean isImage() {
        return (modifiers & PackedComponentModifierSet.I_IMAGE) != 0;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    /**
     * Creates a new build setup.
     * 
     * @param modifiers
     *            the output of the build process
     */
    private BuildSetup(PackedApplicationDriver<?> applicationDriver, Consumer<?> consumer, PackedComponentDriver<?> driver, Wirelet[] wirelets) {
        this.applicationDriver = applicationDriver;
        this.modifiers = PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
        WireletWrapper ww = WireletWrapper.forApplication(applicationDriver, driver, wirelets);
        this.component = new ComponentSetup(this, new RealmSetup(consumer), driver, null, ww);
    }

    /**
     * Creates a new build setup.
     * 
     * @param modifiers
     *            the output of the build process
     */
    private BuildSetup(PackedApplicationDriver<?> applicationDriver, Assembly<?> assembly, PackedComponentDriver<?> driver, @Nullable WireletWrapper wirelets,
            boolean isImage) {
        this.applicationDriver = applicationDriver;

        int tmpM = 0;

        if (driver != null) {
            tmpM += PackedComponentModifierSet.I_ANALYSIS;
            if (applicationDriver.isStateful()) {
                tmpM += PackedComponentModifierSet.I_CONTAINER;
            }
        }

        if (isImage) {
            tmpM += PackedComponentModifierSet.I_IMAGE;
        }

        this.modifiers = tmpM + PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
        this.component = new ComponentSetup(this, new RealmSetup(assembly), driver, null, wirelets);
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
        PackedComponentDriver<?> componentDriver = PackedComponentDriver.getDriver(assembly);

        // Process all wirelets
        WireletWrapper wp = WireletWrapper.forApplication(driver, componentDriver, wirelets);

        // Create a new build setup
        BuildSetup build = new BuildSetup(driver, assembly, componentDriver, wp, isImage);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = componentDriver.toConfiguration(build.component);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            PackedComponentDriver.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        build.component.realmClose();
        return build;
    }

    static <CO extends Composer<?>, CC extends ComponentConfiguration> BuildSetup buildFromComposer(PackedApplicationDriver<?> driver,
            PackedComponentDriver<CC> componentDriver, Function<? super CC, ? extends CO> composerFactory, Consumer<? super CO> consumer, Wirelet... wirelets) {
        BuildSetup build = new BuildSetup(driver, consumer, componentDriver, wirelets);

        CC componentConfiguration = componentDriver.toConfiguration(build.component);

        // Used the supplied composer factory to create a composer from a component configuration instance
        CO composer = requireNonNull(composerFactory.apply(componentConfiguration), "composerFactory.apply() returned null");

        // Invoked the consumer supplied by the end-user
        consumer.accept(composer);

        build.component.realmClose();
        return build;
    }
}
// Build setup does not maintain what thread is building the system.
// If we want to have dynamically recomposable systems...