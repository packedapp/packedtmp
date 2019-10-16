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
package packed.internal.service.run;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.service.Injector;
import app.packed.service.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.WireletList;
import packed.internal.service.build.wirelets.PackedDownstreamInjectionWirelet;
import packed.internal.util.KeyBuilder;

/** The default implementation of {@link Injector}. */
public final class DefaultInjector extends AbstractInjector {

    /** A stack walker used from {@link #spawn(Wirelet...)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The configuration site of this injector. */
    private final ConfigSite configSite;

    /** An optional description of the injector. */
    @Nullable
    private final String description;

    /** The parent of this injector, or null if this is a top-level injector. */
    @Nullable
    final AbstractInjector parent;

    /** All services that this injector provides. */
    private final Map<Key<?>, InjectorEntry<?>> services;

    public DefaultInjector(ConfigSite configSite, @Nullable String description, Map<Key<?>, InjectorEntry<?>> services) {
        this.parent = null;
        this.configSite = requireNonNull(configSite);
        this.description = description;
        this.services = services;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configSite;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    protected void failedGet(Key<?> key) {
        // Oehhh hvad med internal injector, skal vi have en reference til den.
        // Vi kan jo saadan set GC'en den??!?!?!?
        // for (ServiceNode<?> n : services) {
        // System.out.println("Failed Get " + n);
        // if (n instanceof RuntimeNode<T>)
        // }
        super.failedGet(key);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    protected <T> InjectorEntry<T> findNode(Key<T> key) {
        return (InjectorEntry<T>) services.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public void forEachServiceEntry(Consumer<? super InjectorEntry<?>> action) {
        services.values().forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ServiceDescriptor> services() {
        return services.values().stream().filter(e -> !e.key().equals(KeyBuilder.INJECTOR_KEY)).map(e -> {
            return e;
        });
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
            Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> f.getDeclaringClass() == DefaultInjector.class).findFirst());
            cs = sf.isPresent() ? configSite.thenStackFrame("Injector.Spawn", sf.get()) : ConfigSite.UNKNOWN;
        }
        LinkedHashMap<Key<?>, InjectorEntry<?>> newServices = new LinkedHashMap<>(services);
        WireletList wl = WireletList.of(wirelets);
        ConfigSite ccs = cs;
        wl.forEach(PackedDownstreamInjectionWirelet.class, w -> w.process(ccs, newServices));
        // TODO Auto-generated method stub
        return new DefaultInjector(cs, description, newServices);
    }
}
