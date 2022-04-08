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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.base.Variable;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.inject.InternalFactory;
import packed.internal.inject.InternalFactory.BoundFactory;
import packed.internal.inject.InternalFactory.ConstantFactory;
import packed.internal.inject.InternalFactory.LookedUpFactory;
import packed.internal.inject.InternalFactory.PeekableFactory;
import packed.internal.inject.ReflectiveFactory;
import packed.internal.inject.ReflectiveFactory.ExecutableFactory;

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
 */
@SuppressWarnings("rawtypes")
// Rename to Func I think...
// Make into sealed interface???? Why not will make it easier to use records

// toMethodHandle???
// Altsaa hvis vi har et Factory kan vi jo altid bare registrere den et eller andet sted i Packed
// og saa kalde Factory igennem den...
// Saa det der med at det kun er Packed der kan invokere den er vel lidt ligegyldigt....

public abstract sealed class Factory<R> permits CapturingFactory, InternalFactory {

    /**
     * Binds the specified argument(s) to a variable with the specified index as returned by {@link #variables()}. This
     * method is typically used to bind arguments to parameters on a method or constructors when key-based binding is not
     * sufficient. A typical example is a constructor with two parameters of the same type.
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
    // bindRaw??? (The @Nullable additionArguments does not really work... as @Nullable is applied to the actual array)
    public final Factory<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
        requireNonNull(additionalArguments, "additionalArguments is null");
        InternalFactory<R> f = InternalFactory.canonicalize(this);
        List<InternalDependency> dependencies = f.dependencies();
        Objects.checkIndex(position, dependencies.size());
        int len = 1 + additionalArguments.length;
        int newLen = dependencies.size() - len;
        if (newLen < 0) {
            throw new IllegalArgumentException(
                    "Cannot specify more than " + (len - position) + " arguments for position = " + position + ", but arguments array was size " + len);
        }

        // Removing dependencies that are being replaced
        InternalDependency[] dd = new InternalDependency[newLen];
        for (int i = 0; i < position; i++) {
            dd[i] = dependencies.get(i);
        }
        for (int i = position; i < dd.length; i++) {
            dd[i] = dependencies.get(i + len);
        }

        // Populate final argument array
        Object[] args = new Object[len];
        args[0] = argument;
        for (int i = 0; i < additionalArguments.length; i++) {
            args[i + 1] = additionalArguments[i];
        }

        // TODO check types...

        return new BoundFactory<>(f, position, dd, args);
    }

    /**
     * Binds the first variable to the specified argument.
     * <p>
     * 
     * @param argument
     *            the argument to bind.
     * @return a new factory
     */
    // bindConstant like sidecar???
    // bindRaw
    public final Factory<R> bind(@Nullable Object argument) {
        return bind(0, argument);
    }

