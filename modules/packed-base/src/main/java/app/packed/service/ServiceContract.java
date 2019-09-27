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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import app.packed.artifact.ArtifactImage;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.contract.Contract;
import app.packed.util.Key;

/**
 * A service contract consists of two parts. A part detailing which services are required in order for owning entity to
 * be successfully constructed. And another part that the details what kind of services the owning entity provides after
 * having being constructed.
 * 
 * Both the consuming side and the providing part
 * 
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
public final class ServiceContract extends Contract {

    /** A contract with no requirements and providing no services. */
    public static final ServiceContract EMPTY = new ServiceContract(new Builder(), new HashSet<>());

    /** An immutable set of optional service keys. */
    private final Set<Key<?>> optional;

    /** An immutable set of provided service keys. */
    private final Set<Key<?>> provides;

    /** An immutable set of required service keys. */
    private final Set<Key<?>> requires;

    /**
     * Creates a new service contract from the specified builder.
     * 
     * @param builder
     *            the builder to create a service contract from
     */
    private ServiceContract(ServiceContract.Builder builder, HashSet<Key<?>> optional) {
        HashSet<Key<?>> s = builder.requires;
        this.requires = s == null ? Set.of() : Set.copyOf(s);

        s = optional;
        this.optional = s == null ? Set.of() : Set.copyOf(s);

        s = builder.provides;
        this.provides = s == null ? Set.of() : Set.copyOf(s);
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
        return optional.hashCode() + provides.hashCode() + requires.hashCode();
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

    /**
     * Returns an immutable set of all of the optional service keys of the owning entity.
     * 
     * @return an immutable set of all of the optional service keys of the owning entity
     */
    public Set<Key<?>> optional() {
        return optional;
    }

    /**
     * Returns an immutable set of keys for which a service <b>must</b> be made by the owning entity.
     * 
     * @return an immutable set of all keys that <b>must</b> be made available to the entity
     */
    public Set<Key<?>> requires() {
        return requires;
    }

    /**
     * Returns an immutable set of keys of all of the services the owning entity provides.
     * 
     * @return an immutable set of keys of all of the services the owning entity provides
     */
    public Set<Key<?>> services() {
        return provides;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = (optional.isEmpty() ? 0 : 1) + (provides.isEmpty() ? 0 : 1) + (requires.isEmpty() ? 0 : 1);
        if (count == 0) {
            return "InjectorContract {}";
        }
        sb.append("InjectorContract {");
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
     * Returns a new service contract builder.
     * 
     * @return a new service contract builder
     */
    public static ServiceContract.Builder builder() {
        return new ServiceContract.Builder();
    }

    /**
     * Returns a service contract from the specified image. Or fails with {@link UnsupportedOperationException}. if the a
     * contract
     * 
     * @param image
     * @return the contract
     */
    // Or should I return an empty contract???? Hmmmmmmmmmmmm
    // Or an optional.. Technically the constract does not exist.
    // Implications for other extension
    // ofElseEmpty();
    public static ServiceContract of(ArtifactImage image) {
        return BundleDescriptor.of(image).contracts().use(ServiceContract.class);
    }

    public static ServiceContract of(Bundle bundle) {
        Optional<ServiceContract> o = Contract.get(BundleDescriptor.of(bundle).contracts(), ServiceContract.class);
        return o.orElse(ServiceContract.EMPTY);
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
    public static ServiceContract of(Consumer<? super ServiceContract.Builder> action) {
        requireNonNull(action, "action is null");
        ServiceContract.Builder b = new ServiceContract.Builder();
        action.accept(b);
        return b.build();
    }

    /**
     * Since an injector has already been initialized it always has no requirements.
     * 
     * @param injector
     *            the injector to return a contract for
     * @return the service contract for an injector
     */
    public static ServiceContract ofInjector(Injector injector) {
        return of(c -> injector.services().forEach(s -> c.addProvides(s.key())));
    }

    public static ServiceContract ofRequired(Class<?>... keys) {
        return of(b -> List.of(keys).forEach(k -> b.addRequires(k)));
    }

    public static ServiceContract ofRequired(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

    public static ServiceContract ofServices(Class<?>... keys) {
        Builder b = builder();
        List.of(keys).forEach(k -> b.addProvides(k));
        return b.build();
    }

    static ServiceContract ofServices(ServiceContract contract) {
        // En contract, der kun inkludere provides services, men ikke requirements
        return contract;
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
    public static class Builder {

        /** The provided services. */
        private HashSet<Key<?>> optional;

        /** The provided services. */
        private HashSet<Key<?>> provides;

        /** The required services. */
        private HashSet<Key<?>> requires;

        /** Creates a new service contract builder */
        private Builder() {}

        /**
         * Creates a new contract builder builder from an existing service contract.
         * 
         * @param contract
         *            the contract to create a contract builder builder from
         */
        public Builder(ServiceContract contract) {
            requireNonNull(contract, "contract is null");
            requires = new HashSet<>(contract.requires);
        }

        /**
         * 
         * @param contract
         *            the contract to remove
         * @return this builder
         */
        public ServiceContract.Builder add(ServiceContract contract) {
            requireNonNull(contract, "contract is null");
            contract.optional.forEach(k -> addOptional(k));
            contract.provides.forEach(k -> addProvides(k));
            contract.requires.forEach(k -> addRequires(k));
            return this;
        }

        public ServiceContract.Builder addOptional(Class<?> key) {
            return addOptional(Key.of(key));
        }

        /**
         * Adds the specified key to the list of optional services.
         * 
         * @param key
         *            the key to add
         * @return this builder
         */
        public ServiceContract.Builder addOptional(Key<?> key) {
            requireNonNull(key, "key is null");
            HashSet<Key<?>> r = optional;
            if (r == null) {
                r = optional = new HashSet<>();
            }
            r.add(key);
            return this;
        }

        public ServiceContract.Builder addProvides(Class<?> key) {
            return addProvides(Key.of(key));
        }

        public ServiceContract.Builder addProvides(Key<?> key) {
            requireNonNull(key, "key is null");
            HashSet<Key<?>> r = provides;
            if (r == null) {
                r = provides = new HashSet<>();
            }
            r.add(key);
            return this;
        }

        /**
         * Adds the specified key to the list of required services.
         * 
         * @param key
         *            the key to add
         * @return this builder
         */
        public ServiceContract.Builder addRequires(Class<?> key) {
            return addRequires(Key.of(key));
        }

        /**
         * Adds the specified key to the list of required services.
         * 
         * @param key
         *            the key to add
         * @return this builder
         */
        public ServiceContract.Builder addRequires(Key<?> key) {
            requireNonNull(key, "key is null");
            HashSet<Key<?>> r = requires;
            if (r == null) {
                r = requires = new HashSet<>();
            }
            r.add(key);
            return this;
        }

        /**
         * Builds and returns a new service contract from this builder.
         * <p>
         * If there are keys that have both been added as a required and required optionally. The keys under required optionally
         * will be removed.
         * 
         * @return the new service contract
         * @throws IllegalStateException
         *             if any keys have been registered both as optional and required
         */
        public ServiceContract build() {
            if ((optional == null || optional.isEmpty()) && (requires == null || requires.isEmpty()) && (provides == null || provides.isEmpty())) {
                return ServiceContract.EMPTY;
            }

            // Remove optional keys that are also required.
            HashSet<Key<?>> opt = optional;
            if (optional != null && !optional.isEmpty() && requires != null && !requires.isEmpty()) {
                ArrayList<Key<?>> duplicates = null;
                for (Key<?> k : requires) {
                    if (optional.contains(k)) {
                        ArrayList<Key<?>> d = duplicates;
                        if (d == null) {
                            d = duplicates = new ArrayList<>(1);
                        }
                        d.add(k);
                    }
                }
                if (duplicates != null) {
                    opt = new HashSet<>(optional);
                    opt.removeAll(duplicates);
                }
            }
            return new ServiceContract(this, opt);
        }

        /**
         * @param contract
         *            the contract to remove
         * @return this builder
         */
        public ServiceContract.Builder remove(ServiceContract contract) {
            requireNonNull(contract, "contract is null");
            contract.optional.forEach(k -> removeOptional(k));
            contract.provides.forEach(k -> removeProvides(k));
            contract.requires.forEach(k -> removeRequires(k));
            return this;
        }

        public ServiceContract.Builder removeOptional(Class<?> key) {
            return removeOptional(Key.of(key));
        }

        /**
         * @param key
         *            the key to remove
         * @return this builder
         */
        public ServiceContract.Builder removeOptional(Key<?> key) {
            requireNonNull(key, "key is null");
            if (optional != null) {
                optional.remove(key);
            }
            return this;
        }

        public ServiceContract.Builder removeProvides(Class<?> key) {
            return removeProvides(Key.of(key));
        }

        /**
         * @param key
         *            the key to remove
         * @return this builder
         */
        public ServiceContract.Builder removeProvides(Key<?> key) {
            requireNonNull(key, "key is null");
            if (provides != null) {
                provides.remove(key);
            }
            return this;
        }

        public ServiceContract.Builder removeRequires(Class<?> key) {
            return removeRequires(Key.of(key));
        }

        /**
         * @param key
         *            the key to remove
         * @return this builder
         */
        public ServiceContract.Builder removeRequires(Key<?> key) {
            requireNonNull(key, "key is null");
            if (requires != null) {
                requires.remove(key);
            }
            return this;
        }

        // return a view, the mutable set, or an immutable copy????
        // Needs to be consistant with other builders.

        // I think returning the mutable set is bad. Because people could put something that is not keys in it.
        // And we would have to check.....

        // That makes it a view, or an immutable copy.
        // Taenker vi skal vaere konsekvent for buildere...
        public Set<Key<?>> requires() {
            throw new UnsupportedOperationException();
        }
    }
}
