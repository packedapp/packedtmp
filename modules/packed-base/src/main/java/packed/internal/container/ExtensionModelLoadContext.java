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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.analysis.BundleDescriptor;
import app.packed.base.Contract;
import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionComposer;
import app.packed.container.ExtensionWirelet;

/** A context object used for all registration for an {@link ExtensionComposer}. */
public abstract class ExtensionModelLoadContext {

    public BiConsumer<? super Extension, ? super BundleDescriptor.Builder> builder;

    // Need to check that a contract never belongs to two extension.
    // Also, I think we want to do this atomically, so that we do not have half an extension registered somewhere.
    // This means we want to synchronize things.
    // So add all shit, quick validation-> Sync->Validate final -> AddAll ->UnSync
    public final IdentityHashMap<Class<? extends Contract>, Object> contracts = new IdentityHashMap<>();

    /** A list of dependencies on other extensions. */
    Set<Class<? extends Extension>> dependenciesDirect = new HashSet<>();

    /** The type of extension we are building a model for. */
    final Class<? extends Extension> extensionType;

    // public BiConsumer<? super Extension, ? super ExtensionInstantiationContext> onInstantiation;

    /** An action that will be run immediately after all bundles have been configured. */
    @Nullable
    public Consumer<? super Extension> onCompleteEach;

    /** An action that will be run immediately after an extension has been configured. */
    @Nullable
    public Consumer<? super Extension> onConfiguredAction;
    /** An action that will be run immediately after an extension has been instantiated. */
    @Nullable
    Consumer<? super Extension> onExtensionInstantiatedAction;

    @Nullable
    public BiConsumer<? super Extension, ? super Extension> onLinkage;

    public final IdentityHashMap<Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>, Function<?, ?>> pipelines = new IdentityHashMap<>();

    final ExtensionModelLoader.Runtime runtime;

    /** This class can only be overridden by another class in this package. */
    ExtensionModelLoadContext(Class<? extends Extension> extensionType, ExtensionModelLoader.Runtime runtime) {
        this.extensionType = requireNonNull(extensionType);
        this.runtime = runtime;
    }

    @SafeVarargs
    public final void addDependencies(Class<? extends Extension>... dependencies) {
        requireNonNull(dependencies, "dependencies is null");
        for (Class<? extends Extension> c : dependencies) {
            ExtensionModelLoader.load(c, runtime);
            dependenciesDirect.add(c);
        }
    }

    /**
     * Registers an action that is invoked when the extension has first been instantiated
     * 
     * @param action
     *            the action to perform
     * 
     * @see ExtensionComposer#onExtensionInstantiated(Consumer)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void onExtensionInstantiated(Consumer<? super Extension> action) {
        requireNonNull(action, "action is null");
        Consumer<? super Extension> a = onExtensionInstantiatedAction;
        onExtensionInstantiatedAction = a == null ? action : a.andThen((Consumer) action);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void onLinkage(BiConsumer<? super Extension, ? super Extension> action) {
        // ?: Should have a link type?
        // A: No, because extensions are only linked inside a single artifact. Otherwise they use HostAccessor

        // This might be invoked after the c
        requireNonNull(action, "action is null");
        BiConsumer<? super Extension, ? super Extension> a = onLinkage;
        onLinkage = a == null ? action : a.andThen((BiConsumer) action);
    }
}
