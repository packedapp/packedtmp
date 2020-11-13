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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import app.packed.base.Key;
import app.packed.bundle.ExtensionMember;
import app.packed.component.Component;
import app.packed.component.ComponentAnalyzer;
import app.packed.component.ComponentSubSystem;
import packed.internal.component.ComponentBuild;
import packed.internal.inject.service.ServiceComposer;

/**
 * A service contract details of a contractee.
 * 
 * services are needed (represented by a Key)
 * 
 * 
 * A service contract consists of two parts. A part detailing which services are required in order for owning entity to
 * be successfully constructed. And another part that the details what kind of services the owning entity provides after
 * having being constructed.
 * 
 * Both the consuming side and the providing part
 * <p>
 * The required part is split into parts
 * 
 * 
 * optional Optional services that the entity might use if available.
 * 
 * provides Service that the entity makes available to users of the entity.
 */

// ------------ Injector Contract ---------------
// Contract (Contractee)
// Required Services [Services that the contractee requires that the other part provides]
// Optional Required Services [Services that the other part can optionally provide to the contractee]
// Provided Services [Services that the contractee provides to the other part]
// Rules
// * A service contract cannot have same dependency both as a required service and a optional service.
// Instead required should trump optional
// * A service contract cannot both have the same service as a requirement and provide it.
// * A key can only in one catagory at a time! Builder validates this....
// InjectorContract.of(new Bundle());

// ServiceContract

// Dependencies needed and services provided

// This class is typically used at container level.

// provides -> exports??? Nej.. taenker vi tager termerne fra Module systems
@ExtensionMember(ServiceExtension.class)
public final class ServiceContract {

    /** A contract with no requirements and no services provided. */
    public static final ServiceContract EMPTY = new ServiceContract(Set.of(), Set.of(), Set.of());

    /** An immutable set of optional service keys. */
    private final Set<Key<?>> optional;

    /** An immutable set of provided service keys. */
    private final Set<Key<?>> provides;

    /** An immutable set of required service keys. */
    private final Set<Key<?>> requires;

    /**
     * Creates a new service contract from the specified sets.
     */
    private ServiceContract(Set<Key<?>> requires, Set<Key<?>> optional, Set<Key<?>> provides) {
        this.requires = requireNonNull(requires);
        this.optional = requireNonNull(optional);
        this.provides = requireNonNull(provides);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof ServiceContract) {
            ServiceContract sc = (ServiceContract) other;
            return optional.equals(sc.optional) && provides.equals(sc.provides) && requires.equals(sc.requires);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return optional.hashCode() + 31 * (provides.hashCode() + 31 * requires.hashCode());
    }

    /**
     * A service contract is backwards compatibility with another service contract iff:
     * <ul>
     * <li>All the <strong>provided</strong> services in the previous contract are also <strong>provided</strong> in the new
     * contract.</li>
     * <li>The new contract introduces no new <strong>required</strong> services. It is allowed to add
     * <strong>optional</strong> services.</li>
     * </ul>
     * 
     * @param other
     *            the older contract
     * @return whether or not this contract is fully backwards compatible with the other contract
     */
    boolean isBackwardsCompatibleWith(ServiceContract other) {
        requireNonNull(other, "other is null");
        if (!other.requires.containsAll(requires)) {
            return false;
        }
        if (!provides.containsAll(other.provides)) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return optional.isEmpty() && provides.isEmpty() && requires.isEmpty();
    }

    /**
     * Creates a new builder that can be used to modify this contract. For example, by adding or removing keys.
     * 
     * @return the new builder
     */
    public ServiceContract.Builder modify() {
        return new ServiceContract.Builder(this);
    }

    /**
     * Returns an immutable set of all of the optional service keys of the owning entity.
     * 
     * @return an immutable set of all of the optional service keys of the owning entity
     */
    public Set<Key<?>> optional() {
        return optional;
    }

    public void print() {
        // ServiceContract.of(FooBundle()).print();
    }

    /**
     * Returns an immutable set of keys of all of the services the owning entity provides.
     * 
     * @return an immutable set of keys of all of the services the owning entity provides
     */
    public Set<Key<?>> provides() {
        return provides;
    }

    /**
     * Returns an immutable set of keys for which a service <b>must</b> be made by the owning entity.
     * 
     * @return an immutable set of all keys that <b>must</b> be made available to the entity
     */
    public Set<Key<?>> requires() {
        return requires;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = (optional.isEmpty() ? 0 : 1) + (provides.isEmpty() ? 0 : 1) + (requires.isEmpty() ? 0 : 1);
        if (count == 0) {
            return "ServiceContract {}";
        }
        sb.append("ServiceContract {");
        if (!requires.isEmpty()) {
            sb.append("\n  requires : " + requires.stream().map(e -> e.toString()).collect(Collectors.joining(", ")));
        }
        if (!optional.isEmpty()) {
            sb.append("\n  requires optional: " + optional.stream().map(e -> e.toString()).collect(Collectors.joining(", ")));
        }
        if (!provides.isEmpty()) {
            sb.append("\n  provides : " + provides.stream().map(e -> e.toString()).collect(Collectors.joining(", ")));
        }
        sb.append("\n}");
        return sb.toString();
    }

    /**
     * Creates a new service contract by performing the specified action on a new {@link ServiceContract.Builder } instance.
     * Usage:
     * 
     * which is equivalent to:
     * 
     * @param action
     *            the build action to perform
     * @return the new contract
     */
    public static ServiceContract build(Consumer<? super ServiceContract.Builder> action) {
        requireNonNull(action, "action is null");
        ServiceContract.Builder b = new ServiceContract.Builder(null);
        action.accept(b);
        return b.build();
    }

