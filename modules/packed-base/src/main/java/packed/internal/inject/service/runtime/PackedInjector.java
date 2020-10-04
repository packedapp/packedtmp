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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Provider;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import app.packed.service.Injector;
import packed.internal.component.wirelet.WireletList;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.inject.context.PackedProvideContext;
import packed.internal.inject.service.wirelets.PackedDownstreamServiceWirelet;

/** The default implementation of {@link Injector}. */
public final class PackedInjector extends AbstractServiceRegistry implements Injector {

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> find(Class<T> key) {
        return Optional.ofNullable(getInstanceOrNull(key));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> find(Key<T> key) {
        return Optional.ofNullable(getInstanceOrNull(key));
    }

    @Nullable
    protected <T> RuntimeService<T> findNode(Class<T> key) {
        return findNode(Key.of(key));
    }

    @Nullable
    private <T> T getInstanceOrNull(Class<T> key) {
        return getInstanceOrNull(Key.of(key));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T use(Class<T> key) {
        T t = getInstanceOrNull(key);
        if (t != null) {
            return t;
        }
        failedGet(Key.of(key));

        // /child [ss.BaseMyBundle] does not export a service with the specified key

        // FooBundle does not export a service with the key
        // It has an internal service. Maybe you forgot to export it()
        // Is that breaking encapsulation

        throw new NoSuchElementException("No service with the specified key could be found, key = " + key);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T use(Key<T> key) {
        T t = getInstanceOrNull(key);
        if (t == null) {
            throw new NoSuchElementException("No service with the specified key could be found, key = " + key);
        }
        return t;
    }

    /** An empty service locator. */
    public static final ServiceLocator EMPTY_SERVICE_LOCATOR = new PackedInjector(ConfigSite.UNKNOWN, Map.of());

    /** A stack walker used from {@link #spawn(Wirelet...)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The configuration site of this injector. */
    private final ConfigSite configSite;

    /** All services that this injector provides. */
    private final Map<Key<?>, RuntimeService<?>> entries;

    /** The parent of this injector, or null if this is a top-level injector. */
    @Nullable
    final PackedInjector parent;

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

    /**
     * Ideen er egentlig at vi kan lave en detaljeret fejlbesked, f.eks, vi har en X type, men den har en qualifier. Eller
     * vi har en qualifier med navnet DDooo og du skrev PDooo
     * 
     * @param key
     */
    protected void failedGet(Key<?> key) {
        // Oehhh hvad med internal injector, skal vi have en reference til den.
        // Vi kan jo saadan set GC'en den??!?!?!?
        // for (ServiceNode<?> n : services) {
        // if (n instanceof RuntimeNode<T>)
        // }

    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> RuntimeService<T> findNode(Key<T> key) {
        return (RuntimeService<T>) entries.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<Provider<T>> findProvider(Key<T> key) {
        throw new UnsupportedOperationException();
    }

    public void forEachEntry(Consumer<? super RuntimeService<?>> action) {
        entries.values().forEach(action);
    }

    @Nullable
    protected <T> T getInstanceOrNull(Key<T> key) {
        RuntimeService<T> n = findNode(key);
        if (n == null) {
            return null;
        }
        return n.getInstance(PackedProvideContext.of(key));
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
        wl.forEach(PackedDownstreamServiceWirelet.class, w -> w.process(ccs, newServices));
        // TODO Auto-generated method stub
        return new PackedInjector(cs, newServices);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<Key<?>, Service> services() {
        return (Map) entries;
    }
}

//protected final void injectMembers(OldAtInjectGroup descriptor, Object instance, @Nullable Component component) {
//// Inject fields
//if (!descriptor.fields.isEmpty()) {
//for (OldAtInject atInject : descriptor.fields) {
//Dependency dependency = atInject.dependencies.get(0);
//FieldFactoryHandle<?> field = (FieldFactoryHandle<?>) atInject.invokable;
//InjectorEntry<?> node = findNode(dependency.key());
//if (node != null) {
//Object value = node.getInstance(PrototypeRequest.of(dependency, component));
//value = dependency.wrapIfOptional(value);
//field.setOnInstance(instance, value);
//} else if (dependency.isOptional()) {
//// 3 Valgmuligheder
//
//// Altid overskriv
//
//// Overskriv Optional, ikke for nullable
//
//// Aldrig overskriv
//
//// I think we want to set optional fields????
//// But not nullable
//// I think
//// if field == null, inject, otherwise leave to value
//// Hmm, should we override existing value???
//// For consistentsee reason yes, but hmm it is useful not to override
//} else {
//String msg = "Could not find a valid value for " + dependency.key() + " on field " + field.toString();
//throw new InjectionException(msg);
//}
//}
//}
//
/// ** {@inheritDoc} */
//@Override
//public final <T> T injectMembers(T instance, MethodHandles.Lookup lookup) {
//requireNonNull(instance, "instance is null");
//requireNonNull(lookup, "lookup is null");
//AtInjectGroup scd = MemberScanner.forService(instance.getClass(), lookup).inject.build();
//injectMembers(scd, instance, null);
//return instance;
//}
//Better help
//
//public static String getInstanceNotFound(Key<?> key) {
//return "No service of the specified type is available [type = " + key + "]. You can use " +
//OldInjector.class.getSimpleName()
//+ "#getAvailableServices() to find out what kind of services are available";
//}
//
////Think we can put in some more help, when people try inject "strange things"
////InjectionSite can only be injected into non-static methods annotated with @Provides
//
//public static String getProviderNotFound(Key<?> key) {
//return "No service of the specified type is available [type = " + key + "]. You can use " +
//OldInjector.class.getSimpleName()
//+ "#getAvailableServices() to find out what kind of services are available";
//}