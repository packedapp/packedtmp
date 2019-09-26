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
package packed.internal.container.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletPipeline;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class ExtensionWireletPipelineModel {

    /** A cache of values. */
    private static final ClassValue<ExtensionWireletPipelineModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected ExtensionWireletPipelineModel computeValue(Class<?> type) {
            return new ExtensionWireletPipelineModel.Builder((Class<? extends ExtensionWireletPipeline<?, ?>>) type).build();
        }
    };

    /** An extractor to find the extension the node is build upon. */
    private static final TypeVariableExtractor EXTENSION_NODE_TV_EXTRACTOR = TypeVariableExtractor.of(ExtensionWireletPipeline.class);

    /** The method handle used to create a new instance of the extension. */
    private final MethodHandle constructorNode;

    private final MethodHandle constructorPipeline;

    public final ExtensionModel<?> node;

    final Class<? extends ExtensionWireletPipeline<?, ?>> pipelineClass;

    /**
     * @param builder
     */
    @SuppressWarnings("unchecked")
    private ExtensionWireletPipelineModel(Builder builder) {
        Class<? extends ExtensionNode<?>> nodeModel = (Class<? extends ExtensionNode<?>>) EXTENSION_NODE_TV_EXTRACTOR.extract(builder.actualType);
        this.constructorNode = ConstructorFinder.find(builder.actualType, nodeModel);
        this.constructorPipeline = ConstructorFinder.find(builder.actualType, builder.actualType);
        this.pipelineClass = builder.actualType;
        this.node = ExtensionNodeModel.of(nodeModel).extension;
    }

    /**
     * Creates a new instance.
     * 
     * @return a new instance
     */
    public ExtensionWireletPipeline<?, ?> newPipeline(ExtensionNode<?> node) {
        try {
            return (ExtensionWireletPipeline<?, ?>) constructorNode.invoke(node);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    public ExtensionWireletPipeline<?, ?> newPipeline(ExtensionWireletPipeline<?, ?> previous) {
        try {
            return (ExtensionWireletPipeline<?, ?>) constructorPipeline.invoke(previous);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private static ExtensionWireletPipelineModel of(Class<?> type) {
        return CACHE.get(type);
    }

    public static ExtensionWireletPipelineModel ofWirelet(Class<? extends ExtensionWirelet<?>> wireletType) {
        return ExtensionWireletModel.CACHE.get(wireletType).pipeline;
    }

    private static class Builder {

        private final Class<? extends ExtensionWireletPipeline<?, ?>> actualType;

        /**
         * @param type
         */
        private Builder(Class<? extends ExtensionWireletPipeline<?, ?>> type) {
            actualType = requireNonNull(type);
        }

        ExtensionWireletPipelineModel build() {
            return new ExtensionWireletPipelineModel(this);
        }
    }

    /**
     * A model for a class extending {@link ExtensionWirelet}. Is currently only used to extract the type variable from the
     * wirelet. Which points to the pipeline it is a part of.
     */
    // Right now this is just a static class, because I'm unsure whether or not we will cache other information than the
    // type parameter
    // to ExtensionWirelet
    private static class ExtensionWireletModel {

        /** A cache of values. */
        private static final ClassValue<ExtensionWireletModel> CACHE = new ClassValue<>() {

            /** {@inheritDoc} */
            @SuppressWarnings("unchecked")
            @Override
            protected ExtensionWireletModel computeValue(Class<?> type) {
                return new ExtensionWireletModel((Class<? extends ExtensionWirelet<?>>) type);
            }
        };

        /** A type variable extractor to extract what kind of pipeline an extension wirelet belongs to. */
        private static final TypeVariableExtractor EXTENSION_TYPE_EXTRACTOR = TypeVariableExtractor.of(ExtensionWirelet.class);

        private final ExtensionWireletPipelineModel pipeline;

        /**
         * @param type
         */
        @SuppressWarnings("unchecked")
        private ExtensionWireletModel(Class<? extends ExtensionWirelet<?>> type) {
            Class<? extends ExtensionWireletPipeline<?, ?>> extensionType = (Class<? extends ExtensionWireletPipeline<?, ?>>) EXTENSION_TYPE_EXTRACTOR
                    .extract(type);
            this.pipeline = ExtensionWireletPipelineModel.of(extensionType);
        }
    }
}
