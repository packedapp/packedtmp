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
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Modifier;
import java.util.Optional;

import internal.app.packed.inject.InternalDependency;

/**
 *
 */
public class MethodHandleUtil {
    public static final MethodHandle WRAP_OPTIONAL = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), InternalDependency.class, "wrapIfOptional", Object.class,
            Object.class);

    public static final MethodHandle OPTIONAL_EMPTY = LookupUtil.lookupStaticPublic(Optional.class, "empty", Optional.class);

//    public static final MethodHandle SUPPLIER_GET = LookupUtil.lookupStaticPublic(Supplier.class, "get", Supplier.class, Object.class);

    public static final MethodHandle OPTIONAL_OF = LookupUtil.lookupStaticPublic(Optional.class, "of", Optional.class, Object.class);

    public static final MethodHandle OPTIONAL_OF_NULLABLE = LookupUtil.lookupStaticPublic(Optional.class, "ofNullable", Optional.class, Object.class);

    public static final MethodHandle optionalOfTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(OPTIONAL_OF, MethodType.methodType(Optional.class, type));
    }

    public static final MethodHandle optionalOfNullableTo(Class<?> type) {
        return MethodHandles.explicitCastArguments(OPTIONAL_OF_NULLABLE, MethodType.methodType(Optional.class, type));
    }
    
    public static MethodHandle getFromField(int modifiers, VarHandle vh) {
        return Modifier.isVolatile(modifiers) ? vh.toMethodHandle(AccessMode.GET_VOLATILE) : vh.toMethodHandle(AccessMode.GET);
    }

    public static MethodHandle bind(MethodHandle target, int position, Object... arguments) {
        return MethodHandles.insertArguments(target, position, arguments);
    }

    public static MethodHandle castReturnType(MethodHandle target, Class<?> newReturnType) {
        return target.asType(target.type().changeReturnType(newReturnType));
    }

    public static MethodHandle castReturnTypeIfNeeded(MethodHandle target, Class<?> returnType) {
        if (returnType != target.type().returnType()) {
            return castReturnType(target, returnType);
        }
        return target;
    }

//    public static MethodHandle insertFakeParameter(MethodHandle target, Class<?> type) {
//        return insertFakeParameter(target, 0, type);
//    }
//
//    public static MethodHandle insertFakeParameter(MethodHandle target, int position, Class<?> type) {
//        return MethodHandles.dropArguments(target, position, type);
//    }

    public static MethodHandle replaceParameter(MethodHandle target, int position, MethodHandle replaceWith) {
        return MethodHandles.filterArguments(target, position, replaceWith);
    }
    
//    public static MethodHandle throwSupplying(MethodType type, Supplier<? extends Throwable> supplier) {
//       MethodHandle empty = MethodHandles.empty(type);
//       
//       empty.asType(type)
//       MethodHandles.filterArguments( OPTIONAL_EMPTY, 0, null)
//        return MethodHandles.filterArguments(target, position, replaceWith);
//    }
    
}
