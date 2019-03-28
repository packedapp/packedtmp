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

import java.util.HashSet;
import java.util.Set;

import app.packed.util.Key;

/**
 * A service contract consists of two parts. A part detailing which services are requiremented in order to be
 * successfull constructed. And another part that the details what kind of services the entity provides after being
 * constructed.
 * 
 * detailing the types of services that an entity needs for succesful construction can provide.
 * 
 * requires Serviced that are required before the entity can be properly constructed.
 * 
 * optional Optional services that the entity might use if available.
 * 
 * provides Service that the entity makes available to users of the entity.
 */
// Vi dropper description...
// Bliver sgu noedt til at have APIStatus paa her paa here

// Contract.setApiStatus()<- Will override
public final class ServiceContract {

    /** A service contract that has no requirements (optional or mandatory) and provides no services. */
    public static final ServiceContract EMPTY = new Builder().build();

    // add/list/remove requires 2,1,2
    // add/list/remove optional 2,1,2
    // add/list/remove provides 2,1,2

    // 15 metoder...
    // For api maa det vaere 1, 1 ,1 -> 9 ialt

    /** The provided services. */
    private final Set<Key<?>> optional;

    /** The provided services. */
    private final Set<Key<?>> provides;

    /** An immutable set of required keys. */
    private final Set<Key<?>> requires;

    /**
     * Creates a new service contract from the specified builder.
     * 
     * @param builder
     *            the builder to create a service contract from
     */
    private ServiceContract(ServiceContract.Builder builder) {
        requireNonNull(builder, "builder is null");

        HashSet<Key<?>> s = builder.requires;
        requires = s == null ? Set.of() : Set.copyOf(s);

        s = builder.optional;
        optional = s == null ? Set.of() : Set.copyOf(s);

        s = builder.provides;
        provides = s == null ? Set.of() : Set.copyOf(s);
    }

    /**
     * if all exposed services in the previous services are also exposed in this services. And if all required services in
     * this are also required services in the previous.
     * 
     * @param previous
     *            the previous contract
     * @return whether or not the specified service are back
     */
    boolean isBackwardsCompatibleWith(ServiceContract previous) {
        requireNonNull(previous, "previous is null");
        if (!previous.requires.containsAll(requires)) {
            return false;
        }
        if (!provides.containsAll(previous.provides)) {
            return false;
        }
        return true;
    }

    public Set<Key<?>> optional() {
        return optional;
    }

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

    /**
     *
     * This object is not thread safe
     * 
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

        public Builder() {}

        public Builder(ServiceContract contract) {
            requireNonNull(contract, "contract is null");
            requires = new HashSet<>(contract.requires);
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

        public ServiceContract.Builder addOptional(Key<?> key) {
            requireNonNull(key, "key is null");
            HashSet<Key<?>> r = optional;
            if (r == null) {
                r = optional = new HashSet<>();
            }
            r.add(key);
            return this;
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
         * 
         * @return the new service contract
         */
        public ServiceContract build() {
            return new ServiceContract(this);
        }

        public ServiceContract.Builder removeRequires(Class<?> key) {
            return removeRequires(Key.of(key));
        }

        /**
         * @param contract
         *            the contract to remove
         * @return this builder
         */
        public ServiceContract.Builder remove(ServiceContract contract) {
            // Add add
            return this;
        }

        /**
         * @param key
         *            the key to remove
         * @return this builder
         */
        public ServiceContract.Builder removeRequires(Key<?> key) {
            return this;
        }

        // return a view, the mutable set, or an immutable copy????
        // Needs to be consistant with other builders.
        public Set<Key<?>> requires() {
            throw new UnsupportedOperationException();
        }
    }
}
