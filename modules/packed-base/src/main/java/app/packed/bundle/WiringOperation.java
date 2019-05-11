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
import app.packed.inject.SimpleInjectorConfigurator;
import packed.internal.bundle.BundleSupport;

// Wire vs link....

// Interface -> kan man let implementere, uden at fucke et nedarvnings hiraki op
// Klasse -> Vi kan have protected metoder
/**
 * A wiring operation is a piece of glue code that wire bundles and/or runtimes together, through operations such as
 * {@link SimpleInjectorConfigurator#wireInjector(Injector, WiringOperation...)} or
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
                // operation.onFinish();
            }

            @Override
            public Lookup lookupFromWireOperation(WiringOperation operation) {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public void startWireOperation(WiringOperation operation) {
                operation.process(null);
            }
        });
    }
    // For bedre error messages. This operation can only be used if the parent or child bundle
    // has installed the XXX extension (As an alternative, annotated the key with
    // @RequiresExtension(JMXExtension.class)....)
    // Or even better the actual WiringOperation.....
    // protected void useAttachment(Key...., Class<?> requiredExtension);

    // Bootstrap classes... Classes that are only available for injection.... (Not even initialized....)
    // bundleLink.bootstrapWith(StringArgs.of("sdsdsd");
    // bundleLink.bootstrapWith(Configuration.read("c:/sdasdasd\'");
    // run(new XBundle(), Configuration.read("c:/sdad"));

    /**
     * Returns a composed {@code WiringOperation} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed operation.
     * If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after
     *            the operation to perform after this operation
     * @return a composed {@code WiringOperation} that performs in sequence this operation followed by the {@code after}
     *         operation
     */
    public final WiringOperation andThen(WiringOperation after) {
        return compose(this, requireNonNull(after, "after is null"));
    }

    public final WiringOperation andThen(WiringOperation... nextOperations) {
        ArrayList<WiringOperation> l = new ArrayList<>();
        l.add(this);
        l.addAll(List.of(nextOperations));
        return compose(l.toArray(i -> new WiringOperation[i]));
    }

    // protected void validate(); Validates that the operation can be used

    protected abstract void process(BundleLink link);

    static WiringOperation disableConfigSet() {
        // Man skal vel ogsaa kunne enable den igen....
        throw new UnsupportedOperationException();
    }

    static WiringOperation lookup(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    static WiringOperation provide(Object o) {
        // Is this service instead???
        // Bootstrap

        // Den virker jo kun som den yderste container...
        // I den inderste skal vi
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a wiring operation by composing a sequence of zero or more wiring operations.
     * 
     * @param operations
     *            the operations to combine
     * @return a new combined operation
     * @see #andThen(WiringOperation)
     * @see #andThen(WiringOperation...)
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
                result.add(o);
            }
        }

        @Override
        protected void process(BundleLink link) {
            for (WiringOperation wo : operations) {
                wo.process(link);
            }
        }
    }

    // Kunne vaere rart, hvis man f.eks. kunne
    // wire(SomeBundle.class, JaxRSSpec.v2_1.wireStrict());
    // wire(SomeBundle.class, JettySpec.v23_1.wireStrict());

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
// De bliver bare processeret ind efter den anden...
// Eller....