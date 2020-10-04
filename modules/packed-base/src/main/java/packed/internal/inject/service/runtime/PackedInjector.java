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
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import app.packed.service.Injector;
import packed.internal.component.wirelet.WireletList;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.inject.service.wirelets.PackedDownstreamServiceWirelet;

/** The default implementation of {@link Injector}. */
public final class PackedInjector extends AbstractServiceLocator implements Injector {

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

    /** {@inheritDoc} */
    @Override
    protected String failedToUseMessage(Key<?> key) {
        return "No service with the specified key could be found, key = " + key;
    }

    public void forEachEntry(Consumer<? super RuntimeService<?>> action) {
        entries.values().forEach(action);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    protected <T> RuntimeService<T> getService(Key<T> key) {
        return (RuntimeService<T>) entries.get(key);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<Key<?>, Service> services() {
        return (Map) entries;
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