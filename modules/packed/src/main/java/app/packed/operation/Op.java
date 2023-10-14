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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.util.Nullable;
import internal.app.packed.operation.PackedOp;

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
     * @return a new operation without the leading variable
     *
     * @throws ClassCastException
     *             if the argument does not match the type of the leading variable.
     * @throws IndexOutOfBoundsException
     *             if the operation does not have a leading variable
     * @throws NullPointerException
     *             if the specified argument is null and the leading variable does not represent a reference type
     */
    Op<R> bind(@Nullable Object argument);

    /**
     * Returns a new operation that will perform the specified action with the result before returning it.
     *
     * @param action
     *            the action that will be run with the result as an argument on each invocation
     * @return the new operation
     * @throws UnsupportedOperationException
     *             if this operation has void return type
     */
    Op<R> peek(Consumer<? super R> action);

    /** {@return the type of this operation.} */
    OperationType type();

    /**
     * Creates a new operation that will invoke the specified constructor.
     *
     * @param constructor
     *            the constructor that will be called when operation is invoked
     * @return the new operation
     */
    static <T> Op<T> ofConstructor(Lookup lookup, Constructor<T> constructor) {
        requireNonNull(constructor, "constructor is null");
        throw new UnsupportedOperationException();
    }

    // Tror vi maa droppe de her lookups, de fungere ikke rigtig
    static Op<?> ofField(Lookup lookup, Field field) {
        requireNonNull(field, "field is null");
        MethodHandle handle;
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                // vs MethodHandles.private???
                lookup = lookup.in(field.getDeclaringClass());
            }
            handle = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            // I think we are going to throw another exception here
            throw new InaccessibleBeanMemberException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        System.out.println(handle);
        throw new UnsupportedOperationException();
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
    static Op<?> ofMethod(Lookup lookup, Method method) {
        requireNonNull(method, "method is null");

        throw new UnsupportedOperationException();
    }

    static Op<?> ofMethodHandle(MethodHandle methodHandle) {
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
    // Kunne have en lazy version ogsaa
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

    // Hmm, I think we are now a synthetic operation?
    // I don't know how else to map it
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
        throw new UnsupportedOperationException();
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
//    default <T> Op<T> mapTo(TypeToken<T> type, Function<? super R, ? extends T> mapper) {
//        // MappingFactoryHandle<T, R> f = new MappingFactoryHandle<>(type, factory.handle, mapper);
//        // return new Factory<>(new FactorySupport<>(f, factory.dependencies));
//        throw new UnsupportedOperationException();
//    }

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
