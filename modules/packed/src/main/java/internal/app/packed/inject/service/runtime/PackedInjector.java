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
package internal.app.packed.inject.service.runtime;

import java.util.Map;

import app.packed.base.Key;
import internal.app.packed.inject.service.sandbox.Service;

/** The default implementation of {@link OldServiceLocator}. */
public final class PackedInjector extends AbstractServiceLocator implements OldServiceLocator {

    /** An empty service locator. */
    public static final OldServiceLocator EMPTY_SERVICE_LOCATOR = new PackedInjector(Map.of());


    /** All services that this injector provides. */
    private final Map<Key<?>, RuntimeService> entries;

    public PackedInjector(Map<Key<?>, RuntimeService> services) {
        this.entries = Map.copyOf(services);
    }

    /** {@inheritDoc} */
    @Override
    protected String useFailedMessage(Key<?> key) {
        return "No service with the specified key could be found, key = " + key;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<Key<?>, Service> asMap() {
        return (Map) entries; // services are immutable
    }

}
///** {@inheritDoc} */
//@Override
//public Injector spawn(Wirelet... wirelets) {
//    requireNonNull(wirelets, "wirelets is null");
//    if (wirelets.length == 0) {
//        return this;
//    }
//    ConfigSite cs = ConfigSite.UNKNOWN;
//    if (!ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
//        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> f.getDeclaringClass() == PackedInjector.class).findFirst());
//        cs = sf.isPresent() ? configSite.thenStackFrame("Injector.Spawn", sf.get()) : ConfigSite.UNKNOWN;
//    }
//    LinkedHashMap<Key<?>, RuntimeService> newServices = new LinkedHashMap<>(entries);
//    WireletList wl = WireletList.ofAll(wirelets);
//    ConfigSite ccs = cs;
//    wl.forEach(PackedDownstreamServiceWirelet.class, w -> w.process(ccs, newServices));
//    // TODO Auto-generated method stub
//    return new PackedInjector(cs, newServices);
//}
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