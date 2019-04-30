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
package app.packed.inject2;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import app.packed.extension.AnyBundle;
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
public final class ProvisionContract {

    /** A service contract that has no requirements and provides no services. */
    private static final ProvisionContract EMPTY = new Builder().build();

    /** An immutable set of provided service keys. */
    private final Set<Key<?>> provides;

    /**
     * Creates a new service contract from the specified builder.
     * 
     * @param builder
     *            the builder to create a service contract from
     */
    private ProvisionContract(ProvisionContract.Builder builder) {
        HashSet<Key<?>> s = builder.provides;
        provides = s == null ? Set.of() : Set.copyOf(s);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        // If we add description, api status, we probably need to have some more methods to test
        if (!(obj instanceof ProvisionContract)) {
            return false;
        }
        ProvisionContract sc = (ProvisionContract) obj;
        return provides.equals(sc.provides);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return provides.hashCode();
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
    boolean isBackwardsCompatibleWith(ProvisionContract other) {
        requireNonNull(other, "other is null");
        return provides.containsAll(other.provides);
    }

    /**
     * Returns an immutable set of keys of all of the services the owning entity provides.
     * 
     * @return an immutable set of keys of all of the services the owning entity provides
     */
    public Set<Key<?>> services() {
        return provides;
    }

    /**
     * Returns a new service contract builder.
     * 
     * @return a new service contract builder
     */
    public static ProvisionContract.Builder builder() {
        return new ProvisionContract.Builder();
    }

    /**
     * Returns an contract that provides no services.
     * 
     * @return an contract that provides no services
     */
    public static ProvisionContract empty() {
        return EMPTY;
    }

    /**
     * Returns a provision contract from the specified bundle.
     * 
     * @param bundle
     * @return
     */
    public static ProvisionContract of(AnyBundle bundle) {
        return EMPTY;
    }

    /**
     * A builder object used to create instances of {@link ProvisionContract}.
     * <p>
     * In addition to creating new contracts, this class also supports creating new contracts by transforming an existing
     * contracts using the {@link #ServiceContract(ProvisionContract)} constructor.
     * <p>
     * <strong>Note that this builder is not synchronized.</strong> If multiple threads access a builder concurrently, and
     * at least one of the threads modifies the builder structurally, it <i>must</i> be synchronized externally.
     */
    public static class Builder {

        /** The provided services. */
        private HashSet<Key<?>> provides = new HashSet<>();

        /** Creates a new service contract builder */
        private Builder() {}

        /**
         * Creates a new contract builder builder from an existing service contract.
         * 
         * @param contract
         *            the contract to create a contract builder builder from
         */
        public Builder(ProvisionContract contract) {
            requireNonNull(contract, "contract is null");
            provides = new HashSet<>(contract.provides);
        }

        /**
         * 
         * @param contract
         *            the contract to remove
         * @return this builder
         */
        public ProvisionContract.Builder add(ProvisionContract contract) {
            requireNonNull(contract, "contract is null");
            contract.provides.forEach(k -> addProvides(k));
            return this;
        }

        public ProvisionContract.Builder addProvides(Class<?> key) {
            return addProvides(Key.of(key));
        }

        public ProvisionContract.Builder addProvides(Key<?> key) {
            requireNonNull(key, "key is null");
            HashSet<Key<?>> r = provides;
            if (r == null) {
                r = provides = new HashSet<>();
            }
            r.add(key);
            return this;
        }

        /**
         * Builds and returns a new service contract from this builder.
         * 
         * @return the new service contract
         * @throws IllegalStateException
         *             if any keys have been registered both as optional and required
         */
        public ProvisionContract build() {
            return new ProvisionContract(this);
        }

        /**
         * @param contract
         *            the contract to remove
         * @return this builder
         */
        public ProvisionContract.Builder remove(ProvisionContract contract) {
            requireNonNull(contract, "contract is null");
            contract.provides.forEach(k -> removeProvides(k));
            return this;
        }

        public ProvisionContract.Builder removeProvides(Class<?> key) {
            return removeProvides(Key.of(key));
        }

        /**
         * @param key
         *            the key to remove
         * @return this builder
         */
        public ProvisionContract.Builder removeProvides(Key<?> key) {
            requireNonNull(key, "key is null");
            if (provides != null) {
                provides.remove(key);
            }
            return this;
        }
    }
}
