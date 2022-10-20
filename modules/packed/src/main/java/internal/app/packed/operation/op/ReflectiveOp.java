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
package internal.app.packed.operation.op;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.operation.OperationType;
import internal.app.packed.operation.op.PackedOp.TerminalOp;
import internal.app.packed.operation.op.ReflectiveOp.ExecutableOp;

/**
 * A factory that needs a {@link Lookup} object.
 */
// Maaske returnere ReflectionFactory med en lookup
// ReflectiveFactory
// LookupFactory (Fungere nok bedre hvis vi faar mirrors engang)
@SuppressWarnings("rawtypes")
public abstract sealed class ReflectiveOp<T> extends TerminalOp<T>permits ExecutableOp {

    ReflectiveOp(OperationType type) {
        super(type);
    }

    /** A cache of factories used by {@link #factoryOf(Class)}. */
    public static final ClassValue<ExecutableOp<?>> DEFAULT_FACTORY = new ClassValue<>() {

        /** {@inheritDoc} */
        protected ExecutableOp<?> computeValue(Class<?> implementation) {
            Executable executable = ConstructorFinder.getConstructor(implementation, true, e -> new IllegalArgumentException(e));
            return new ExecutableOp<>(executable);
        }
    };

    /** A factory that wraps a method or constructor. */
    public static final class ExecutableOp<T> extends ReflectiveOp<T> {

        /** A factory with an executable as a target. */
        public final Executable executable;

        public ExecutableOp(Executable constructor) {
            super(OperationType.ofExecutable(constructor));
            this.executable = constructor;
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
                    // System.out.println(lookup);
                }
                if (executable instanceof Constructor<?> c) {
                    methodHandle = lookup.unreflectConstructor(c);
                } else {
                    methodHandle = lookup.unreflect((Method) executable);
                }

            } catch (IllegalAccessException e) {
                String name = executable instanceof Constructor ? "constructor" : "method";
                throw new InaccessibleBeanMemberException("No access to the " + name + " " + executable + " with the specified lookup object", e);
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
    public static final class FieldOp<T> extends TerminalOp<T> {

        /** The field we invoke. */
        final Field field;

        public FieldOp(OperationType type, Field field) {
            super(type);
            this.field = field;
        }
    }
}
