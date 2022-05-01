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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.base.Variable;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.factory.InternalFactory.ConstantFactory;
import packed.internal.inject.factory.InternalFactory.LookedUpFactory;
import packed.internal.inject.factory.ReflectiveFactory;
import packed.internal.inject.factory.ReflectiveFactory.ExecutableFactory;

/**
 * An object that creates other objects. Factories are always immutable and any method that returnsfactory is an
 * immutable that creates this
 * 
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class does not expose any methods that actually create new objects, this is all hidden in the internals of
 * Packed. This might change in the future, but for now users can only create factories, and not consume their output.
 * <p>
 * A {@link Factory} type that uses a {@link Supplier} to provide instances.
 * <p>
 * This class is typically used like this:
 * 
 * <pre> {@code Factory<Long> f = new Factory<>(System::currentTimeMillis) {};}</pre>
 * <p>
 * In this example we create a new class that extends Factory0 is order to capture information about the suppliers type
 * variable (in this case {@code Long}). Thereby circumventing the limitations of Java's type system for retaining type
 * information at runtime.
 * <p>
 * Qualifier annotations can be used if they have {@link ElementType#TYPE_USE} in their {@link Target}:
 * 
 * <pre> {@code Factory<Long> f = new Factory<@SomeQualifier Long>(() -> 1L) {};}</pre>
 * 
 * @apiNote Factory implementations does generally not implement {@link #hashCode()} or {@link #equals(Object)}.
 */
// Rename to Func I think...
// Make into sealed interface???? Why not will make it easier to use records

// toMethodHandle??? Ja hvorfor ikke... hvis man har et Factory, kan man jo altid bare registrere det og bruge det...
// Skal vi tage et Lookup object??? IDK

// Altsaa hvis vi har et Factory kan vi jo altid bare registrere den et eller andet sted i Packed
// og saa kalde Factory igennem den...
// Saa det der med at det kun er Packed der kan invokere den er vel lidt ligegyldigt....

// Kunne vi have CapturingFactory extends InternalFactory??? saa alt altid er InternalFactory
@SuppressWarnings("rawtypes")
public abstract sealed class Factory<R> permits CapturingFactory, InternalFactory {

