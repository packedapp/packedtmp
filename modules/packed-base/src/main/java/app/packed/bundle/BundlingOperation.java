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

import app.packed.inject.Injector;
import app.packed.inject.InjectorBundle;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.BundlingServiceImportStage;
import packed.internal.inject.builder.InjectorBuilder;

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
// Bundling -> import pipeline and an export pipeline where each element of the exposed api is processed through
/**
 * A bundling operation is an operation that can be performed when bundles and/or runtimes are bundled together.
 * 
 * A shared superclass for {@link BundlingImportOperation} and {@link BundlingExportOperation}.
 */
// https://martinfowler.com/articles/collection-pipeline/
// A bundling always has a bundle (source) and a target???
// The process of connected two modules is called wiring
// WiringOperation
// BundleWiringOperation
public abstract class BundlingOperation {

    static {
        BundleSupport.Helper.init(new BundleSupport.Helper() {

            /** {@inheritDoc} */
            @Override
            public void configureInjectorBundle(InjectorBundle bundle, InjectorBuilder configuration, boolean freeze) {
                // bundle.configure(configuration, freeze);
            }

            @Override
            public Lookup stageLookup(BundlingOperation stage) {
                return stage.lookup;
            }

            /** {@inheritDoc} */
            @Override
            public void bundleOperationFinish(BundlingOperation operation) {
                operation.onFinish();
            }

            @Override
            public void stageOnService(BundlingOperation stage, ServiceConfiguration<?> sc) {
                if (stage instanceof BundlingServiceImportStage) {
                    ((BundlingServiceImportStage) stage).onEachService(sc);
                }
            }

            @Override
            public List<BundlingOperation> extractBundlingOperations(BundlingOperation[] operations, Class<?> type) {
                return AggregatedBundlingOperation.operationsExtract(operations, type);
            }
        });
    }
    /** A Lookup object. */
    @Nullable
    final MethodHandles.Lookup lookup;

    /** Creates a new operation. */
    BundlingOperation() {
        this.lookup = MethodHandles.publicLookup();
    }

    /**
     * Creates a new operation.
     * 
     * @param lookup
     *            a lookup object that will be used, for example, for invoking methods annotated with {@link Provides}.
     */
    BundlingOperation(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup, "lookup is null");
    }

    /**
     * Returns a new operation which will first use this operation for execution and then the specified operation for
     * execution
     * 
     * @param next
     *            the operation to execute after this operation
     * @return a new operation that combines this operation and the specified operation
     * @see #of(BundlingOperation...)
     */
    public BundlingOperation andThen(BundlingOperation next) {
        return of(this, requireNonNull(next, "next is null"));
    };

    /** Performs cleanup or post processing validation of this operation. The default implementation does nothing. */
    protected void onFinish() {}

    /**
     * Combine multiple operations into a single operation.
     * 
     * @param operations
     *            the operations to combine
     * @return a new combined operation
     * @see #andThen(BundlingOperation)
     */
    public static BundlingOperation of(BundlingOperation... operations) {
        return new AggregatedBundlingOperation(operations);
    }

    /** An operation that combines multiple bundling operations. */
    static final class AggregatedBundlingOperation extends BundlingOperation {

        /** The stages that have been combined */
        private final List<BundlingOperation> operations;

        AggregatedBundlingOperation(BundlingOperation... operations) {
            this.operations = List.of(requireNonNull(operations, "operations is null"));
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(operations.get(0));
            for (int i = 1; i < operations.size(); i++) {
                sb.append(", ").append(operations.get(i));
            }
            sb.append("]");
            return sb.toString();
        }

        static List<BundlingOperation> operationsExtract(BundlingOperation[] operations, Class<?> type) {
            requireNonNull(operations, "operations is null");
            if (operations.length == 0) {
                return List.of();
            }
            ArrayList<BundlingOperation> result = new ArrayList<>(operations.length);
            for (BundlingOperation s : operations) {
                requireNonNull(s, "The specified array of operations contained a null");
                operationsExtract0(s, type, result);
            }
            return List.copyOf(result);
        }

        private static void operationsExtract0(BundlingOperation o, Class<?> type, ArrayList<BundlingOperation> result) {
            if (o instanceof AggregatedBundlingOperation) {
                for (BundlingOperation ies : ((AggregatedBundlingOperation) o).operations) {
                    operationsExtract0(ies, type, result);
                }
            } else {
                if (type == Injector.class) {
                    if (!(o instanceof BundlingImportOperation)) {
                        throw new IllegalArgumentException(
                                "Only operations extending " + BundlingImportOperation.class + " are allowed for this method, operation = " + o.getClass());
                    }
                } else if (type == InjectorBundle.class) {
                    if (!(o instanceof BundlingImportOperation)) {
                        throw new IllegalArgumentException("Only operation extending " + BundlingImportOperation.class.getSimpleName() + " or "
                                + BundlingExportOperation.class.getSimpleName() + " are allowed for this method, operation = " + o.getClass());
                    }
                }
                result.add(o);
            }
        }

    }

    // protected Configuration onConfiguration(@Nullable Configuration<?> configuration) {} or
    // protected void onConfiguration(ConfigurationBuilder configuration) {} and then we check for overrides.
    // ImportExportStage configuration.extractChild("jetty"); installContainer(XBundle.class, ConfigurationTransformer<>
    // Kan ogsaa vaere en alm service, og saa naa den er der saa blive annoteringerne aktiveret....
    /// Men ihvertfal conf bliver skubbet op fra roden... Man kan aldrig exportere en Configuration.. Eller det kan man vel
    // godt.
    // Optional Configuration-> You may provide a configuration if you want, mandatory you must provide a Confgiuration.
    // A configuration is always bound with singleton scope, not lazy, not prototype

    // Fordi vi ikke har en ServiceConfiguration
    // Make public... Vil helst have den i Lifecycle maaske. Saa kan vi ogsaa registere den paa
    // onStart(String name)... skal kunne saette navnet, saa maaske
    // onStart(CompletionStage).setName(); eller
    // onStart(String startingPointName, CompletionStage)
    // men hvis ogsaa skulle kunne vente a.la. onStart("cacheLoader").thenRun(sysout(yeah"));
}
