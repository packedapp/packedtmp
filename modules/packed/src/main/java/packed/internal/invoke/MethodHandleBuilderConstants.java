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
package packed.internal.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

import packed.internal.bean.inject.InternalDependency;
import packed.internal.util.LookupUtil;

/**
 *
 * <p>
 * Yes the name is intentional.
 */
final class MethodHandleBuilderConstants {

    static final MethodHandle WRAP_OPTIONAL = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), InternalDependency.class, "wrapIfOptional", Object.class,
            Object.class);

    static final MethodHandle OPTIONAL_EMPTY = LookupUtil.lookupStaticPublic(Optional.class, "empty", Optional.class);

    static final MethodHandle OPTIONAL_OF = LookupUtil.lookupStaticPublic(Optional.class, "of", Optional.class, Object.class);

    static final MethodHandle OPTIONAL_OF_NULLABLE = LookupUtil.lookupStaticPublic(Optional.class, "ofNullable", Optional.class, Object.class);

    static final MethodHandle optionalOfTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(MethodHandleBuilderConstants.OPTIONAL_OF, MethodType.methodType(Optional.class, type));
    }

    static final MethodHandle optionalOfNullableTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(MethodHandleBuilderConstants.OPTIONAL_OF_NULLABLE, MethodType.methodType(Optional.class, type));
    }
}
