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
package packed.internal.application;

import app.packed.application.Build;
import app.packed.application.BuildTarget;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireableComponentDriver;
import packed.internal.container.ContainerSetup;

/** The configuration of a build. */
public final class BuildSetup implements Build {

    /** The root component. */
    final ContainerSetup component;

    /** Modifiers of the build. */
    // Hmm hvad er disse i forhold til component modifiers???
    public final int modifiers;

    // Ideen er at vi validere per built... F.eks Foo bruger @Inject paa et field... // Assembly = sdd, Source = DDD,
    // ruleBroken = FFF
    // Man kan kun validere assemblies...
    // Maaske er det exposed paa BuildInfo...
    // Giver det mening at returnere en component hvis det er fejlet??? InjectionGraph er det eneste jeg kan taenke...
    // Object validationErrors;

    /**
     * Creates a new build setup.
     * 
     * @param modifiers
     *            the output of the build process
     */
    BuildSetup(PackedApplicationDriver<?> applicationDriver, RealmSetup realm, WireableComponentDriver<?> componentDriver, int modifiers, Wirelet[] wirelets) {
        this.modifiers = PackedComponentModifierSet.I_BUILD + applicationDriver.modifiers + componentDriver.modifiers + modifiers;
        this.component = (ContainerSetup) componentDriver.newComponent(this, new ApplicationSetup(applicationDriver, componentDriver, modifiers), realm, null,
                wirelets);
        realm.wireCommit(component, false);
    }

    /** {@return whether or not we are creating the root application is part of an image}. */
    public boolean isImage() {
        return PackedComponentModifierSet.isSet(modifiers, ComponentModifier.IMAGE);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    @Override
    public BuildTarget target() {
        if (PackedComponentModifierSet.isImage(modifiers)) {
            return BuildTarget.IMAGE;
        }
        return PackedComponentModifierSet.isAnalysis(modifiers) ? BuildTarget.ANALYSIS : BuildTarget.INSTANCE;
    }

    @Override
    public boolean isFailed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSuccess() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        return false;
    }
}
// Build setup does not maintain what thread is building the system.
// If we want to have dynamically recomposable systems...