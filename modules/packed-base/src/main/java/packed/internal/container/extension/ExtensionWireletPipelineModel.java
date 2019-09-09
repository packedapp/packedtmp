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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionWireletPipeline;
import packed.internal.reflect.MemberProcessor;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class ExtensionWireletPipelineModel {

    /** A cache of values. */
    static final ClassValue<ExtensionWireletPipelineModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected ExtensionWireletPipelineModel computeValue(Class<?> type) {
            return new ExtensionWireletPipelineModel.Builder((Class<? extends ExtensionWireletPipeline<?>>) type).build();
        }
    };

    /** An extractor to find the extension the node is build upon. */
    private static final TypeVariableExtractor EXTENSION_NODE_TV_EXTRACTOR = TypeVariableExtractor.of(ExtensionWireletPipeline.class);

    /** The method handle used to create a new instance of the extension. */
    final MethodHandle constructorNode;

    final MethodHandle constructorPipeline;

    final Class<? extends ExtensionWireletPipeline<?>> pipelineClass;

    public final ExtensionNodeModel node;

    /**
     * @param builder
     */
    @SuppressWarnings("unchecked")
    private ExtensionWireletPipelineModel(Builder builder) {
        Class<? extends ExtensionNode<?>> nodeModel = (Class<? extends ExtensionNode<?>>) EXTENSION_NODE_TV_EXTRACTOR.extract(builder.actualType);
        constructorNode = builder.findConstructor(nodeModel);
        constructorPipeline = builder.findConstructor(builder.actualType);
        this.pipelineClass = (Class<? extends ExtensionWireletPipeline<?>>) builder.actualType;
        this.node = ExtensionNodeModel.of(nodeModel);
    }

    /**
     * Creates a new instance.
     * 
     * @return a new instance
     */
    public final ExtensionWireletPipeline<?> newPipeline(ExtensionNode<?> node) {
        try {
            return (ExtensionWireletPipeline<?>) constructorNode.invoke(node);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    public final ExtensionWireletPipeline<?> newPipeline(ExtensionWireletPipeline<?> previous) {
        try {
            return (ExtensionWireletPipeline<?>) constructorPipeline.invoke(previous);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    public Class<? extends ExtensionWireletPipeline<?>> pipelineClass() {
        return pipelineClass;
    }

    public static ExtensionWireletPipelineModel of(Class<?> type) {
        return CACHE.get(type);
    }

    private static class Builder extends MemberProcessor {

        /**
         * @param type
         */
        private Builder(Class<? extends ExtensionWireletPipeline<?>> type) {
            super(ExtensionWireletPipeline.class, type);
        }

        ExtensionWireletPipelineModel build() {
            return new ExtensionWireletPipelineModel(this);
        }
    }
}
