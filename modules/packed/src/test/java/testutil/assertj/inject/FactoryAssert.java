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
import app.packed.base.TypeToken;
import app.packed.operation.op.Op;

/**
 *
 */
public class FactoryAssert<T> extends AbstractAssert<FactoryAssert<T>, Op<T>> {

    public FactoryAssert(Op<T> actual) {
        super(actual, FactoryAssert.class);
    }

    public FactoryAssert<T> hasNoDependencies() {
//        if (!actual.dependencies().isEmpty()) {
//            failWithMessage("\nExpecting no dependencies for the factory, but was '%s'", actual.dependencies());
//        }

        // TODO add depenndecy checks
        return this;
    }

    public FactoryAssert<T> is(Class<?> type) {
        return is(Key.of(type));
    }

    public FactoryAssert<T> is(TypeToken<?> type) {
        return is(Key.convertTypeLiteral(type));
    }

    public FactoryAssert<T> is(Key<?> type) {
        isNotNull();
//        Key<?> key = actual.key();
//        if (!key.equals(type)) {
//            failWithMessage("\nExpecting key of type '%s' but was '%s'", key, type);
//        }
//        TypeToken<?> typeLiteral = actual.typeLiteral();
//        if (!typeLiteral.equals(key.typeToken())) {
//            failWithMessage("\nExpecting TypeLiteral of type '%s' but was '%s'", key.typeToken(), typeLiteral);
//        }
//        Class<?> rawType = actual.rawType();
//        if (!rawType.equals(key.typeToken().rawType())) {
//            failWithMessage("\nExpecting Raw type of type '%s' but was '%s'", key.typeToken(), typeLiteral);
//        }
        return this;
    }

}
