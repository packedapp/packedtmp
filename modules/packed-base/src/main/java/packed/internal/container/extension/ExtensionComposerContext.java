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

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.container.BundleDescriptor;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.container.extension.ExtensionIntrospectionContext;
import app.packed.container.extension.ExtensionWireletPipeline;
import app.packed.contract.Contract;
import app.packed.util.Nullable;
import packed.internal.hook.HGBModel;

/**
 *
 */
public class ExtensionComposerContext {

    public Set<HGBModel> hgbs = new HashSet<>();

    public BiConsumer<? super Extension, ? super BundleDescriptor.Builder> builder;

    // Need to check that a contract never belongs to two extension.
    public final IdentityHashMap<Class<? extends Contract>, BiFunction<?, ? super ExtensionIntrospectionContext, ?>> contracts = new IdentityHashMap<>();

    /** An action that will be run immediately after an extension has been instantiated. */
    @Nullable
    public Consumer<? super Extension> onExtensionInstantiatedAction;

    /** An action that will be run immediately after an extension has been configured. */
    @Nullable
    public Consumer<? super Extension> onConfiguredAction;

    @Nullable
    public BiConsumer<? super Extension, ? super Extension> onLinkage;

    public BiConsumer<? super Extension, ? super ExtensionInstantiationContext> onInstantiation;

    public final IdentityHashMap<Class<? extends ExtensionWireletPipeline<?, ?>>, Function<?, ?>> pipelines = new IdentityHashMap<>();

}
