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
package app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.extension.BeanElement;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import app.packed.util.AnnotationList;
import app.packed.util.Variable;


// Hmm maybe this class simply transforms how the class is viewed

// Needs a mirror:D


// Maybe we just allow ignore/hide for now

/**
 * Only works on non {@link BeanSourceKind#SOURCELESS} beans.
 */
// Annotation must be visible to Faetter guf if adding or modifying
// Annotations (Remove, Transform, Add) for Class, Method, Fields, Constructor, Parameter

// Skip Member(s)

// Subclases : Skip Scan, Special Lookup for subclass
// Lookup
// Class Hierarchy

// Force Bind Parameter, (Field???)

// AOP?
// Member modification

// OperationTransformer

// All about operations I guess

// Hmm, tror vi maa have en OperationTransformer...
// Also with naming and stuff

// Targets -> BeanClass, BaseExtensionde


// Thought about having a .scan() method to allow pre and post.
// But doesnt really work with multiple transformers


// Den har vel rigtig meget at goere med interceptors a.la. operations...

// Er vi declarativ. Fx hvis vi nu gerne vil intercepte alle operationer.
// Men saa nogen lige pludselig tilfoerjer en operation senere hen

// Ved ikke hvor meget den giver mening foerend Leyden

// How do we handle super classes???

// extends SyntheticBean???
public interface BeanClassMutator {

    AnnotationList annotations();

    // add(OperationType.of(void.class, SomeService, e->{});
    void addFunction(OperationType type, Object function);

    // new TypeVarToken<@OnStarting>(){};
    // addFunction0(new SyntheticVariable<@OnStarting>(){}, e->{ sysout("We started) })
    void addFunction(Variable result, Supplier<?> supplier);

    void addFunction(Variable result, Variable param1, Function<?, ?> function);

    void addFunction(Variable result, Variable param1, Supplier<?> supplier);

    void addFunction(Variable result, Variable param1, Variable param2, BiFunction<?, ?, ?> function);

    void addOperation(Op<?> op);

    // usefull for processes and default bean transformers
    Class<Class<? super Assembly>> assemblyClass();

    /** {@return the current bean class.} */
    Class<?> beanClass();

    /** {@return the bean kind.} */
    BeanKind beanKind();

    /** {@return the bean source kind.} */
    BeanSourceKind beanSourceKind();

    // top to buttom?
    // Also need member order
    void classScanOrder();

    // Null = ignore
    void computeAllFieldSignatures(BiFunction<? super Field, ? super Variable, ? extends Variable> function);

    void computeAllMethodSignatures(BiFunction<? super Method, ? super OperationType, ? extends OperationType> function);

    void computeFieldSignature(Field field, Function<? super Variable, ? extends Variable> function);

    void computeMethodSignature(Method method, Function<? super OperationType, ? extends OperationType> function);

    // maybe hide instead of ignore
    void hideAllFields(Predicate<? super Field> method);

    void hideAllConstructors(Predicate<? super Method> method);

    void hideAllMethods(Predicate<? super Method> method);

    // on Bean class or members

    /**
     * <p>
     * If no targets are specified. Annotations will be ignored for all targets
     *
     * @param annotationType
     * @param targets
     */
    void hideAnnotations(Class<? extends Annotation> annotationType, ElementType... targets);

    void lookup(Function<Class<?>, Lookup> lookupFunction);

    void lookup(Lookup lookup);

    // We need OperationTarget + OperationType here I think
    // Hmm, this is big and complicated...
    // Think we need
    void operationOrder(Comparator<? super OperationType> ot);
    // ignore fields
    // Use X lookup for super class
    // ignore super class

    // replace method

    // Are operations added before
    void setAddBeforeScan();

    // Hmm, skipMethod + skipFields???
    void skipScan(@SuppressWarnings("unchecked") Class<? extends BeanElement>... elements);

    // Will include the bean class and everyone of its super classes.
    // Object.class is never scanned.
    void skipScanForClass(Predicate<? super Class<?>> skipScanFor);

    // I don't think we allow for unregistering it


    // Force transformer of the bean class always
    // Do we support interfaces??? For example, with default methods?
    // I don't think so
    // It is actually the same with super classes of a bean...

    // Can be used to augment extension beans that are open to you
    static void forceTransform(Lookup lookup, Class<?> beanClass, Consumer<? super BeanClassMutator> transformation) {
        throw new UnsupportedOperationException();
    }

    // What about subclassing?
    static void forceTransform(Lookup lookup, Consumer<? super BeanClassMutator> transformation) {
        forceTransform(lookup, lookup.lookupClass(), transformation);
    }

    // hmm IDK about this
    static void forceTransformSubclassesOf(Lookup lookup, Consumer<? super BeanClassMutator> transformation) {
        forceTransform(lookup, lookup.lookupClass(), transformation);
    }

}
