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
package packed.internal.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;

import app.packed.inject.Provider;
import packed.internal.inject.ServiceDependency;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 * <p>
 * Yes the name is intentional.
 */
final class MethodHandleBuilderStatics {

    static final MethodHandle WRAP_OPTIONAL = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), ServiceDependency.class, "wrapIfOptional", Object.class,
            Object.class);

    static final MethodHandle OPTIONAL_EMPTY = LookupUtil.mhStaticPublic(Optional.class, "empty", Optional.class);

    static final MethodHandle OPTIONAL_OF = LookupUtil.mhStaticPublic(Optional.class, "of", Optional.class, Object.class);

    static final MethodHandle OPTIONAL_OF_NULLABLE = LookupUtil.mhStaticPublic(Optional.class, "ofNullable", Optional.class, Object.class);

    static final MethodHandle optionalOfTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(MethodHandleBuilderStatics.OPTIONAL_OF, MethodType.methodType(Optional.class, type));
    }

    static final MethodHandle optionalOfNullableTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(MethodHandleBuilderStatics.OPTIONAL_OF_NULLABLE, MethodType.methodType(Optional.class, type));
    }

    static class InvokeExactProvider<T> implements Provider<T> {

        private final MethodHandle mh;

        InvokeExactProvider(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        /** {@inheritDoc} */
        @Override
        public T provide() {
            try {
                return (T) mh.invokeExact();
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(ThrowableUtil.throwIfUnchecked(e));
            }
        }
    }

}
