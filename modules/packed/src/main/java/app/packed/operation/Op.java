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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.bean.Inject;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.operation.op.PackedOp.ConstantOp;
import internal.app.packed.operation.op.ReflectiveOp.ExecutableOp;

/**
 * An object that creates other objects. Factories are always immutable and any method that returnsfactory is an
 * immutable that creates this
 * 
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class does not expose any methods that actually create new objects, this is all hidden in the internals of
 * Packed. This might change in the future, but for now users can only create factories, and not consume their output.
 * <p>
 * A {@link Op} type that uses a {@link Supplier} to provide instances.
 * <p>
 * This class is typically used like this:
 * 
 * <pre> {@code Op<Long> f = new Op0<>(System::currentTimeMillis) {};}</pre>
 * <p>
 * In this example we create a new class that extends Factory0 is order to capture information about the suppliers type
 * variable (in this case {@code Long}). Thereby circumventing the limitations of Java's type system for retaining type
 * information at runtime.
 * <p>
 * Qualifier annotations can be used if they have {@link ElementType#TYPE_USE} in their {@link Target}:
 * 
 * <pre> {@code Op<Long> f = new Op0<@SomeQualifier Long>(() -> 1L) {};}</pre>
 * 
 * @apiNote Op implementations does not generally implement {@link #hashCode()} or {@link #equals(Object)}.
 */

// toMethodHandle??? Ja hvorfor ikke... hvis man har et Factory, kan man jo altid bare registrere det og bruge det...
// Skal vi tage et Lookup object??? IDK

@SuppressWarnings("rawtypes") // eclipse being difficult
public sealed interface Op<R> permits PackedOp, CapturingOp {

    /**
     * Binds the specified argument(s) to a variable with the specified index.
     * <p>
     * This method is typically used to bind arguments to parameters on a method or constructors. A typical example is a
     * constructor which takes two parameters of the same type.
     * 
     * @param position
     *            the index of the first variable to bind
     * @param argument
     *            the (nullable) argument to bind
     * @param additionalArguments
     *            any additional (nullable) arguments to bind
     * @return a new operation
     * @throws ClassCastException
     *             if any of the arguments does not match their corresponding variable type.
     * @throws IndexOutOfBoundsException
     *             if (@code position) is less than {@code 0} or greater than {@code N - 1 - L} where {@code N} is the
     *             number of variables and {@code L} is the length of the additional argument array.
     * @throws NullPointerException
     *             if any of specified arguments are null and the corresponding variable does not represent a reference type
     */
    Op<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments);

    /**
     * Binds the first variable of the operation to the specified argument.
     * 
     * @param argument
     *            the argument to bind.
     * @return a new operation
     * 
     * @throws ClassCastException
     *             if the argument does not match the leading variable type.
     * @throws IndexOutOfBoundsException
     *             if the operation does not have a leading variable
     * @throws NullPointerException
     *             if the specified argument is null and the leading variable does not represent a reference type
     */
    Op<R> bind(@Nullable Object argument);

    /**
     * Returns a new operation that will perform the specified action immediately after the invocation before returning
     * result to the runtime.
     * <p>
     * If the op has void return type {@link MethodHandles#zero(Class)} will be used to find a fitting value to provide to
     * the action.
     * 
     * @param action
     *            the consume that will be run with the result of each invocation
     * @return the new op
     */
    Op<R> peek(Consumer<? super R> action);

    /** {@return the type of this operation.} */
    OperationType type();

    /**
     * Creates a new op that can invoke the specified constructor.
     * 
     * @param constructor
     *            the constructor that will be called when operation is invoked
     * @return the new operation
     */
    public static <T> Op<T> ofConstructor(Constructor<T> constructor) {
        requireNonNull(constructor, "constructor is null");
        return new ExecutableOp<>(constructor);
    }

    public static <T> Op<T> ofConstructor(Constructor<T> constructor, Lookup lookup) {
        requireNonNull(constructor, "constructor is null");
        return new ExecutableOp<>(constructor);
    }

    /**
     * Returns a operation that returns the specified instance every time the operation is invoked.
     * 
     * @param <T>
     *            the type of value returned by the operation
     * @param instance
     *            the instance to return on every invocation
     * @return the new operation
     */
