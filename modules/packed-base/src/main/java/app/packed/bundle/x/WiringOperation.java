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
package app.packed.bundle.x;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;

import app.packed.bundle.Bundle;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.Provides;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;

/**
 * A wiring operation is a piece of glue code that wire bundles and/or runtimes together, through operations such as
 * {@link InjectorConfigurator#wireInjector(Injector, WiringOperation...)} or
 * 
 * <p>
 * A typical usage for wiring operations is for rebinding services under another key when wiring an injector into
 * another injector.
 * 
 * start with peek, then import with peek around a service Available as {@code X} in one injector available under a key
 * {@code Y}
 * 
 * Show example where we rebind X to Y, and Y to X, maybe with a peek inbetween
 * 
 * Operations is order Example with rebind
 * 
 * 
 * Pipeline
 */
// https://martinfowler.com/articles/collection-pipeline/
// A bundling always has a bundle (source) and a target???

// WiringOptions
/// kunne have en bundle.disableConfigurationSite()... <- Saa har man den kun paa toplevel app'en....
//// NoConfigurationSite, ForceConfigurationSite

//// ExportTransient -> Meaning everything is exported out again from the bundle
//// exportTransient(Filter) <-Kunne ogsaa vaere paa WiredBundle
public abstract class WiringOperation {

    static {
        BundleSupport.Helper.init(new BundleSupport.Helper() {

            @Override
            public List<WiringOperation> extractWiringOperations(WiringOperation[] operations, Class<?> type) {
                return ComposedWiringOperation.operationsExtract(operations, type);
            }

            /** {@inheritDoc} */
            @Override
            public void finishWireOperation(WiringOperation operation) {
                operation.onFinish();
            }

            @Override
            public Lookup lookupFromWireOperation(WiringOperation operation) {
                return operation.lookup;
            }

            /** {@inheritDoc} */
            @Override
            public void startWireOperation(WiringOperation operation) {
                operation.onStart();
            }
        });
    }

    /** A Lookup object. */
    @Nullable
    final MethodHandles.Lookup lookup;

    /** Creates a new wiring operation. */
    protected WiringOperation() {
        this.lookup = MethodHandles.publicLookup();
    }

    /**
     * Creates a new wiring operation.
     * 
     * @param lookup
     *            a lookup object that will be used, for example, for invoking methods annotated with {@link Provides}.
     */
    protected WiringOperation(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup, "lookup is null");
    }

    /**
     * Returns a new operation which will first use this operation for execution and then the specified operation.
     * 
     * @param next
     *            the operation to execute after this operation
     * @return a new operation that combines this operation and the specified operation
     * @see #compose(WiringOperation...)
     */
    public WiringOperation andThen(WiringOperation next) {
        return compose(this, requireNonNull(next, "next is null"));
    }

    /** Performs cleanup or post processing validation of this operation. The default implementation does nothing. */
    protected void onFinish() {}

    /** Performs any post processing that is needed for the operation. The default implementation does nothing. */
    protected void onStart() {}

    static List<WiringOperation> expandRecursively(WiringOperation... operations) {
        // Maybe just return an array... then we can wrap it ourself
        // could check if we have any aggregated wiring operations
        // Nice to wrap in a list, so we make sure we do not parse along the specified array.
        // Users could modify it
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a wiring operation by composing a sequence of zero or more wiring operations.
     * 
     * @param operations
     *            the operations to combine
     * @return a new combined operation
     * @see #andThen(WiringOperation)
     */
    public static WiringOperation compose(WiringOperation... operations) {
        return new ComposedWiringOperation(operations);
    }

    /** An operation that combines multiple operations. */
    static final class ComposedWiringOperation extends WiringOperation {

        /** The stages that have been combined */
        private final List<WiringOperation> operations;

        ComposedWiringOperation(WiringOperation... operations) {
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

        static List<WiringOperation> operationsExtract(WiringOperation[] operations, Class<?> type) {
            requireNonNull(operations, "operations is null");
            if (operations.length == 0) {
                return List.of();
            }
            ArrayList<WiringOperation> result = new ArrayList<>(operations.length);
            for (WiringOperation s : operations) {
                requireNonNull(s, "The specified array of operations contained a null");
                operationsExtract0(s, type, result);
            }
            return List.copyOf(result);
        }

        private static void operationsExtract0(WiringOperation o, Class<?> type, ArrayList<WiringOperation> result) {
            if (o instanceof ComposedWiringOperation) {
                for (WiringOperation ies : ((ComposedWiringOperation) o).operations) {
                    operationsExtract0(ies, type, result);
                }
            } else {
                if (type == Injector.class) {
                    if (!(o instanceof UpstreamWiringOperation)) {
                        throw new IllegalArgumentException(
                                "Only operations extending " + UpstreamWiringOperation.class + " are allowed for this method, operation = " + o.getClass());
                    }
                } else if (type == Bundle.class) {
                    if (!(o instanceof UpstreamWiringOperation)) {
                        throw new IllegalArgumentException("Only operation extending " + UpstreamWiringOperation.class.getSimpleName() + " or "
                                + DownstreamWiringOperation.class.getSimpleName() + " are allowed for this method, operation = " + o.getClass());
                    }
                }
                result.add(o);
            }
        }

    }

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
