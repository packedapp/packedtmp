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
package testutil.assertj.packed.inject.factory;

import org.assertj.core.api.AbstractAssert;

import app.packed.base.Key;
import app.packed.base.TypeLiteral;
import packed.internal.inject.factoryhandle.FactoryHandle;

/**
 *
 */
public class InternalFunctionAssert<T> extends AbstractAssert<InternalFunctionAssert<T>, FactoryHandle<T>> {

    public InternalFunctionAssert(FactoryHandle<T> actual) {
        super(actual, InternalFunctionAssert.class);
    }

    public InternalFunctionAssert<T> hasBound(Class<?> lowerBound, Class<?> upperBound) {
        isNotNull();
        // if (actual.getLowerBound() != lowerBound) {
        // failWithMessage("\nExpecting lower bound of '%s' but was '%s'", lowerBound, actual.getLowerBound());
        // }
        return this;
    }

    public InternalFunctionAssert<T> is(Class<?> type) {
        return is(Key.of(type));
    }

    // public InternalFunctionAssert<T> instantiateIs(Object expected, Object... args) {
    // isNotNull();
    // T result = actual.invoke(args);
    // if (!expected.equals(result)) {
    // failWithMessage("\nExpecting instantiate to return '%s' but was '%s'", expected, result);
    // }
    // return this;
    // }

    public InternalFunctionAssert<T> is(TypeLiteral<?> type) {
        return is(type.toKey());
    }

    public InternalFunctionAssert<T> is(Key<?> type) {
        isNotNull();
        // Key<?> key = actual.key;
        // if (!key.equals(type)) {
        // failWithMessage("\nExpecting key of type '%s' but was '%s'", key, type);
        // }
        // TypeLiteral<?> typeLiteral = actual.getType();
        // if (!typeLiteral.equals(key.getTypeLiteral())) {
        // failWithMessage("\nExpecting TypeLiteral of type '%s' but was '%s'", key.getTypeLiteral(), typeLiteral);
        // }
        return this;
    }

}
