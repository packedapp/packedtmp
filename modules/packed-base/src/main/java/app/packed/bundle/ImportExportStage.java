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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.inject.Provides;
import app.packed.util.Nullable;

/**
 * A shared superclass for {@link InjectorImportStage}, {@link ImportExportStage}, {@link ContainerImportStage} and
 * {@link ContainerExportStage}.
 */

// Kunne vaere rart, hvis man f.eks. kunne
// bindInjector(SomeBundle.class, JaxRSSpec.2_1);
// bindInjector(SomeBundle.class, JettySpec.23_1);

/// Ideen er at JettySpec.23_1 kan vaere + JaxRSSpec.2_1
// ComponentInstanceHook
// AnnotatedComponentTypeHook
// AnnotatedComponentMethodHook

/// activeInstance(Startable.class)
/// activeAnnotatedMethod(onStart.class)...
//// De skal vaere en del af specifikationen
// Activators
// InstanceOfActivator (Component+<T>)

// AnnotatedTypeActivator
// AnnotatedMethodActivator

// ServiceLoader.enabledInvocationOnComponentMethodsAnnotatedWith(xxx.class, ...);

// Interface (maybe ditch it for now) + Description

//
public abstract class ImportExportStage {

    /** A Lookup object. */
    @Nullable
    final MethodHandles.Lookup lookup;

    /** Creates a new stage */
    ImportExportStage() {
        this.lookup = MethodHandles.publicLookup();
    }

    /**
     * Creates a new stage
     * 
     * @param lookup
     *            a lookup object that will be used, for example, for invoking methods annotated with {@link Provides}.
     */
    ImportExportStage(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup, "lookup is null");
    }

    /** Performs cleanup or post processing validation of the stage. The default implementation does nothing. */
    protected void onFinish() {};
}
