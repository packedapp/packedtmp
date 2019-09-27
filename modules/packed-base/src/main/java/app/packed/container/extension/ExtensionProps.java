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

import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.contract.Contract;
import app.packed.hook.HookGroupBuilder;
import packed.internal.container.extension.ExtensionPropsContext;
import packed.internal.util.StringFormatter;

/**
 *
 */
public abstract class ExtensionProps<T extends ComposableExtension<?>> {

    private ExtensionPropsContext context;

    protected final <E extends Contract> void addContract(Class<E> contractType, BiFunction<T, ExtensionPipelineContext, E> contractFactory) {
        // -> BiFunction(Extension, DescriptorContextWithPipelines)
        requireNonNull(contractType, "contractType is null");
        requireNonNull(contractFactory, "contractFactory is null");
        context().contracts.putIfAbsent(contractType, contractFactory);
    }

    protected final <E extends ExtensionWireletPipeline<E, ?>> void addPipeline(Class<E> pipelineType, Function<T, E> pipelineFactory) {
        requireNonNull(pipelineType, "pipelineType is null");
        requireNonNull(pipelineFactory, "pipelineFactory is null");
        // Validation??? Pipeline model...
        context().pipelines.putIfAbsent(pipelineType, pipelineFactory);
    }

    protected final <B extends HookGroupBuilder<G>, G> void addHookGroup(Class<B> builderType, BiConsumer<T, G> groupConsumer) {
        // OnHookGroup

    }

    @SuppressWarnings("unchecked")
    protected final void buildBundleDescriptor(BiConsumer<? super T, ? super BundleDescriptor.Builder> builder) {
        context().builder = (BiConsumer<? super Extension, ? super Builder>) requireNonNull(builder, "builder is null");
    }

    protected abstract void configure();

    // /**
    // * Returns the extension's extension node. This method will be invoked exactly once by the runtime and must return a
    // * non-null value of the exact same type as the single type parameter to ComposableExtension.
    // *
    // * @return the extension's extension node
    // */
    private ExtensionPropsContext context() {
        ExtensionPropsContext c = context;
        if (c == null) {
            throw new IllegalStateException(
                    "This method can only be called from within the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /**
     * Invoked by the runtime to start the configuration process.
     * 
     * @param context
     *            the context to wrap
     */
    final void doConfigure(ExtensionPropsContext context) {
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

    // addNode??
    protected final <E extends ExtensionNode<T>> void useNode(Class<E> nodeType, Function<T, E> nodeFactory) {
        requireNonNull(nodeType, "nodeType is null");
        requireNonNull(nodeFactory, "nodeFactory is null");
        if (!Modifier.isFinal(nodeType.getModifiers())) {
            throw new ExtensionDeclarationException("The extension node type must be declared final, node type = " + StringFormatter.format(nodeType));
        }
        context().nodeType = nodeType;
        context().nodeFactory = nodeFactory;
    }
}
