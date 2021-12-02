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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import app.packed.base.TypeToken;

/**
 *
 */

// Maaske extender man bare Factory??? IDK Syntes maaske det er fint

abstract class CapturingFactory<R> extends Factory<R> {

    /** A cache of extracted type variables from subclasses of this class. */
    static final ClassValue<TypeToken<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })

        protected TypeToken<?> computeValue(Class<?> type) {
            return TypeToken.fromTypeVariable((Class) type, Factory.class, 0);
        }
    };

    final Object instance;

    /**
     * Used by the various FactoryN constructor, because we cannot call {@link Object#getClass()} before calling a
     * constructor in this (super) class.
     * 
     * @param instance
     *            the function instance
     */
    protected CapturingFactory(Object instance) {
        super();
        this.instance = requireNonNull(instance); // should have already been checked by subclasses
       // analyze();
    }

    void analyze() {
        // Altsaa jeg ved ikke om vi spiller tiden ved ikke at afvente og se hvad der kommer med generiks
        
        Class<?> t = getClass();
        Class<?> n = t.getSuperclass();
        while (n.getSuperclass() != CapturingFactory.class) {
            n = n.getSuperclass();
        }
        Constructor<?>[] con = n.getDeclaredConstructors();
        if (con.length != 1) {
            throw new Error(n + " must declare a single constructor");
        }
        Constructor<?> c = con[0];
        if (c.getParameterCount() != 1) {
            throw new Error(n + " must declare a single constructor taking a single parameter");
        }

        Parameter p = c.getParameters()[0];

        Class<?> samType = p.getType();
        Method m = samType.getMethods()[0];

        // check SAM interface type

        MethodHandle mh;
        try {
            mh = MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new Error(m + " must be accessible via MethodHandles.publicLookup()", e);
        }
        System.out.println(mh);
    }

    public static void main(String[] args) {
        new Factory0<>(() -> "") {};
    }

    // Vi har 2 af dem, ind omkring Factory0 og en for ExtendsFactory0
    // Den for Factory0 skal have MethodHandlen... og noget omkring antallet af dependencies
    static class FactoryMetadata {
        // find single Constructor... extract information about function type

        // must be a public type readable for anyone

        // create MH to access it

        // store it

        // and keep it for all furt
    }
}
