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

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.Component;
import app.packed.service.InjectionException;
import app.packed.service.Injector;
import app.packed.service.ServiceDependency;
import app.packed.service.ServiceRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.ServiceEntry;
import packed.internal.service.factoryhandle.FieldFactoryHandle;
import packed.internal.service.util.nextapi.OldAtInject;
import packed.internal.service.util.nextapi.OldAtInjectGroup;

/** An abstract implementation of an injector. */
public abstract class AbstractInjector implements Injector {

    /** {@inheritDoc} */
    @Override
    public final boolean contains(Class<?> key) {
        return findNode(key) != null;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(Key<?> key) {
        return findNode(key) != null;
    }

    /**
     * Ideen er egentlig at vi kan lave en detaljeret fejlbesked, f.eks, vi har en X type, men den har en qualifier. Eller
     * vi har en qualifier med navnet DDooo og du skrev PDooo
     * 
     * @param key
     */
    protected void failedGet(Key<?> key) {}

    @Nullable
    protected <T> ServiceEntry<T> findNode(Class<T> key) {
        return findNode(Key.of(key));
    }

    @Nullable
    protected abstract <T> ServiceEntry<T> findNode(Key<T> key);

    public abstract void forEachServiceEntry(Consumer<? super ServiceEntry<?>> action);

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> get(Class<T> key) {
        return Optional.ofNullable(getInstanceOrNull(key));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> get(Key<T> key) {
        return Optional.ofNullable(getInstanceOrNull(key));
    }

    @Nullable
    private <T> T getInstanceOrNull(Class<T> key) {
        return getInstanceOrNull(Key.of(key));
    }

    @Nullable
    private <T> T getInstanceOrNull(Key<T> key) {
        ServiceEntry<T> n = findNode(key);
        if (n == null) {
            return null;
        }
        return n.getInstance(ServiceRequest.of(key));
    }

    protected final void injectMembers(OldAtInjectGroup descriptor, Object instance, @Nullable Component component) {
        // Inject fields
        if (!descriptor.fields.isEmpty()) {
            for (OldAtInject atInject : descriptor.fields) {
                ServiceDependency dependency = atInject.dependencies.get(0);
                FieldFactoryHandle<?> field = (FieldFactoryHandle<?>) atInject.invokable;
                ServiceEntry<?> node = findNode(dependency.key());
                if (node != null) {
                    Object value = node.getInstance(dependency, component);
                    value = dependency.wrapIfOptional(value);
                    field.setOnInstance(instance, value);
                } else if (dependency.isOptional()) {
                    // 3 Valgmuligheder

                    // Altid overskriv

                    // Overskriv Optional, ikke for nullable

                    // Aldrig overskriv

                    // I think we want to set optional fields????
                    // But not nullable
                    // I think
                    // if field == null, inject, otherwise leave to value
                    // Hmm, should we override existing value???
                    // For consistentsee reason yes, but hmm it is useful not to override
                } else {
                    String msg = "Could not find a valid value for " + dependency.key() + " on field " + field.toString();
                    throw new InjectionException(msg);
                }
            }
        }

        // Inject methods
        if (!descriptor.methods.isEmpty()) {
            for (OldAtInject method : descriptor.methods) {
                Object[] arguments = new Object[method.dependencies.size()];
                System.out.println(Arrays.toString(arguments));
                for (ServiceDependency dependency : method.dependencies) {
                    ServiceEntry<?> node = findNode(dependency.key());
                    System.out.println(node);
                }
                System.out.println("Should have injected " + method);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T use(Class<T> key) {
        T t = getInstanceOrNull(key);
        if (t != null) {
            return t;
        }
        failedGet(Key.of(key));
        throw new UnsupportedOperationException("No service with the specified key could be found, key = " + key);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T use(Key<T> key) {
        T t = getInstanceOrNull(key);
        if (t == null) {
            throw new UnsupportedOperationException("No service with the specified key could be found, key = " + key);
        }
        return t;
    }
}
/// ** {@inheritDoc} */
// @Override
// public final <T> T injectMembers(T instance, MethodHandles.Lookup lookup) {
// requireNonNull(instance, "instance is null");
// requireNonNull(lookup, "lookup is null");
// AtInjectGroup scd = MemberScanner.forService(instance.getClass(), lookup).inject.build();
// injectMembers(scd, instance, null);
// return instance;
// }
// Better help
//
// public static String getInstanceNotFound(Key<?> key) {
// return "No service of the specified type is available [type = " + key + "]. You can use " +
// OldInjector.class.getSimpleName()
// + "#getAvailableServices() to find out what kind of services are available";
// }
//
//// Think we can put in some more help, when people try inject "strange things"
//// InjectionSite can only be injected into non-static methods annotated with @Provides
//
// public static String getProviderNotFound(Key<?> key) {
// return "No service of the specified type is available [type = " + key + "]. You can use " +
// OldInjector.class.getSimpleName()
// + "#getAvailableServices() to find out what kind of services are available";
// }