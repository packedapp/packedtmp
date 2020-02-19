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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory;

/**
 *
 */

//Ideen er vi dropper disse. Fordi all metoder virker...
//Men instanse metoder skal man binde...
//Factory.findMethod(Doo.class, "dooo").bind(new Doo());

//static <T> Factory<T> fromMethodHandle(MethodHandle mh) {
//// We don't support this because annotations and generic information are stripped from MethodHandles.
//throw new UnsupportedOperationException();
//}
//Virker kun med noget der
//final MethodHandle toMethodHandle() {
////How does this method handle prime annotations????
////It does not, so maybe just jinx it...
//throw new UnsupportedOperationException();
//}

public class DeprecatedFactory {

    static <T> Factory<T> findInstanceMethod(Object onInstance, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    static <T> Factory<T> findInstanceMethod(Object onInstance, Class<T> returnType, String name) {
        throw new UnsupportedOperationException();
    }

    // Man kunne ogsaa bare sige man tog instance metoder...
    // Man saa skal man binde receiveren.....
    //// Dvs for instans metoder, saa bliver selve instancen en dependency...
    public static <T> Factory<T> fromMethodInstance(Object onInstance, Method method, Class<T> returnType) {
        requireNonNull(returnType, "returnType is null");
        return fromMethodInstance(onInstance, method, TypeLiteral.of(returnType));
    }

    public static <T> Factory<T> fromMethodInstance(Object onInstance, Method method, TypeLiteral<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");
        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }

    // How we skal have
    public static <T> Factory<T> fromMethodInstance(Object onInstance, TypeLiteral<T> returnType, String name, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }
}
