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

import java.util.IdentityHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.container.BundleDescriptor;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionPipelineContext;
import app.packed.container.extension.ExtensionWireletPipeline;
import app.packed.contract.Contract;

/**
 *
 */
public final class ExtensionComposerContext {

    public Consumer<? super Extension> onAdd;

    public Class<? extends ExtensionNode<?>> nodeType;

    public Function<? extends Extension, ? extends ExtensionNode<?>> nodeFactory;

    public final IdentityHashMap<Class<? extends ExtensionWireletPipeline<?, ?>>, Function<?, ?>> pipelines = new IdentityHashMap<>();

    public final IdentityHashMap<Class<? extends Contract>, BiFunction<?, ExtensionPipelineContext, ?>> contracts = new IdentityHashMap<>();

    public BiConsumer<? super Extension, ? super BundleDescriptor.Builder> builder;
}
