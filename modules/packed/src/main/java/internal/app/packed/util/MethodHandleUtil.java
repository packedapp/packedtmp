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
package internal.app.packed.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.operation.Provider;

/**
 *
 */
public class MethodHandleUtil {

    /** A method handle that calls {@link OptionalDouble#of(double)} (double)OptionalDouble. */
    public static final MethodHandle PROVIDER_GET = LookupUtil.findVirtualPublic(Provider.class, "get", Object.class);

    /** A method handle that calls {@link OptionalDouble#of(double)} (double)OptionalDouble. */
    public static final MethodHandle OPTIONAL_DOUBLE_OF = LookupUtil.findStaticPublic(OptionalDouble.class, "of", OptionalDouble.class, double.class);

    /** A method handle that calls {@link OptionalInt#of(int)} (int)OptionalInt. */
    public static final MethodHandle OPTIONAL_INT_OF = LookupUtil.findStaticPublic(OptionalInt.class, "of", OptionalInt.class, int.class);

    /** A method handle that calls {@link OptionalLong#of(long)} (long)OptionalLong. */
    public static final MethodHandle OPTIONAL_LONG_OF = LookupUtil.findStaticPublic(OptionalLong.class, "of", OptionalLong.class, long.class);

    public static final MethodHandle OPTIONAL_OF = LookupUtil.findStaticPublic(Optional.class, "of", Optional.class, Object.class);

    public static final MethodHandle OPTIONAL_OF_NULLABLE = LookupUtil.findStaticPublic(Optional.class, "ofNullable", Optional.class, Object.class);

    public static final MethodHandle optionalOfNullableTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(OPTIONAL_OF_NULLABLE, MethodType.methodType(Optional.class, type));
    }

    public static final MethodHandle optionalOfTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(OPTIONAL_OF, MethodType.methodType(Optional.class, type));
    }

    public static void main(String[] args) {
        System.out.println(OPTIONAL_DOUBLE_OF.type());
        System.out.println(optionalOfTo(String.class).type());

    }
}

//
//public static MethodHandle getFromField(int modifiers, VarHandle vh) {
//  return Modifier.isVolatile(modifiers) ? vh.toMethodHandle(AccessMode.GET_VOLATILE) : vh.toMethodHandle(AccessMode.GET);
//}

//public static MethodHandle castReturnTypeIfNeeded(MethodHandle target, Class<?> returnType) {
//    if (returnType != target.type().returnType()) {
//        return castReturnType(target, returnType);
//    }
//    return target;
//}

//public static MethodHandle insertFakeParameter(MethodHandle target, Class<?> type) {
//    return insertFakeParameter(target, 0, type);
//}
//
//public static MethodHandle insertFakeParameter(MethodHandle target, int position, Class<?> type) {
//    return MethodHandles.dropArguments(target, position, type);
//}

//public static MethodHandle throwSupplying(MethodType type, Supplier<? extends Throwable> supplier) {
//   MethodHandle empty = MethodHandles.empty(type);
//
//   empty.asType(type)
//   MethodHandles.filterArguments( OPTIONAL_EMPTY, 0, null)
//    return MethodHandles.filterArguments(target, position, replaceWith);
//}