//    * Produces a method handle of the requested return type which returns the given
//    * constant value every time it is invoked.
    // Hedder MethodHandle.constant
    public static <T> Op<T> ofInstance(T instance) {
//        * <p>
//        * If the specified instance makes use of field or method injection the returned factory should not be used more than
//        * once. As these fields and members will be injected every time, possible concurrently, an instance is provided by the
//        * factory.
        requireNonNull(instance, "instance is null");
        // Really no need for a ConstantOp..
        // should just be able to cll ofMethodHAndle(OperationType.of(instance.getClass), Mh);
        // toString() is nice though... Maybe keep it
        return new ConstantOp<T>(instance);
    }

    /**
     * <p>
     * If the specified method is not a static method. The returned factory will have the method's declaring class as its
     * first variable. Use to bind an instance of the declaring class.
     * 
     * @param <T>
     *            the type of value returned by the method
     * @param method
     *            the method to wrap
     * @param returnType
     *            the type of value returned by the method
     * @return a factory that wraps the specified method
     */
    public static Op<?> ofMethod(Method method) {
        requireNonNull(method, "method is null");
        throw new UnsupportedOperationException();
    }

    public static Op<?> ofMethod(Method method, Lookup lookup) {
        requireNonNull(method, "method is null");
        throw new UnsupportedOperationException();
    }

    public static Op<?> ofMethodHandle(MethodHandle methodHandle) {
        throw new UnsupportedOperationException();
    }
    
    public static Op<?> ofMethodHandle(MethodHandle methodHandle, OperationType type) {
        throw new UnsupportedOperationException();
    }
}

interface ZandboxOp<R> {

    /**
     * 
     * <p>
     * At invocationtime
     * 
     * {@link NullPointerException} will be thrown if the supplier return null, but does not take a reference type
     * 
     * {@link ClassCastException} if we create something that does not match
     * 
     * @param position
     *            the index of the first variable to bind
     * @param supplier
     *            the supplier
     * @return a new operation
     * 
     * @throws IndexOutOfBoundsException
     *             if (@code position) is less than {@code 0} or greater than {@code N - 1 - L} where {@code N} is the
     *             number of variables and {@code L} is the length of the additional argument array.
     */
    default Op<R> bindSupplier(int position, Supplier<?> supplier, Supplier<?>... additionalSuppliers) {
        // IOBE -> now
        // NPE -> Later
        // CCE -> Later
        throw new UnsupportedOperationException();
    }

//  /** {@return The number of variables this factory takes.} */
//  public abstract int variableCount();

