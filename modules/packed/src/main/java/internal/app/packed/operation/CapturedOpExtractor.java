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
package internal.app.packed.operation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import app.packed.binding.Variable;
import app.packed.operation.CapturingOp;
import app.packed.operation.OperationType;
import internal.app.packed.binding.InternalDependency;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 *
 */
class CapturedOpExtractor {

    private static final ClassValue<Base> BASE = new ClassValue<Base>() {

        @Override
        protected Base computeValue(Class<?> type) {

            // Maaske er det fint at smide en error?
            Constructor<?>[] con = type.getDeclaredConstructors();
            if (con.length != 1) {
                throw new Error(type + " must declare a single constructor");
            }
            Constructor<?> c = con[0];
            if (c.getParameterCount() != 1) {
                throw new Error(type + " must declare a single constructor with a single parameter taking a function interface");
            }

            Parameter p = c.getParameters()[0];

            Class<?> functionalInterface = p.getType();

            SamType st = SamType.of(functionalInterface);
            return new Base(type, st);
        }
    };

    static final ClassValue<Top> TOP = new ClassValue<>() {

        @Override
        protected Top computeValue(Class<?> type) {
            Class<?> baseClass = type.getSuperclass();
            while (baseClass.getSuperclass() != CapturingOp.class) {
                baseClass = baseClass.getSuperclass();
            }
            Base b = BASE.get(baseClass);

            Variable[] types = b.tve.extractAllVariables(type, IllegalArgumentException::new);

            Variable last = types[types.length - 1];

            OperationType ot = OperationType.of(last, Arrays.copyOf(types, types.length - 1));

            return new Top(b, ot);
        }
    };

    static class Base {
        final SamType samType;

        final TypeVariableExtractor tve;

        Base(Class<?> baseType, SamType samType) {
            this.samType = samType;
            this.tve = TypeVariableExtractor.of(baseType);

            // TODO make methodHandle that can
        }
    }

    static class Top {
        final Base base;
        final OperationType ot;

        final List<InternalDependency> deps;

        Top(Base base, OperationType ot) {
            this.base = base;
            this.ot = ot;
            this.deps = InternalDependency.fromOperationType(ot);

        }

        MethodHandle mh = null;

        MethodHandle create(Object function) {
            MethodHandle mh = base.samType.methodHandle().bindTo(function);
            return mh.asType(ot.toMethodType());

            // Think we need to validate it

//            if (!expectedType.isInstance(value)) {
//                String type = Supplier.class.isAssignableFrom(supplierOrFunction.getClass()) ? "supplier" : "function";
//                if (value == null) {
//                    // NPE???
//                    throw new NullPointerException("The " + type + " '" + supplierOrFunction + "' must not return null");
//                } else {
//                    // throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " +
//                    // instance.getClass());
//                    throw new ClassCastException("The \" + type + \" '" + supplierOrFunction + "' was expected to return instances of type "
//                            + expectedType.getName() + " but returned a " + value.getClass().getName() + " instance");
//                }
//            }
        }
    }
}
