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

import java.util.function.Consumer;

import app.packed.application.BuildInfo;
import app.packed.component.Assembly;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.SlotTableSetup;
import packed.internal.component.WireletWrapper;

/**
 * A setup class for a build.
 * 
 */
public final class BuildSetup implements BuildInfo {

    /** The artifact driver used for the build process. */
    private final PackedApplicationDriver<?> applicationDriver;

    public final SlotTableSetup slotTable = new SlotTableSetup();
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

    /**
     * Creates a new build setup from an assembly.
     * 
     * @param modifiers
     *            the output of the build process
     */
    BuildSetup(PackedApplicationDriver<?> applicationDriver, Assembly<?> assembly, PackedComponentDriver<?> driver, boolean isImage, Wirelet[] wirelets) {
        this.applicationDriver = applicationDriver;

        // Vi flytter wirelets'ene ind i ComponentSetup med mindre vi vil extracte info her i BuildSetup omkring dem
        // Men det ser vi paa et senere tidspunkt
        WireletWrapper ww = WireletWrapper.forApplication(applicationDriver, driver, wirelets);

        int tmpM = 0;

        tmpM += PackedComponentModifierSet.I_ANALYSIS;
        if (applicationDriver.isStateful()) {
            tmpM += PackedComponentModifierSet.I_CONTAINER;
        }

        if (isImage) {
            tmpM += PackedComponentModifierSet.I_IMAGE;
        }

        this.modifiers = tmpM + PackedComponentModifierSet.I_APPLICATION + PackedComponentModifierSet.I_BUILD;
        this.component = new ComponentSetup(this, new RealmSetup(assembly), driver, null, ww);
    }

    /**
     * Creates a new build setup for a composer.
     * 
     * @param modifiers
     *            the output of the build process
     */
    BuildSetup(PackedApplicationDriver<?> applicationDriver, Consumer<?> consumer, PackedComponentDriver<?> componentDriver, Wirelet[] wirelets) {
        this.applicationDriver = applicationDriver;
        this.modifiers = PackedComponentModifierSet.I_APPLICATION + PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
        WireletWrapper ww = WireletWrapper.forApplication(applicationDriver, componentDriver, wirelets);
        this.component = new ComponentSetup(this, new RealmSetup(consumer), componentDriver, null, ww);
    }

    /** {@return the root application driver} */
    public PackedApplicationDriver<?> applicationDriver() {
        return applicationDriver;
    }

    void close() {
        component.realmClose();
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
}
// Build setup does not maintain what thread is building the system.
// If we want to have dynamically recomposable systems...