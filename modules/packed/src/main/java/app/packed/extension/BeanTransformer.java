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
package app.packed.extension;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import app.packed.util.Variable;

/**
 *
 */

// Hmm, tror vi maa have en OperationTransformer...
// Also with naming and stuff
public interface BeanTransformer {

    void addOperation(Op<?> op);

    /** {@return the current bean class.} */
    Class<?> beanClass();

    /** {@return the current bean kind.} */
    BeanKind beanKind();

    /** {@return the current bean source kind.} */
    BeanSourceKind beanSourceKind();

    // add(OperationType.of(void.class, SomeService, e->{});
    void addFunction(OperationType type, Object function);

    // new TypeVarToken<@OnStarting>(){};
    // addFunction0(new SyntheticVariable<@OnStarting>(){}, e->{ sysout("We started) })
    void addFunction(Variable result, Supplier<?> supplier);

    void addFunction(Variable result, Variable param1, Supplier<?> supplier);

    void addFunction(Variable result, Variable param1, Function<?, ?> function);

    void addFunction(Variable result, Variable param1, Variable param2, BiFunction<?, ?, ?> function);

    // Null = ignore
    void computeAllFieldSignatures(BiFunction<? super Field, ? super Variable, ? extends Variable> function);

    void computeAllMethodSignatures(BiFunction<? super Method, ? super OperationType, ? extends OperationType> function);

    void computeFieldSignature(Field field, Function<? super Variable, ? extends Variable> function);

    void computeMethodSignature(Method field, Function<? super OperationType, ? extends OperationType> function);

    void lookup(Function<Class<?>, Lookup> lookupFunction);

    void lookup(Lookup lookup);

    void ignoreAllFields(Predicate<? super Field> method);

    void ignoreAllFieldsNamed(String name);

    void ignoreAllMethods(Predicate<? super Method> method);

    // top to buttom?
    // Also need member order
    void classScanOrder();

    // We need OperationTarget + OperationType here I think
    // Hmm, this is big and complicated...
    // Think we need
    void operationOrder(Comparator<? super OperationType> ot);
    // ignore fields
    // Use X lookup for super class
    // ignore super class

    // replace method

    void skipScan();

    // Are operations added before
    void setAddBeforeScan();

    // Will include the bean class and everyone of its super classes.
    // Object.class is never scanned.
    void skipScanForClass(Predicate<? super Class<?>> skipScanFor);
}
