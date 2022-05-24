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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.TypeToken;
import app.packed.inject.InaccessibleMemberException;
import app.packed.inject.Variable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.factory.ReflectiveFactory.ExecutableFactory;
import packed.internal.inject.factory.ReflectiveFactory.FieldFactory;
import packed.internal.util.MemberScanner;

/**
 * A factory that needs a {@link Lookup} object.
 */
// Maaske returnere ReflectionFactory med en lookup
// ReflectiveFactory
// LookupFactory (Fungere nok bedre hvis vi faar mirrors engang)
@SuppressWarnings("rawtypes")
public abstract sealed class ReflectiveFactory<T> extends InternalFactory<T>permits ExecutableFactory, FieldFactory {

    /** A cache of factories used by {@link #factoryOf(Class)}. */
    public static final ClassValue<ExecutableFactory<?>> DEFAULT_FACTORY = new ClassValue<>() {

        /** {@inheritDoc} */
        protected ExecutableFactory<?> computeValue(Class<?> implementation) {
            return new ExecutableFactory<>(TypeToken.of(implementation), implementation);
        }
    };

    /** A factory that wraps a method or constructor. */
    public static final class ExecutableFactory<T> extends ReflectiveFactory<T> {

        private final List<InternalDependency> dependencies;

        /** A factory with an executable as a target. */
        public final Executable executable;

        /** The type of objects this factory creates. */
        private final TypeToken<T> typeLiteral;

        public ExecutableFactory(ExecutableFactory<?> from, TypeToken<T> key) {
            typeLiteral = requireNonNull(key);
            this.executable = from.executable;
            this.dependencies = from.dependencies;
        }

        public ExecutableFactory(TypeToken<T> key, Class<?> findConstructorOn) {
            typeLiteral = requireNonNull(key);
            this.executable = MemberScanner.getConstructor(findConstructorOn, true, e -> new IllegalArgumentException(e));
            this.dependencies = InternalDependency.fromExecutable(executable);
        }

        public ExecutableFactory(TypeToken<T> key, Constructor<?> constructor) {
            typeLiteral = requireNonNull(key);
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
                if (!Modifier.isPublic(executable.getModifiers())) {

//                    Class<?> c = executable.getDeclaringClass();

                    // For some reason the lookup objects that comes here might not have full privilege access
                    lookup = MethodHandles.lookup();
                    
//                    System.out.println(lookup.hasFullPrivilegeAccess());
//
//                    Module m1 = BeanExtension.class.getModule();
//                    Module m2 = executable.getDeclaringClass().getModule();
//
//                    System.out.println("Is Open " + m2.isOpen(c.getPackageName(), m1));
//
////                    lookup = lookup.in(executable.getDeclaringClass());
//
//  //                  lookup.accessClass(executable.getDeclaringClass());
//
//                    System.out.println(lookup);
//    
                    lookup = MethodHandles.privateLookupIn(executable.getDeclaringClass(), lookup);
                    //
                    //System.out.println(lookup);
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

        /** {@inheritDoc} */
        @Override
        public TypeToken<T> typeLiteral() {
            return typeLiteral;
        }

        /** {@inheritDoc} */
        @Override
        public int variableCount() {
            return dependencies.size();
        }
    }

    /** An invoker that can read and write fields. */
    public static final class FieldFactory<T> extends ReflectiveFactory<T> {

        /** The field we invoke. */
        private final Field field;

        /** The type of objects this factory creates. */
        private final TypeToken<T> typeLiteral;

        @SuppressWarnings("unchecked")
        public FieldFactory(Field field) {
            typeLiteral = (TypeToken<T>) TypeToken.fromField(field);
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

        /** {@inheritDoc} */
        @Override
        public TypeToken<T> typeLiteral() {
            return typeLiteral;
        }

        @Override
        public int variableCount() {
            return 0;
        }

        @Override
        public List<Variable> variables() {
            return List.of();
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