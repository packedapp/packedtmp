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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.ContainerConfiguration;
import app.packed.container.extension.graph.ExtensionOracle;
import app.packed.contract.Contract;
import app.packed.hook.HookGroupBuilder;
import app.packed.hook.HookGroupProcessor;
import packed.internal.container.extension.ExtensionComposerContext;
import packed.internal.hook.HGBModel;
import packed.internal.util.TypeUtil;

/**
 * An extension composer is used for specifying how an extension works.
 */
// Ville vaere rart at kunne gruppe metoderne efter et system og et prefix
public abstract class ExtensionComposer<E extends ComposableExtension<?>> {

    /** The context that all calls are delegated to, must only be accessed via {@link #context}. */
    private ExtensionComposerContext context;

    protected final <B extends HookGroupBuilder<G>, G> void addHookGroup(Class<B> builderType, Supplier<B> builderFactory,
            HookGroupProcessor<E, G> groupProcessor) {
        requireNonNull(builderType, "builderType is null");
        requireNonNull(builderFactory, "builderFactory is null");
        requireNonNull(groupProcessor, "groupProcessor is null");
        TypeUtil.checkClassIsInstantiable(builderType);
        if (!HookGroupBuilder.class.isAssignableFrom(builderType)) {
            throw new IllegalArgumentException("The specified builderType does not implement " + HookGroupBuilder.class.getSimpleName());
        }
        HGBModel m = new HGBModel(builderType, builderFactory, groupProcessor);
        context().hgbs.add(m);
    }

    protected final <P extends ExtensionWireletPipeline<P, ?>> void addPipeline(Class<P> pipelineType, Function<E, P> pipelineFactory) {
        requireNonNull(pipelineType, "pipelineType is null");
        requireNonNull(pipelineFactory, "pipelineFactory is null");
        // Validation??? Pipeline model...
        context().pipelines.putIfAbsent(pipelineType, pipelineFactory);
    }

    protected final void addPostProcessor(Consumer<? extends ExtensionOracle<E>> consumer) {

    }

    /** Configures the composer. This method is invoked exactly once for a given implementation. */
    protected abstract void configure();

    /**
     * Returns the context object that this composer wraps.
     * 
     * @return the context object that this composer wraps.
     * @throws IllegalStateException
     *             if called outside {@link #configure()}
     */
    private ExtensionComposerContext context() {
        ExtensionComposerContext c = context;
        if (c == null) {
            throw new IllegalStateException(
                    "This method can only be called from within the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /**
     * Adds the specified extension types to the set of extensions that this extension depends on.
     * <p>
     * There are no need to add transitive dependencies. Only dependencies that requires a direct call to use
     * 
     * 
     * This is done in order
     * 
     * @param extensionTypes
     *            the types of extension the extension uses.
     */
    @SafeVarargs
    protected final void dependsOn(Class<? extends Extension>... extensionTypes) {
        context().addDependencies(extensionTypes);
    }

    final void dependsOn(String... extensionTypes) {
        // The names will be resolved when composer is created

        // Det ideeele ville vaere hvis man kunne specificere en eller callback/klasse der skulle koeres.
        // Hvis den givne extension var der.
        // Maaske noget a.la. dependOn(String, String instantiateThisClassAndInvokXX)
    }

    /**
     * Invoked by the runtime to start the configuration process.
     * 
     * @param context
     *            the context to wrap
     */
    final void doConfigure(ExtensionComposerContext context) {
        this.context = context;
        // Im not sure we want to null it out...
        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
        try {
            configure();
        } finally {
            this.context = null;
        }
    }

    /**
     * Exposes a contract of the specified type.
     * <p>
     * If the specified contract factory does not return a non-null object of the specified contract type when invoked, the
     * runtime will will throw a {@link ExtensionDeclarationException}.
     * 
     * @param <C>
     *            the type of contract to expose
     * @param contractType
     *            the type of contract the factory creates
     * @param contractFactory
     *            a factory for creating the contract
     * @throws ExtensionDeclarationException
     *             if trying to register a contract type that has already been registered with another extension
     */
    protected final <C extends Contract> void exposeContract(Class<C> contractType,
            BiFunction<? super E, ? super ExtensionIntrospectionContext, C> contractFactory) {
        requireNonNull(contractType, "contractType is null");
        requireNonNull(contractFactory, "contractFactory is null");
        context().contracts.putIfAbsent(contractType, contractFactory);
    }

    @SuppressWarnings("unchecked")
    protected final void exposeDescriptor(BiConsumer<? super E, ? super BundleDescriptor.Builder> builder) {
        context().builder = (BiConsumer<? super Extension, ? super Builder>) requireNonNull(builder, "builder is null");
    }

    protected final void exposeFeature() {}

    /**
     * A callback method that is invoked immediately after a container has been successfully configured. This is typically
     * after {@link Bundle#configure()} has returned.
     * <p>
     * <p>
     * The default implementation of this method does nothing.
     */
    // If the container contains multiple extensions. They are invoked in reverse order. If E2 has a dependency on E1.
    // E2.onConfigured() will be invoked before E1.onConfigure(). This is done in order to allow extensions to perform
    // additional configuration on other extension after user code has been executed
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void onConfigured(Consumer<? super E> action) {
        requireNonNull(action, "action is null");
        Consumer<? super E> a = context().onConfiguredAction;
        context().onConfiguredAction = a == null ? (Consumer) action : a.andThen((Consumer) action);
    }

    /**
     * Registers a (callback) action that is invoked, by the runtime, immediately after an extension has been instantiated,
     * but before the extension has been returned to the user. Typically because a user invoked
     * {@link ContainerConfiguration#use(Class)}.
     * <p>
     * If this method is invoked more than once, each action will be performed in order of registration.
     * 
     * @param action
     *            The action to be performed after the extension has been instantiated
     */
    @SuppressWarnings("unchecked")
    protected final void onExtensionInstantiated(Consumer<? super E> action) {
        context().onExtensionInstantiated((Consumer<? super Extension>) action);
    }

    /**
     * Invoked whenever the container is being instantiated. In case of a container image this means that method might be
     * invoked multiple times. Even by ___multiple threads___
     * 
     * @param action
     *            an instantiation context object
     */
    // Maa koeres efter trae ting??? Eller ogsaa skal det foregaa paa trae tingen...
    // Nope det skal ikke foregaa paa trae tingen. Fordi den skal kun bruges hvis man ikke
    // har extension communication
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void onInstantiation(BiConsumer<? super E, ? super ExtensionInstantiationContext> action) {
        requireNonNull(action, "action is null");
        BiConsumer<? super E, ? super ExtensionInstantiationContext> a = context().onInstantiation;
        context().onInstantiation = a == null ? (BiConsumer) action : a.andThen((BiConsumer) action);
    }

    /**
     * A callback method that is invoked, by the runtime, whenever an extension is present in both a parent container and in
     * a child container.
     * <p>
     * {@link #onExtensionInstantiated(Consumer)} is always invoked for the extension before this method.
     * <p>
     * If this method is invoked more than once, each action will be performed in order of registration.
     * 
     * @param action
     *            the action to perform
     */
    @SuppressWarnings("unchecked")
    protected final void onLinkage(BiConsumer<? super E, ? super E> action) {
        context().onLinkage((BiConsumer<? super Extension, ? super Extension>) action);
    }
}
