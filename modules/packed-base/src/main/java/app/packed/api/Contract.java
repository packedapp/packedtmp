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
package app.packed.api;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.lang.Nullable;
import packed.internal.artifact.BuildOutput;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
// En contract er kun noedvendig naar vi har en taet wiring..
// Mellem 2 containere....
// F.eks. injection, lifecycle...

// Men ikke errorHandling, tror bare den eksistere
// Logging ihvertfald
// Eller AOP, eller maaske eksistere den bare.

// Use an interface????

public abstract class Contract {

    /** {@inheritDoc} */
    @Override
    public abstract boolean equals(Object obj);

    @SuppressWarnings("unchecked")
    @Nullable
    private final <T extends Contract> T get0(Class<T> contractType) {
        requireNonNull(contractType, "contractType is null");
        if (contractType == ContractSet.class) {
            throw new IllegalArgumentException("Cannot specify " + ContractSet.class.getSimpleName());
        }
        if (this instanceof ContractSet) {
            return (T) ((ContractSet) this).contracts.get(contractType);
        } else {
            return this.getClass() == contractType ? (T) this : null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public abstract int hashCode();

    /** {@inheritDoc} */
    @Override
    public abstract String toString();

    public static final boolean contains(Contract c, Class<? extends Contract> contractType) {
        requireNonNull(contractType, "contractType is null");
        if (c instanceof ContractSet) {
            return ((ContractSet) c).contracts.containsKey(contractType);
        } else {
            return c.getClass() == contractType;
        }
    }

    public static final <T extends Contract> Optional<T> get(Contract c, Class<T> contractType) {
        return Optional.ofNullable(c.get0(contractType));
    }

    public static <T extends Contract> T use(Contract c, Class<T> contractType) {
        T t = c.get0(contractType);
        if (t == null) {
            throw new Error("Could not find contract, type = " + contractType);
        }
        return t;
    }

    public static <T extends Contract> Optional<T> of(Bundle bundle, Class<T> contractType) {
        requireNonNull(bundle, "bundle is null");
        PackedContainerConfiguration conf = new PackedContainerConfiguration(BuildOutput.descriptor(BundleDescriptor.class), bundle);
        conf.doBuild();
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundle.getClass());
        conf.buildDescriptor(builder);
        // BundleDescriptor d = builder.build();
        throw new UnsupportedOperationException();
    }

    // Should probably be abstract, or final, and then have a toString(ContractStringBuilder b);
    // @Override
    // public String toString() {
    // s
    // }

    // ContractSet
    // |-InjectorContract
}