    /**
     * Returns a new service contract builder.
     * 
     * @return a new service contract builder
     */
    public static ServiceContract.Builder builder() {
        return new ServiceContract.Builder(null);
    }

    /**
     * Returns a service contract from the specified image. Or fails with {@link UnsupportedOperationException}. if the a
     * contract
     * 
     * @param system
     *            the image to return a contract for
     * @return the contract
     */
    // Or should I return an empty contract???? Hmmmmmmmmmmmm
    // Or an optional.. Technically the constract does not exist.
    // Implications for other extension
    // ofElseEmpty();
    // I Think optional, jeg kunne godt forstille mig en contract som ikke har noget der svarer til empty.
    // Men det er ogsaa fint.. Det her gaelder kun for ServiceContract...

    public static ServiceContract of(ComponentSubSystem system) {
        Component c = ComponentAnalyzer.analyze(system);
        if (!c.modifiers().isBundle()) {
            throw new IllegalArgumentException("Can only specify a system where the root component is a bundle, was " + c);
        }
        ComponentBuild compConf = ComponentBuild.unadapt(null, c);
        ServiceComposer sm = compConf.cube.getServiceManager();
        return sm == null ? ServiceContract.EMPTY : sm.newServiceContract();
    }

    /**
     * A builder object used to create instances of {@link ServiceContract}.
     * <p>
     * In addition to creating new contracts, this class also supports creating new contracts by transforming an existing
     * contracts using the xxx constructor.
     * <p>
     * <strong>Note that this builder is not synchronized.</strong> If multiple threads access a builder concurrently, and
     * at least one of the threads modifies the builder structurally, it <i>must</i> be synchronized externally.
     */
    public static final class Builder {

        private static final Integer OPTIONAL = 1;
        private static final Integer PROVIDES = 2;
        private static final Integer REQUIRES = 3;

        private final HashMap<Key<?>, Integer> map = new HashMap<>();

        /**
         * Creates a new contract builder builder from an existing service contract.
         * 
         * @param existing
         *            the contract to create a contract builder builder from
         */
        private Builder(ServiceContract existing) {
            if (existing != null) {
                existing.optional.forEach(k -> map.put(k, OPTIONAL));
                existing.provides.forEach(k -> map.put(k, PROVIDES));
                existing.requires.forEach(k -> map.put(k, REQUIRES));
            }
        }

        /**
         * Builds and returns a new service contract from this builder.
         * 
         * 
         * @return the new service contract
         * @throws IllegalStateException
         *             if any keys have been registered both as optional and required
         */
        public ServiceContract build() {
            if (map.isEmpty()) {
                return ServiceContract.EMPTY;
            }

            HashSet<Key<?>> tmpOptional = new HashSet<>();
            HashSet<Key<?>> tmpProvides = new HashSet<>();
            HashSet<Key<?>> tmpRequires = new HashSet<>();

            map.forEach((k, v) -> {
                if (v == OPTIONAL) {
                    tmpOptional.add(k);
                } else if (v == PROVIDES) {
                    tmpProvides.add(k);
                } else {
                    tmpRequires.add(k);
                }
            });
            return new ServiceContract(Set.copyOf(tmpOptional), Set.copyOf(tmpProvides), Set.copyOf(tmpRequires));
        }

        private Builder compute(Integer type, Key<?>... keys) {
            requireNonNull(keys, "keys is null");
            for (int i = 0; i < keys.length; i++) {
                Key<?> key = keys[i];
                requireNonNull(key, "Specified array of keys, contained a null at index " + i);
                map.merge(key, type, (oldValue, newValue) -> {
                    if (oldValue == newValue) {
                        return oldValue;
                    } else if (oldValue == PROVIDES) {
                        throw new IllegalArgumentException();
                        // fail because new is optional or provides
                    } else if (newValue == PROVIDES) {
                        throw new IllegalArgumentException("Cannot provide a key, when it is already part of the requirements, key = " + key);
                        // fail because already optional or provides
                    }
                    return REQUIRES; // Includes "upgrade" from Optional->Requires
                });
            }
            return this;
        }

        public ServiceContract.Builder optional(Class<?>... keys) {
            return optional(Key.of(keys));
        }

        /**
         * Adds the specified key to the list of optional services.
         * 
         * @param keys
         *            the keys to add
         * @return this builder
         */
        public ServiceContract.Builder optional(Key<?>... keys) {
            return compute(OPTIONAL, keys);
        }

        public ServiceContract.Builder provides(Class<?>... keys) {
            return provides(Key.of(keys));
        }

        /**
         * <p>
         * If there are keys that have both been added as a required and required optionally. The keys under required optionally
         * will be removed.
         * 
         * @param keys
         *            the keys to add
         * @return this builder
         */
        public ServiceContract.Builder provides(Key<?>... keys) {
            return compute(PROVIDES, keys);
        }

        public ServiceContract.Builder remove(Class<?>... keys) {
            return remove(Key.of(keys));
        }

        /**
         * @param keys
         *            the keys to remove
         * @return this builder
         * @apiNote sin
         */
        public ServiceContract.Builder remove(Key<?>... keys) {
            requireNonNull(keys, "keys is null");
            map.keySet().removeAll(List.of(keys));
            return this;
        }

        /**
         * Adds the specified key to the list of required services.
         * 
         * @param keys
         *            the keys to add
         * @return this builder
         */
        public ServiceContract.Builder requires(Class<?>... keys) {
            return requires(Key.of(keys));
        }

        /**
         * Adds the specified key to the list of required services.
         * 
         * @param keys
         *            the keys to add
         * @return this builder
         */
        public ServiceContract.Builder requires(Key<?>... keys) {
            return compute(REQUIRES, keys);
        }
    }
}
