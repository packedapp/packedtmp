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
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.Injector;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * A shared superclass for {@link InjectorImportStage}, {@link ImportExportStage}, {@link ContainerImportStage} and
 * {@link ContainerExportStage}. It is not possible to extend this class outside of this package.
 */

// Kunne vaere rart, hvis man f.eks. kunne
// bindInjector(SomeBundle.class, JaxRSSpec.2_1.strict());
// bindInjector(SomeBundle.class, JettySpec.23_1.strict());

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

// BundleActivationStage
public abstract class ImportExportStage {

    static {
        BundleSupport.Helper.init(new BundleSupport.Helper() {

            /** {@inheritDoc} */
            @Override
            public void configureInjectorBundle(InjectorBundle bundle, InjectorBuilder configuration, boolean freeze) {
                bundle.configure(configuration, freeze);
            }

            /** {@inheritDoc} */
            @Override
            public void stageOnFinish(ImportExportStage stage) {
                stage.onFinish();
            }

            @Override
            public void stageOnService(ImportExportStage stage, ServiceConfiguration<?> sc) {
                stage.onService(sc);
            }

            @Override
            public List<ImportExportStage> stagesExtract(ImportExportStage[] stages, Class<?> type) {
                return ImportExportStage.stagesExtract(stages, type);
            }
        });
    }
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

    public ImportExportStage andThen(ImportExportStage stage) {
        requireNonNull(stage, "stage is null");
        return new CombinedStages(List.of(this, stage));
    };

    /** Performs cleanup or post processing validation of the stage. The default implementation does nothing. */
    protected void onFinish() {};

    protected void onService(ServiceConfiguration<?> sc) {}

    // protected Configuration onConfiguration(@Nullable Configuration<?> configuration) {} or
    // protected void onConfiguration(ConfigurationBuilder configuration) {} and then we check for overrides.
    // ImportExportStage configuration.extractChild("jetty"); installContainer(XBundle.class, ConfigurationTransformer<>
    // Kan ogsaa vaere en alm service, og saa naa den er der saa blive annoteringerne aktiveret....
    /// Men ihvertfal conf bliver skubbet op fra roden... Man kan aldrig exportere en Configuration.. Eller det kan man vel
    // godt.
    // Optional Configuration-> You may provide a configuration if you want, mandatory you must provide a Confgiuration.
    // A configuration is always bound with singleton scope, not lazy, not prototype

    static List<ImportExportStage> stagesExtract(ImportExportStage[] stages, Class<?> type) {
        requireNonNull(stages, "stages is null");
        if (stages.length == 0) {
            return List.of();
        }
        ArrayList<ImportExportStage> result = new ArrayList<>(stages.length);
        for (ImportExportStage s : stages) {
            requireNonNull(s, "The specified array of stages contained a null");
            stagesExtract0(s, type, result);
        }
        return List.copyOf(result);
    }

    private static void stagesExtract0(ImportExportStage s, Class<?> type, ArrayList<ImportExportStage> result) {
        if (s instanceof CombinedStages) {
            for (ImportExportStage ies : ((CombinedStages) s).stages) {
                stagesExtract0(ies, type, result);
            }
        } else {
            if (type == Injector.class) {
                if (!(s instanceof InjectorImportStage)) {
                    throw new IllegalArgumentException(
                            "Only stages extending " + InjectorImportStage.class + " are allowed for this method, stage = " + s.getClass());
                }
            } else if (type == InjectorBundle.class) {
                if (!(s instanceof InjectorImportStage)) {
                    throw new IllegalArgumentException("Only stages extending " + InjectorImportStage.class.getSimpleName() + " or "
                            + InjectorExportStage.class.getSimpleName() + " are allowed for this method, stage = " + s.getClass());
                }
            }
            result.add(s);
        }
    }

    static class CombinedStages extends ImportExportStage {
        final List<ImportExportStage> stages;

        CombinedStages(List<ImportExportStage> stages) {
            this.stages = requireNonNull(stages);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(stages.get(0));
            for (int i = 1; i < stages.size(); i++) {
                sb.append(", ").append(stages.get(i));
            }
            sb.append("]");
            return sb.toString();
        }
    }

    // Fordi vi ikke har en ServiceConfiguration
    // Make public... Vil helst have den i Lifecycle maaske. Saa kan vi ogsaa registere den paa
    // onStart(String name)... skal kunne saette navnet, saa maaske
    // onStart(CompletionStage).setName(); eller
    // onStart(String startingPointName, CompletionStage)
    // men hvis ogsaa skulle kunne vente a.la. onStart("cacheLoader").thenRun(sysout(yeah"));
    static class StartStopPointImportExport {

    }
}
