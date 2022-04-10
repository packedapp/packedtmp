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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.TypeToken;
import packed.internal.inject.ReflectiveFactory.ExecutableFactory;
import packed.internal.inject.ReflectiveFactory.FieldFactory;
import packed.internal.inject.bean.InternalDependency;
import packed.internal.invoke.MemberScanner;

/**
 * A factory that needs a {@link Lookup} object.
 */
// Maaske returnere ReflectionFactory med en lookup
// ReflectiveFactory
// LookupFactory (Fungere nok bedre hvis vi faar mirrors engang)
@SuppressWarnings("rawtypes")
public abstract sealed class ReflectiveFactory<T> extends InternalFactory<T>permits ExecutableFactory,FieldFactory {

    /** A cache of factories used by {@link #defaultFactoryFor(Class)}. */
    public static final ClassValue<ExecutableFactory<?>> DEFAULT_FACTORY = new ClassValue<>() {

        /** {@inheritDoc} */
        protected ExecutableFactory<?> computeValue(Class<?> implementation) {
            return new ExecutableFactory<>(TypeToken.of(implementation), implementation);
        }
    };

    
    private ReflectiveFactory(TypeToken<T> typeLiteralOrKey) {
        super(typeLiteralOrKey);
    }
  
    /** A factory that wraps a method or constructor. */
    public static final class ExecutableFactory<T> extends ReflectiveFactory<T> {

        private final List<InternalDependency> dependencies;

        /** A factory with an executable as a target. */
        public final Executable executable;

        public ExecutableFactory(ExecutableFactory<?> from, TypeToken<T> key) {
            super(key);
            this.executable = from.executable;
            this.dependencies = from.dependencies;
        }

        public ExecutableFactory(TypeToken<T> key, Class<?> findConstructorOn) {
            super(key);
            this.executable = MemberScanner.getConstructor(findConstructorOn, true, e -> new IllegalArgumentException(e));
            this.dependencies = InternalDependency.fromExecutable(executable);
        }

        public ExecutableFactory(TypeToken<T> key, Constructor<?> constructor) {
            super(key);
            this.executable = constructor;
            this.dependencies = InternalDependency.fromExecutable(executable);
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return dependencies;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle methodHandle;
            try {
                if (Modifier.isPrivate(executable.getModifiers())) {
                    lookup = lookup.in(executable.getDeclaringClass());
                }
                if (executable instanceof Constructor<?> c) {
                    methodHandle = lookup.unreflectConstructor(c);
                } else {
                    methodHandle = lookup.unreflect((Method) executable);
                }

            } catch (IllegalAccessException e) {
                String name = executable instanceof Constructor ? "constructor" : "method";
                throw new InaccessibleMemberException("No access to the " + name + " " + executable + " with the specified lookup object", e);
            }

            MethodHandle mh = methodHandle;
            if (executable.isVarArgs()) {
                mh = mh.asFixedArity();
            }
            return mh;
        }

        @Override
        public String toString() {
            return executable.toString();
        }
    }

    /** An invoker that can read and write fields. */
    public static final class FieldFactory<T> extends ReflectiveFactory<T> {

        /** The field we invoke. */
        private final Field field;

        @SuppressWarnings("unchecked")
        public FieldFactory(Field field) {
            super((TypeToken<T>) TypeToken.fromField(field));
            this.field = field;
        }

        /** {@inheritDoc} */

        @Override
        public List<InternalDependency> dependencies() {
            return List.of();
        }

        /**
         * Compiles the code to a single method handle.
         * 
         * @return the compiled method handle
         */

        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle handle;
            try {
                if (Modifier.isPrivate(field.getModifiers())) {
                    // vs MethodHandles.private???
                    lookup = lookup.in(field.getDeclaringClass());
                }
                handle = lookup.unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new InaccessibleMemberException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
            }
            return handle;
        }
    }

}
//
//public Factory<T> lookup() {
//  // Problemet er her at vi jo faktisk i mange tilfaelde vil laase hele beanen op????
//  // Taenker vi har metoderne paa BeanFactory
//
//  // Vi vil helst ikke have at vi overskrive metoder... Men det bliver vi jo noedt til at kunne
//  // hvis vi har subklasser
//  return this;
//}