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
package support.assertj.app.packed.inject;

import org.assertj.core.api.AbstractAssert;

import app.packed.service.ServiceDependency;
import app.packed.util.Key;

/**
 *
 */
public class KeyAssert extends AbstractAssert<KeyAssert, ServiceDependency> {

    public KeyAssert keyIs(Class<?> type) {
        return keyIs(Key.of(type));
    }

    public KeyAssert keyIs(Key<?> type) {
        isNotNull();
        Key<?> key = actual.key();
        if (!key.equals(type)) {
            failWithMessage("\nExpecting key of type '%s' but was '%s'", key, type);
        }
        return this;
    }

    public KeyAssert isOptionalInt() {
        isNotNull();
        if (!actual.isOptional()) {
            failWithMessage("\nExpecting Dependency to be optional");
        }
        keyIs(Integer.class);
        return this;
    }

    public KeyAssert isOptional(Class<?> optionalType) {
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

    public KeyAssert isMandatory() {
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

    public KeyAssert(ServiceDependency actual) {
        super(actual, KeyAssert.class);
    }

    public static KeyAssert assertThat(ServiceDependency actual) {
        return new KeyAssert(actual);
    }

}
