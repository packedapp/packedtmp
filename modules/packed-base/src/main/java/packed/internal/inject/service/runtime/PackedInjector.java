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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.service.Injector;
import app.packed.service.Service;
import app.packed.service.Provider;
import app.packed.service.ServiceLocator;
import packed.internal.component.wirelet.WireletList;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.inject.context.PackedProvideContext;
import packed.internal.inject.service.wirelets.PackedDownstreamInjectionWirelet;
import packed.internal.util.KeyBuilder;

/** The default implementation of {@link Injector}. */
public final class PackedInjector extends AbstractInjector {

    public static final ServiceLocator EMPTY_SERVICE_REGISTRY = new PackedInjector(ConfigSite.UNKNOWN, Map.of());

    /** A stack walker used from {@link #spawn(Wirelet...)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The configuration site of this injector. */
    private final ConfigSite configSite;

    /** All services that this injector provides. */
    private final Map<Key<?>, RuntimeService<?>> entries;

    /** The parent of this injector, or null if this is a top-level injector. */
    @Nullable
    final AbstractInjector parent;

    public PackedInjector(ConfigSite configSite, Map<Key<?>, RuntimeService<?>> services) {
        this.parent = null;
        this.configSite = requireNonNull(configSite);
        this.entries = services;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configSite;
    }

    @Override
    protected void failedGet(Key<?> key) {
        // Oehhh hvad med internal injector, skal vi have en reference til den.
        // Vi kan jo saadan set GC'en den??!?!?!?
        // for (ServiceNode<?> n : services) {
        // if (n instanceof RuntimeNode<T>)
        // }
        super.failedGet(key);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    protected <T> RuntimeService<T> findNode(Key<T> key) {
        return (RuntimeService<T>) entries.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<Provider<T>> findProvider(Key<T> key) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void forEachEntry(Consumer<? super RuntimeService<?>> action) {
        entries.values().forEach(action);
    }

    @Override
    @Nullable
    protected <T> T getInstanceOrNull(Key<T> key) {
        RuntimeService<T> n = findNode(key);
        if (n == null) {
            return null;
        }
        return n.getInstance(PackedProvideContext.of(key));
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<Service> iterator() {
        return (Iterator) Collections.unmodifiableCollection(entries.values()).iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> keys() {
        return entries.keySet(); // I assume entries are immutable
    }

    /** {@inheritDoc} */
    @Override
    public Injector spawn(Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return this;
        }
        ConfigSite cs = ConfigSite.UNKNOWN;
        if (!ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> f.getDeclaringClass() == PackedInjector.class).findFirst());
            cs = sf.isPresent() ? configSite.thenStackFrame("Injector.Spawn", sf.get()) : ConfigSite.UNKNOWN;
        }
        LinkedHashMap<Key<?>, RuntimeService<?>> newServices = new LinkedHashMap<>(entries);
        WireletList wl = WireletList.ofAll(wirelets);
        ConfigSite ccs = cs;
        wl.forEach(PackedDownstreamInjectionWirelet.class, w -> w.process(ccs, newServices));
        // TODO Auto-generated method stub
        return new PackedInjector(cs, newServices);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<Service> stream() {
        return entries.values().stream().filter(e -> !e.key().equals(KeyBuilder.INJECTOR_KEY)).map(e -> e);
    }
}