    // Will retain annotations
    // adaptReturn <--- will do something about annotations
    default <T> Op<T> castReturn(Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    default <T> Op<T> mapResult(Class<T> type, Function<? super R, ? extends T> mapper) {

        // Ideen er at kunne lave en transformation for alt...
        // Tilfoej denne metode, representeret ved denne klasse...

        // ComponentTransformer.of(Class).....
        // Produces a factory??? Ved ikke hvad vi ellers skulle lave....
        // FactoryN har ikke brug for det taenker jeg...

        // MetaClass
        // addAnnotationToParameter2OnMethodX()..
        // F.eks. for assisted inject...
        // c
        // mapAnnotations(javax.inject.Inject).to(app.packed.inject)

        // I thinkg

        // FactoryMapper...
        // FactoryMapper.of(dddd).removeMethodsStartingWithX().toFactory();

        return mapTo(TypeToken.of(type), mapper);
    }

    /**
     * Returns a new factory that maps every object this factory create using the specified mapper.
     * 
     * @param <R>
     *            the type of result to map to
     * @param type
     *            the type of the mapped value
     * @param mapper
     *            the mapper used to map the result
     * @return a new mapped factory
     */
    // How do we handle key??? Think we might need a version that also takes a key.
    default <T> Op<T> mapTo(TypeToken<T> type, Function<? super R, ? extends T> mapper) {
        // MappingFactoryHandle<T, R> f = new MappingFactoryHandle<>(type, factory.handle, mapper);
        // return new Factory<>(new FactorySupport<>(f, factory.dependencies));
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new factory that maps every object this factory create using the specified mapper.
     * 
     * @param <R>
     *            the type of result to map to
     * @param mapper
     *            the mapper used to map the result
     * @return a new mapped factory
     */
    // Men keys er vel ikke laengere compatible saa... f.eks. hvis vi har Factory<String> f
    // f.map(UUID.class, e->new UUID(e)); -> Factory<UUID> ff, ff.key=String.class();

    // Hvem skal vi scanne???? Den vi laver oprindelig?? Eller den vi har mappet til?
    // Haelder nok til den vi har mappet til?????
    // Kan vi finde en usecase???
    default <T> Op<T> mapTo0(Op1<? super R, ? extends R> mapper) {
        // Factory<String> f = null;
        // @SuppressWarnings({ "null", "unused" })
        // Create a factory by taking the output and mapping it...
        // Factory<Integer> fi = f.mapTo0(new Factory1<>(e -> e.length()) {});
        throw new UnsupportedOperationException();
    }

    // needsRealm???
    default boolean needsLookup() {

//      final boolean needsLookup() {
        // Needs Realm?

//          // Tror ikke rigtig den fungere...
//          // Det skal jo vaere relativt til en klasse...
//          // F.eks. hvis X en public klasse, med en public constructor.
//          // Og X er readable til A, men ikke B.
//          // Saa har A ikke brug for et Lookup Object, men B har.
//          // Ved ikke rigtig hvad denne skal bruges til....
//          // Maa betyde om man skal
//          return false;
//      }
        return false;
    }

    /**
     * Returns the (raw) type of values this factory provide. This is also the type that is used for annotation scanning,
     * for example, for finding fields annotated with {@link Inject}.
     *
     * @return the raw type of the type of objects this factory provide
     */
    @Deprecated
    default Class<?> rawReturnType() {
        throw new UnsupportedOperationException();
    }

    default Op<R> useExactType(Class<? extends R> type) {
        // TypeHint.. withExactType

        // scanAs() must be exact type. Show example with static method that returns a Foo, but should scan with FooImpl
        // Ideen er lidt tænkt at man kan specifiere det på static factory methods, der ikke giver den.
        // fulde info om implementation
        // @Inject
        // SomeService create();
        // istedet for
        // @Inject
        // SomeServiceImpl create();
        throw new UnsupportedOperationException();
    }
}

// Hvad goer vi med en klasse der er mere restri
// If the specified instance is not a static method. An extra variable
// use bind(Foo) to bind the variable.
//public static <T> Op<T> ofMethod(Class<?> implementation, String name, Class<T> returnType, Class<?>... parameters) {
//    requireNonNull(returnType, "returnType is null");
//    return ofMethod(implementation, name, TypeToken.of(returnType), parameters);
//}
//
//// Annotations will be retained from the method
//public static <T> Op<T> ofMethod(Class<?> implementation, String name, TypeToken<T> returnType, Class<?>... parameters) {
//    throw new UnsupportedOperationException();
//}

// TODO Hmm do we cast the return type to type????

//// ofConstructor().castReturn(Class<T>)
//public static <T> Op<T> ofConstructor(Constructor<?> constructor, Class<T> type) {
//    requireNonNull(type, "type is null");
//    return ofConstructor(constructor, TypeToken.of(type));
//}
//
//// * <pre>
////* Factory<List<String>> f = Factory.ofConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {
////* });
////* </pre>
//public static <T> Op<T> ofConstructor(Constructor<?> constructor, TypeToken<T> type) {
//    requireNonNull(constructor, "constructor is null");
//    // TODO we probably need to validate the type literal here
//    return new ExecutableFactory<>(type, constructor);
//}
