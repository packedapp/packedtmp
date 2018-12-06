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
package packed.internal.inject.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.container.Component;
import app.packed.inject.InjectionException;
import app.packed.inject.InjectionSite;
import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.Node;
import packed.internal.inject.function.FieldInvoker;
import packed.internal.inject.support.AtInject;
import packed.internal.invokers.ServiceClassDescriptor;

/** An abstract implementation of an injector. */
public abstract class AbstractInjector implements Injector {

    @Nullable
    protected <T> Node<T> findNode(Class<T> key) {
        requireNonNull(key, "key is null");
        return findNode(Key.of(key));
    }

    @Nullable
    protected abstract <T> Node<T> findNode(Key<T> key);

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
    final <T> T getInstanceOrNull(Class<T> key) {
        requireNonNull(key, "key is null");
        return getInstanceOrNull(Key.of(key));
    }

    @Nullable
    final <T> T getInstanceOrNull(Key<T> key) {
        Node<T> n = findNode(key);
        if (n == null) {
            return null;
        }
        return n.getInstance(InjectionSite.of(this, key));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean hasService(Class<?> key) {
        return findNode(key) != null;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean hasService(Key<?> key) {
        return findNode(key) != null;
    }

    protected final void injectMembers(ServiceClassDescriptor<?> descriptor, Object instance, @Nullable Component component) {
        // Inject fields
        if (!descriptor.inject.fields.isEmpty()) {
            for (AtInject atInject : descriptor.inject.fields) {
                InternalDependency dependency = atInject.dependencies.get(0);
                FieldInvoker<?> field = (FieldInvoker<?>) atInject.ifm;
                Node<?> node = findNode(dependency.getKey());
                if (node != null) {
                    Object value = node.getInstance(this, dependency, component);
                    value = dependency.wrapIfOptional(value);
                    field.setField(instance, value);
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
                    String msg = "Could not find a valid value for " + dependency.getKey() + " on field " + field.toString();
                    throw new InjectionException(msg);
                }
            }
        }

        // Inject methods
        if (!descriptor.inject.methods.isEmpty()) {
            for (AtInject method : descriptor.inject.methods) {
                Object[] arguments = new Object[method.dependencies.size()];
                System.out.println(arguments);
                for (InternalDependency dependency : method.dependencies) {
                    Node<?> node = findNode(dependency.getKey());
                    System.out.println(node);

                }
                System.out.println("Should have injected " + method);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T injectMembers(T instance, MethodHandles.Lookup lookup) {
        requireNonNull(instance, "instance is null");
        requireNonNull(lookup, "lookup is null");
        ServiceClassDescriptor<?> scd = ServiceClassDescriptor.from(lookup, instance.getClass());
        injectMembers(scd, instance, null);
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T with(Class<T> key) {
        T t = getInstanceOrNull(key);
        if (t != null) {
            return t;
        }
        failedGet(Key.of(key));
        throw new UnsupportedOperationException("No service with the specified key could  be found, key = " + key);
    }

    protected void failedGet(Key<?> key) {}

    /** {@inheritDoc} */
    @Override
    public final <T> T with(Key<T> key) {
        T t = getInstanceOrNull(key);
        if (t == null) {
            throw new UnsupportedOperationException("No service with the specified key could  be found, key = " + key);
        }
        return t;
    }
}
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