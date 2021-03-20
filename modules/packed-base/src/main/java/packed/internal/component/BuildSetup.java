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

import app.packed.base.Nullable;
import app.packed.component.BuildInfo;
import app.packed.component.Component;
import app.packed.component.ComponentModifierSet;

/** A setup class for a build. */
public final class BuildSetup implements BuildInfo {

    /** The artifact driver used for the build process. */
    private final PackedApplicationDriver<?> artifactDriver;

    /** The build output. */
    final int modifiers;

    /** The root component, set in build. */
    ComponentSetup rootComponent;

    /** The thread that is assembling the system. */
    // We need WeakReference, or some try-final
    // This should not be permanently..
    // What if we create an image in one thread. Passes it to another thread.
    @Nullable
    private Thread thread = Thread.currentThread();

    /**
     * Creates a new build setup.
     * 
     * @param modifiers
     *            the output of the build process
     */
    BuildSetup(PackedApplicationDriver<?> artifactDriver, int modifiers) {
        this.artifactDriver = artifactDriver;
        this.modifiers = modifiers + PackedComponentModifierSet.I_BUILD; // we use + to make sure others don't provide ASSEMBLY
    }

    /**
     * Returns the artifact driver that initiated the build process.
     * 
     * @return the artifact driver that initiated the build process
     */
    public PackedApplicationDriver<?> artifactDriver() {
        return artifactDriver;
    }

    /**
     * @return comp
     */
    public Component asComponent() {
        return rootComponent.adaptToComponent();
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
        return PackedInitializationContext.process(rootComponent, null);
    }

    /**
     * Returns the thread that is used for build process.
     * 
     * @return the thread that is used for build process
     */
    public Thread thread() {
        return thread;
    }
}