    /**
     * Binds the specified argument(s) to a variable with the specified index as returned by {@link #variables()}. This
     * method is typically used to bind arguments to parameters on a method or constructors. A typical example is a
     * constructor with two parameters of the same type.
     * 
     * @param position
     *            the index of the variable to bind
     * @param argument
     *            the (nullable) argument to bind
     * @param additionalArguments
     *            any additional (nullable) arguments to bind
     * @return a new factory
     * @throws IndexOutOfBoundsException
     *             if the specified index does not represent a valid variable in {@link #variables()}
     * @throws ClassCastException
     *             if an argument does not match the corresponding variable type.
     * @throws IllegalArgumentException
     *             if (@code position) is less than {@code 0} or greater than {@code N - 1 - L} where {@code N} is the
     *             number of dependencies and {@code L} is the length of the additional argument array.
     * @throws NullPointerException
     *             if the specified argument is null and the variable does not represent a reference type
     */
    // Smid CCE istedet for NPE?
    public abstract Factory<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments);

    /**
     * Binds the first variable to the specified argument.
     * <p>
     * 
     * @param argument
     *            the argument to bind.
     * @return a new factory
     * 
     * @throws ClassCastException
     *             if the argument does not match the leading variable type.
     * @throws UnsupportedOperationException
     *             if the factory does not have a leading variable
     * @throws NullPointerException
     *             if the specified argument is null and the variable does not represent a reference type
     */
    public final Factory<R> bind(@Nullable Object argument) {
        return bind(0, argument);
    }

    final Factory<R> bindSupplier(int position, Supplier<?> supplier) {
        throw new UnsupportedOperationException();
    }

    final <T> Factory<T> mapTo(Class<T> key, Function<? super T, ? extends T> mapper) {

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

        return mapTo(TypeToken.of(key), mapper);
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
    final <T> Factory<T> mapTo(TypeToken<T> type, Function<? super T, ? extends T> mapper) {
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
    final <T> Factory<T> mapTo0(Factory1<? super R, ? extends R> mapper) {
        // Factory<String> f = null;
        // @SuppressWarnings({ "null", "unused" })
        // Create a factory by taking the output and mapping it...
        // Factory<Integer> fi = f.mapTo0(new Factory1<>(e -> e.length()) {});
        throw new UnsupportedOperationException();
    }

    // needsRealm???
    boolean needsLookup() {

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
     * Returns a new factory that will perform the specified action immediately after the factory has constructed an object.
     * And before the object is returned to the runtime.
     * 
     * @param action
     *            the action to run after the factory has returned an object
     * @return the new factory
     */
    public abstract Factory<R> peek(Consumer<? super R> action);

    /**
     * Returns the (raw) type of values this factory provide. This is also the type that is used for annotation scanning,
     * for example, for finding fields annotated with {@link Inject}.
     *
     * @return the raw type of the type of objects this factory provide
     * @see #typeLiteral()
     */
    public final Class<?> rawReturnType() {
        return typeLiteral().rawType();
    }

    /**
     * Returns the type of the type of objects this factory provide.
     *
     * @return the type of the type of objects this factory provide
     * @see #rawType()
     */
    public abstract TypeToken<R> typeLiteral();

    final Factory<R> useExactType(Class<? extends R> type) {
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

    /** {@return The number of variables this factory takes.} */
    public abstract int variableCount();

    /** {@return The variables this factory takes.} */
    public abstract List<Variable> variables();

    /**
     * If this factory was created from a member (field, constructor or method), this method returns a new factory that uses
     * the specified lookup object to access any underlying member whenever this framework needs to access.
     * <p>
     * This method is useful, for example, to make a factory publically available for an class that does not have a public
     * constructor.
     * <p>
     * The specified lookup object will always be preferred, even when, for example, being registered with a container who
     * has its own lookup object.
     * <p>
     * If you have split-module class hierarchies with an abstract class in one module a concrete class in another module.
     * 
     * Remember to register the support class via the standard service loading mechanism as outlined in ....
     * 
     * @param lookup
     *            the lookup object
     * @return a new factory with uses the specified lookup object when accessing the underlying member
     * @throws InaccessibleMemberException
     *             if the specified lookup object does not give access to the underlying member
     * @throws UnsupportedOperationException
     *             if this factory was not created from either a field, constructor or method.
     */
    // Goddamn, what about static create method on one object, and the actuak object in another module.
    // Her taenker jeg ogsaa paa at det lookup object bliver brugt til Hooks, o.s.v.
    // Igen der er kun et problem, hvis metoden
    // Maaske skal vi tillade stacked MethodHandles..
    // Maaske skal vi endda have en SelectiveMethodHandle
    //// Ideen er at man kan pakke en method handle ind...
    // Stacked lookups..
    // Vi skal have en hel section omkring method handlers.
    // Lookup object paa et factory. Kan bruges til alle metoder....Ikke kun dem med inject
    // Giver ikke mening andet...

    // open(Lookup)
    // openResult(Lookup) <---- maaske er den baa en
    // open()????
    // openTo(Lookup, xxx)
    public final Factory<R> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        if (this instanceof ReflectiveFactory<R> f) {
            return new LookedUpFactory<>(f, f.toMethodHandle(lookup));
        }
        throw new UnsupportedOperationException(
                "This method is only supported by factories created from a field, constructor or method. And must be applied as the first operation after creating the factory");
    }

    // ReflectionFactory.of
    public static <T> Factory<T> ofConstructor(Constructor<?> constructor, Class<T> type) {
        requireNonNull(type, "type is null");
        return ofConstructor(constructor, TypeToken.of(type));
    }

    // * <pre>
//  * Factory<List<String>> f = Factory.ofConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {
//  * });
//  * </pre>
    public static <T> Factory<T> ofConstructor(Constructor<?> constructor, TypeToken<T> type) {
        requireNonNull(constructor, "constructor is null");
        // TODO we probably need to validate the type literal here
        return new ExecutableFactory<>(type, constructor);
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances.
     *
     * @param constructor
     *            the constructor used for creating new instances
     * @return the new factory
     */
    public static <T> Factory<T> ofConstructor(Constructor<T> constructor) {
        requireNonNull(constructor, "constructor is null");
        TypeToken<T> tl = TypeToken.of(constructor.getDeclaringClass());
        return new ExecutableFactory<>(tl, constructor);
    }

    /**
     * Returns a factory that returns the specified instance every time the factory must provide a value.
     * <p>
     * If the specified instance makes use of field or method injection the returned factory should not be used more than
     * once. As these fields and members will be injected every time, possible concurrently, an instance is provided by the
     * factory.
     * 
     * @param <T>
     *            the type of value returned by the factory
     * @param instance
     *            the instance to return on every request
     * @return the factory
     */
    public static <T> Factory<T> ofInstance(T instance) {
        requireNonNull(instance, "instance is null");
        return new ConstantFactory<T>(instance);
    }

    // Hvad goer vi med en klasse der er mere restri
    // If the specified instance is not a static method. An extra variable
    // use bind(Foo) to bind the variable.
    public static <T> Factory<T> ofMethod(Class<?> implementation, String name, Class<T> returnType, Class<?>... parameters) {
        requireNonNull(returnType, "returnType is null");
        return ofMethod(implementation, name, TypeToken.of(returnType), parameters);
    }

    // Annotations will be retained from the method
    public static <T> Factory<T> ofMethod(Class<?> implementation, String name, TypeToken<T> returnType, Class<?>... parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * If the specified method is not a static method. The returned factory will have the method's declaring class as its
     * first variable. Use {@link #provideInstance(Object)} to bind an instance of the declaring class.
     * 
     * @param <T>
     *            the type of value returned by the method
     * @param method
     *            the method to wrap
     * @param returnType
     *            the type of value returned by the method
     * @return a factory that wraps the specified method
     * @see #ofMethod(Method, TypeToken)
     */
    public static <T> Factory<T> ofMethod(Method method, Class<T> returnType) {
        requireNonNull(returnType, "returnType is null");
        return ofMethod(method, TypeToken.of(returnType));
    }

    // Den her sletter evt. Qualifier paa metoden...
    public static <T> Factory<T> ofMethod(Method method, TypeToken<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");

        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> ofMethodHandle(MethodHandle methodHandle) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> ofMethodHandle(MethodHandle methodHandle, List<Variable> variables) {
        // Variables must match the method handle
        throw new UnsupportedOperationException();
    }
}
