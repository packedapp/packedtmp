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
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;

import app.packed.container.ContainerExportStage;
import app.packed.container.ContainerImportStage;
import app.packed.inject.Injector;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * A shared superclass for {@link BundlingImportStage}, {@link BundlingStage}, {@link ContainerImportStage} and
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
// BundleActivationStage// BundleGlueStage
// BundleCompositionStage
// ServiceLoader.enabledInvocationOnComponentMethodsAnnotatedWith(xxx.class, ...);
// Interface (maybe ditch it for now) + Description

// BundlingFilter

// Bundling -> import pipeline and an export pipeline where each element of the exposed api is processed through
public abstract class BundlingStage {

    static {
        BundleSupport.Helper.init(new BundleSupport.Helper() {

            /** {@inheritDoc} */
            @Override
            public void configureInjectorBundle(InjectorBundle bundle, InjectorBuilder configuration, boolean freeze) {
                bundle.configure(configuration, freeze);
            }

            @Override
            public Lookup stageLookup(BundlingStage stage) {
                return stage.lookup;
            }

            /** {@inheritDoc} */
            @Override
            public void stageOnFinish(BundlingStage stage) {
                stage.onFinish();
            }

            @Override
            public void stageOnService(BundlingStage stage, ServiceConfiguration<?> sc) {
                stage.onEachService(sc);
            }

            @Override
            public List<BundlingStage> stagesExtract(BundlingStage[] stages, Class<?> type) {
                return BundlingStage.stagesExtract(stages, type);
            }
        });
    }
    /** A Lookup object. */
    @Nullable
    final MethodHandles.Lookup lookup;

    /** Creates a new stage. */
    BundlingStage() {
        this.lookup = MethodHandles.publicLookup();
    }

    /**
     * Creates a new stage.
     * 
     * @param lookup
     *            a lookup object that will be used, for example, for invoking methods annotated with {@link Provides}.
     */
    BundlingStage(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup, "lookup is null");
    }

    /**
     * Returns a new bundling stage which will first execute this stage and then execute the specified stage
     * 
     * @param stage
     *            the stage to execute after this stage
     * @return a new stage that combines this stage and the specified stage
     * @see #of(BundlingStage...)
     */
    public BundlingStage andThen(BundlingStage stage) {
        return of(this, requireNonNull(stage, "stage is null"));
    };

    // IDeen er lidt at kalde alle der procerere mere end en entity onEachX, og resten onX
    // f.eks. onDescription <- Der er kun en per injector/container hvilket jo saa
    protected void onEachService(ServiceConfiguration<?> sc) {};

    /** Performs cleanup or post processing validation of the stage. The default implementation does nothing. */
    protected void onFinish() {}

    // protected Configuration onConfiguration(@Nullable Configuration<?> configuration) {} or
    // protected void onConfiguration(ConfigurationBuilder configuration) {} and then we check for overrides.
    // ImportExportStage configuration.extractChild("jetty"); installContainer(XBundle.class, ConfigurationTransformer<>
    // Kan ogsaa vaere en alm service, og saa naa den er der saa blive annoteringerne aktiveret....
    /// Men ihvertfal conf bliver skubbet op fra roden... Man kan aldrig exportere en Configuration.. Eller det kan man vel
    // godt.
    // Optional Configuration-> You may provide a configuration if you want, mandatory you must provide a Confgiuration.
    // A configuration is always bound with singleton scope, not lazy, not prototype

    /**
     * @param stages
     *            the stages to combine
     * @return
     * @see #andThen(BundlingStage)
     */
    public static BundlingStage of(BundlingStage... stages) {
        requireNonNull(stages, "stages is null");
        return new CombinedStages(List.of(stages));
    }

    static List<BundlingStage> stagesExtract(BundlingStage[] stages, Class<?> type) {
        requireNonNull(stages, "stages is null");
        if (stages.length == 0) {
            return List.of();
        }
        ArrayList<BundlingStage> result = new ArrayList<>(stages.length);
        for (BundlingStage s : stages) {
            requireNonNull(s, "The specified array of stages contained a null");
            stagesExtract0(s, type, result);
        }
        return List.copyOf(result);
    }

    private static void stagesExtract0(BundlingStage s, Class<?> type, ArrayList<BundlingStage> result) {
        if (s instanceof CombinedStages) {
            for (BundlingStage ies : ((CombinedStages) s).stages) {
                stagesExtract0(ies, type, result);
            }
        } else {
            if (type == Injector.class) {
                if (!(s instanceof BundlingImportStage)) {
                    throw new IllegalArgumentException(
                            "Only stages extending " + BundlingImportStage.class + " are allowed for this method, stage = " + s.getClass());
                }
            } else if (type == InjectorBundle.class) {
                if (!(s instanceof BundlingImportStage)) {
                    throw new IllegalArgumentException("Only stages extending " + BundlingImportStage.class.getSimpleName() + " or "
                            + BundlingExportStage.class.getSimpleName() + " are allowed for this method, stage = " + s.getClass());
                }
            }
            result.add(s);
        }
    }

    static class CombinedStages extends BundlingStage {
        final List<BundlingStage> stages;

        CombinedStages(List<BundlingStage> stages) {
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
}