    final Factory<R> bindSupplier(int index, Supplier<?> supplier) {
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
    public final Factory<R> peek(Consumer<? super R> action) {
        return new PeekableFactory<>(InternalFactory.canonicalize(this), action);
    }

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
    public final int variableCount() {
        throw new UnsupportedOperationException();
    }

    /** {@return The variables this factory takes.} */
    public final List<Variable> variables() {
        throw new UnsupportedOperationException();
    }

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
    public final Factory<R> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        if (this instanceof ReflectiveFactory<R> f) {
            return new LookedUpFactory<>(f, f.toMethodHandle(lookup));
        }
        throw new UnsupportedOperationException(
                "This method is only supported by factories created from a field, constructor or method. And must be applied as the first operation after creating the factory");
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
    public static <T> Factory<T> ofConstant(T instance) {
        requireNonNull(instance, "instance is null");
        return new ConstantFactory<T>(instance);
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

    // Hvad goer vi med en klasse der er mere restri
    public static <T> Factory<T> ofMethod(Class<?> implementation, String name, Class<T> returnType, Class<?>... parameters) {
        requireNonNull(returnType, "returnType is null");
        return ofMethod(implementation, name, TypeToken.of(returnType), parameters);
    }

    // Annotations will be retained from the method
    public static <T> Factory<T> ofMethod(Class<?> implementation, String name, TypeToken<T> returnType, Class<?>... parameters) {
        throw new UnsupportedOperationException();
    }

    // If the specified instance is not a static method. An extra variable
    // use bind(Foo) to bind the variable.
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

    // new Factory<String>(SomeMethod);
    // How we skal have
    // Maaske kan vi

    // If the specified method is an instance method
    // variables will include a dependenc for it as the first
    // parameters

    public static <T> Factory<T> ofMethodHandle(MethodHandle methodHandle) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances. Compared to the simpler
     * {@link #ofConstructor(Constructor)} method this method takes a type literal that can be used to create factories with
     * a generic signature:
     *
     *
     * 
     * @param constructor
     *            the constructor used from creating an instance
     * @param type
     *            a type literal
     * @return the new factory
     * @see #ofConstructor(Constructor)
     */

    public static <T> Factory<T> ofStaticFactory(Class<?> clazz, TypeToken<T> returnType) {
        throw new UnsupportedOperationException();
    }

}
//TODO Qualifiers on Methods, Types together with findInjectable????
//Yes need to pick those up!!!!
//probably rename defaultKey to key.

//Split-module class hierachies, must

//Factories only
//
//Is it the responsibility of the factory or the injector to inject fields and methods???
//+ Factory
//
//+ Injector
//Then we can disable it on a case to case basis
//You can actually use factories without injection
//-------------------------
//ServiceDescriptor
//Refereres fra InjectorDescriptor....
//Skal bruges til Filtrering... Men hvis noeglerne er skjult kan vi vel bruge service....

//Does this belong in app.packed.service????
//No because components also uses it...

//This class used to provide some bind methods...
//But we don't do that no more. Because it was just impossible to
//see what was what...
////////TYPES (Raw)
//ExactType... -> Instance, Constructor
//LowerBoundType, Field, Method
//PromisedType -> Fac0,Fac1,Fac2,

/// TypeLiteral<- Always the promised, key must be assignable via raw type
///////////////

//TypeLiteral
//actual type

//Correctness
//Instance -> Lowerbound correct, upper correct
//Executable -> Lower bound maybe correct (if exposedType=return type), upper correct if final return type
//Rest, unknown all
//Bindable -> has no effect..

//static {
//Dependency.of(String.class);// Initializes InternalApis for InternalFactory
//}

//Ideen er her. at for f.eks. Factory.of(XImpl, X) saa skal der stadig scannes paa Ximpl og ikke paa X

///**
// * Returns the injectable type of this factory. This is the type that will be used for scanning for scanning for
// * annotations. This might differ from the actual type, for example, if {@link #mapTo(Class, Function)} is used
// *
// * @return stuff
// */
//// We should make this public...
//// InjectableType
//Class<? super T> scannableType() {
//    return rawType();
//}

///** {@inheritDoc} */
//@Override
//public final <S> Factory<T> bind(Class<S> key, @Nullable S instance) {
//
//  // Do we allow binding non-matching keys???
//  // Could be useful from Prime annotations...
//
//  // Tror vi skal have to forskellige
//
//  // bindParameter(int index).... retains index....
//  // Throws
//
//  // bindWithKey();
//
//  // bindRaw(); <---- Only takes a class, ignores nullable.....
//
//  // Hvordan klarer vi Foo(String firstName, String lastName)...
//  // Eller
//
//  // Hvordan klarer vi Foo(String firstName, SomeComposite sc)...
//
//  // Det eneste der er forskel er parameter index'et...
//  // Maaske bliver man bare noedt til at lave en statisk metoder....
//
//  // Skal vi have en speciel MemberFactory?????
//
//  //
//
//  // bindTo? Det er jo ikke et argument hvis det f.eks. er et field...
//
//  // resolveDependency()...
//  // Its not really an argument its a dependency that we resolve...
//
//  // withArgumentSupplier
//  throw new UnsupportedOperationException();
//}

///** {@inheritDoc} */
//// Required/Optional - Key - Variable?
//// Requirement
//

// Problemet med at fjerne ting fra #variables() er at saa bliver index'et lige pludselig aendret.
// F.eks. for dooo(String x, String y)
// Og det gider vi ikke....
// Saa variables stay the same -> Why shouldn't we able to bind them...

// Maaske er index ligegyldigt...
// Og det er bare en speciel mode for MethodSidecar
// Hvor man kan sige jeg tager denne variable ud af ligningen...

// Maybe add isVariableBound(int index)

// Rebinding? Ja hvorfor ikke... maaske have en #unbindable()

// Har vi en optional MemberDescriptor?????

// Hvis man nu vil injecte en composite....
