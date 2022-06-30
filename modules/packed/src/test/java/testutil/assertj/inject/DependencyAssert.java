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
package testutil.assertj.inject;

import org.assertj.core.api.AbstractAssert;

import app.packed.base.Key;
import internal.app.packed.inject.InternalDependency;

/**
 *
 */
public class DependencyAssert extends AbstractAssert<DependencyAssert, InternalDependency> {

    public DependencyAssert keyIs(Class<?> type) {
        return keyIs(Key.of(type));
    }

    public DependencyAssert keyIs(Key<?> type) {
        isNotNull();
        Key<?> key = actual.key();
        if (!key.equals(type)) {
            failWithMessage("\nExpecting key of type '%s' but was '%s'", key, type);
        }
        return this;
    }

    public DependencyAssert isOptionalInt() {
        isNotNull();
        if (!actual.isOptional()) {
            failWithMessage("\nExpecting Dependency to be optional");
        }
        keyIs(Integer.class);
        return this;
    }

    public DependencyAssert isOptional(Class<?> optionalType) {
        isNotNull();
        if (!actual.isOptional()) {
            failWithMessage("\nExpecting Dependency to be optional");
        }
        // Class<?> actualOptionalType = actual.optionalContainerType();
        // if (actualOptionalType != optionalType) {
        // failWithMessage("\nExpecting Optional Type of type '%s', but was %s", optionalType, actualOptionalType);
        // }
        return this;
    }

    public DependencyAssert isMandatory() {
        isNotNull();
        if (actual.isOptional()) {
            failWithMessage("\nExpecting Dependency to be non-optional");
        }

        // Class<?> optionalType = actual.optionalContainerType();
        // if (optionalType == null) {
        // failWithMessage("\nExpecting Optional Type to be non-null, but was %s", optionalType);
        // }
        return this;
    }

    public DependencyAssert(InternalDependency actual) {
        super(actual, DependencyAssert.class);
    }

    public static DependencyAssert assertThat(InternalDependency actual) {
        return new DependencyAssert(actual);
    }
}
