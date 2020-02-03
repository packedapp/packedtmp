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
 * See the License from the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import app.packed.base.invoke.UncheckedIllegalAccessException;
import app.packed.base.reflect.ConstructorDescriptor;
import app.packed.base.reflect.MethodDescriptor;
import app.packed.base.reflect.VariableDescriptor;
import packed.internal.inject.factory.FactoryFindInjectableExecutable;
import packed.internal.inject.factory.FactorySupport;
import packed.internal.inject.factory.InstanceFactoryHandle;
import packed.internal.inject.factory.MappingFactoryHandle;
import packed.internal.moduleaccess.AppPackedInjectAccess;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.BaseSupport;

/**
 * An object that creates other objects. factory is an immutable that creates this
 * 
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class does not expose any methods that actually create new instances. This is all hidden in the internals of
 * Packed. This might change in the future, but for now users can only create factories, and not consume their output.
 */

// TODO Qualifiers on Methods, Types together with findInjectable????
// Yes need to pick those up!!!!
// probably rename defaultKey to key.

// Split-module class hierachies, must

// Factories only
//
// Is it the responsibility of the factory or the injector to inject fields and methods???
// + Factory
//
// + Injector
// Then we can disable it on a case to case basis
// You can actually use factories without injection
// -------------------------
// ServiceDescriptor
// Refereres fra InjectorDescriptor....
// Skal bruges til Filtrering... Men hvis noeglerne er skjult kan vi vel bruge service....

//Does this belong in app.packed.service????
//No because components also uses it...
public class Factory<T> {

    /** A cache of factories used by {@link #find(Class)}. */
    private static final ClassValue<Factory<?>> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            return new Factory(FactoryFindInjectableExecutable.find(implementation));
        }
    };

    /**
     * A cache of factories used by {@link #find(TypeLiteral)}. This cache is only used by subclasses of TypeLiteral, never
     * literals that are manually constructed.
     */
    private static final ClassValue<Factory<?>> TYPE_LITERAL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            Type t = TYPE_LITERAL_TV_EXTRACTOR.extract(implementation);
            return new Factory(FactoryFindInjectableExecutable.find(ModuleAccess.util().toTypeLiteral(t)));
        }
    };

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeLiteral.class);

    static {
        ModuleAccess.initialize(AppPackedInjectAccess.class, new AppPackedInjectAccess() {

            @Override
            public <T> FactorySupport<T> toSupport(Factory<T> factory) {
                return factory.factory;
            }
        });
    }

    /** The internal instance that all calls delegate to. */
    private final FactorySupport<T> factory;

    /**
     * Used by {@link Factory2#Factory2(BiFunction)} because we cannot refer to an instance method {@link Object#getClass()}
     * before calling this constructor.
     *
     * @param function
     *            the function used to create new instances
     */
    @SuppressWarnings("unchecked")
    Factory(BiFunction<?, ?, ? extends T> function) {
        this.factory = (FactorySupport<T>) Factory2.create(getClass(), function);
    }

    /**
     * Creates a new factory by wrapping an internal factory.
     *
     * @param factory
     *            the internal factory to wrap.
     */
    Factory(FactorySupport<T> factory) {
        this.factory = requireNonNull(factory, "factory is null");
    }

    /**
     * Used by {@link Factory1#Factory1(Function)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function used to create new instances
     */
    @SuppressWarnings("unchecked")
    Factory(Function<?, ? extends T> function) {
        this.factory = (FactorySupport<T>) Factory1.create(getClass(), function);
    }

    /**
     * Used by {@link Factory0#Factory0(Supplier)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param supplier
     *            the supplier used to create new instances
     */
    @SuppressWarnings("unchecked")
    Factory(Supplier<? extends T> supplier) {
        this.factory = (FactorySupport<T>) Factory0.create(getClass(), supplier);
    }

    /**
     * Returns a list of all of the dependencies that needs to be fulfilled in order for this factory to successfully create
     * an instance. Returns an empty list if this factory does not have any dependencies.
     * <p>
     * 
     * @apiNote The list does not include dependencies that may be needed to do field or instance method injection. As these
     *          are the responsibility of the injector in which they are registered.
     * 
     * @return a list of all of the dependencies of this factory
     */
    // Required/Optional - Key - Variable?
    // Requirement

    // FactoryDescriptor.of(Factory f) <--- in devtools???

    public final <S> Factory<T> bind(Class<S> key, @Nullable S instance) {

        // Do we allow binding non-matching keys???
        // Could be useful from Prime annotations...

        // Tror vi skal have to forskellige

        // bindParameter(int index).... retains index....
        // Throws

        // bindWithKey();

        // bindRaw(); <---- Only takes a class, ignores nullable.....

        // Hvordan klarer vi Foo(String firstName, String lastName)...
        // Eller

        // Hvordan klarer vi Foo(String firstName, SomeComposite sc)...

        // Det eneste der er forskel er parameter index'et...
        // Maaske bliver man bare noedt til at lave en statisk metoder....

        // Skal vi have en speciel MemberFactory?????

        //

        // bindTo? Det er jo ikke et argument hvis det f.eks. er et field...

        // resolveDependency()...
        // Its not really an argument its a dependency that we resolve...

        // withArgumentSupplier
        throw new UnsupportedOperationException();
    }

    public final <S> Factory<T> bind(Key<S> key, @Nullable S instance) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final Factory<T> bind(Object instance) {
        requireNonNull(instance, "instance is null");
        return bind((Class) instance.getClass(), instance);

        // someExtension()
        // install(Factory.of(Foo.class).withArgument(this))).

        // There is going to be some automatic support for injecting extensions into
        // services installed by them. We are just not quite there yet.
        // Will bind to any assignable parameter...
    }

    public final <S> Factory<T> bindSupplier(Class<S> key, Supplier<?> supplier) {
        // Altsaa vi kan vel bruge et andet factory????
        // En mulig usecase f.eks. for Factory1 er at kunne mappe dependencies...
        // f.eks. fra Foo(CardReader) -> new Factory0<
        // new Factory0<>(e->e);
        // withArgumentSupplier
        throw new UnsupportedOperationException();
    }

    public final <S> Factory<T> bindSupplier(Key<S> key, Supplier<?> supplier) {
        throw new UnsupportedOperationException();
    }

    public final Factory<T> bindVariable(int index, @Nullable Object argument) {

        // IndexOutOfBoundsException... if invalid index....

        // UnsupportedOperationException... if was not created from a member
        //

        // Har vi en optional MemberDescriptor?????

        // Hvis man nu vil injecte en composite....

        throw new UnsupportedOperationException();
    }

    public final Factory<T> bindVariableSupplier(int index, Supplier<?> supplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * The key under which If this factory is registered as a service. This method returns the (default) key that will be
     * used, for example, when regist Returns the (default) key to which this factory will bound to if using as If this
     * factory is used to register a service.
     *
     * @return the key under which this factory will be registered unless
     * @see #withKey(Key)
     */
    public final Key<T> key() {
        return factory.key;
    }

    /**
     * Returns a new factory that maps every object this factory create using the specified mapper.
     * 
     * @param <R>
     *            the type of result to map to
     * @param key
     *            the type of the mapped value
     * @param mapper
     *            the mapper used to map the result
     * @return a new mapped factory
     */
    // Men keys er vel ikke laengere compatible saa... f.eks. hvis vi har Factory<String> f
    // f.map(UUID.class, e->new UUID(e)); -> Factory<UUID> ff, ff.key=String.class();

    // Hvem skal vi scanne???? Den vi laver oprindelig?? Eller den vi har mappet til?
    // Haelder nok til den vi har mappet til?????
    public final <R> Factory<R> mapTo(Class<R> key, Function<? super T, ? extends R> mapper) {
        return mapTo(TypeLiteral.of(key), mapper);
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
    public final <R> Factory<R> mapTo(TypeLiteral<R> type, Function<? super T, ? extends R> mapper) {
        MappingFactoryHandle<T, R> f = new MappingFactoryHandle<>(type, factory.handle, mapper);
        return new Factory<>(new FactorySupport<>(f, factory.dependencies));
    }

    public final boolean needsLookup() {
        return false;
    }

    /**
     * Returns the raw type of the objects this factory creates.
     *
     * @return the raw type of the objects this factory creates
     */
    public final Class<? super T> rawType() {
        return typeLiteral().rawType();
    }

    /**
     * Returns the injectable type of this factory. This is the type that will be used for scanning for scanning for
     * annotations. This might differ from the actual type, for example, if {@link #mapTo(Class, Function)} is used
     *
     * @return stuff
     */
    // We should make this public...
    // InjectableType
    Class<? super T> scannableType() {
        return rawType();
    }

    /**
     * Returns the type of objects this factory creates.
     *
     * @return the type of objects this factory creates
     */
    public final TypeLiteral<T> typeLiteral() {
        // Passer ikke hvis vi bruger map()...
        return factory.handle.returnType();
    }

    public final Factory<T> useExactType(Class<? extends T> type) {
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

    /**
     * Returns an immutable list of any variables (typically fields or parameters) that was used to construct this factory.
     * <p>
     * If this factory was created using {@link #fromInstance(Object)} the returned list is empty.
     * 
     * @return any variables that was used to construct the factory
     */
    public final List<VariableDescriptor> variables() {
        // this list is static...

        // Returns empty list for type variables for now...
        throw new UnsupportedOperationException();

        // If we have a List<VariableDescriptor> unboundVariables()...
        // How would composite + primed be treated...
    }

    public List<?> dependencies() {
        // What if have Factory f = Factory.of(Foo(String x, String y));
        // f.bindVariable(0, "FooBar");
        // Now the first parameter (with Key String) is bound.
        // But not the second parameter (also with Key String)
        // What if we bind String now??? Only too second parameter?

        throw new UnsupportedOperationException();

        // Factory<T> narrow() <- removes bound dependencies/parameters()...
    }

    /**
     * Returns a new factory retaining all of the existing properties of this factory. Except that the key returned by
     * {@link #key()} will be changed to the specified key.
     * 
     * @param key
     *            the key under which to bind the factory
     * @return the new factory
     * @throws ClassCastException
     *             if the type of the key does not match the type of instances this factory provides
     * @see #key()
     */
    public final Factory<T> withKey(Key<?> key) {
        // Must be compatible with key in some way
        throw new UnsupportedOperationException();
    }

    /**
     * If this factory was created from a member (field, constructor or method), this method returns a new factory that uses
     * the specified lookup object to access any mem underlying member whenever this framework needs to access.
     * <p>
     * This method is useful, for example, to make a factory publically available for an class that does not have a public
     * constructor.
     * <p>
     * The specified lookup object will always be preferred, even when, for example, being registered with a bundle who has
     * its own lookup object.
     * <p>
     * If you have split-module class hierarchies with an abstract class in one module a concrete class in another module.
     * You can use a {@link BaseSupport} class to register a method handle with the abstract class.
     * 
     * Remember to register the support class via the standard service loading mechanism as outlined in ....
     * 
     * @param lookup
     *            the lookup object
     * @return a new factory with uses the specified lookup object when accessing the underlying member
     * @throws UncheckedIllegalAccessException
     *             if the specified lookup object does not give access to the underlying member
     * @throws UnsupportedOperationException
     *             if this factory was not created from either a field, constructor or method.
     */
    // Goddamn, what about static create method on one object, and the actuak object in another module.
    // Her taenker jeg ogsaa paa at det lookup object bliver brugt til Hooks, o.s.v.
    // Maaske skal vi tillade stacked MethodHandles..
    // Maaske skal vi endda have en SelectiveMethodHandle
    //// Ideen er at man kan pakke en method handle ind...
    // Stacked lookups..
    // Vi skal have en hel section omkring method handlers.

    public final Factory<T> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        return new Factory<>(new FactorySupport<T>(factory.handle.withLookup(lookup), factory.dependencies));
    }

    /**
     * Tries to find a single injectable static method or constructor on the specified class using the following rules:
     * <p>
     * <ul>
     * <li>If a single static method (non-static methods are ignored) annotated with {@link Inject} is present a factory
     * wrapping the method will be returned. If there are multiple static methods annotated with Inject this method will
     * fail with {@link IllegalStateException}.</li>
     * <li>If a single constructor annotated with {@link Inject} is present a factory wrapping the constructor will be
     * returned. If there are multiple constructors annotated with Inject this method will fail with
     * {@link IllegalStateException}.</li>
     * <li>If there is exactly one public constructor, a factory wrapping the constructor will be returned. If there are
     * multiple public constructors this method will fail with {@link IllegalStateException}.</li>
     * <li>If there is exactly one protected constructor, a factory wrapping the constructor will be returned. If there are
     * multiple protected constructors this method will fail with {@link IllegalStateException}.</li>
     * <li>If there is exactly one package-private constructor, a factory wrapping the constructor will be returned. If
     * there are multiple package-private constructors this method will fail with {@link IllegalStateException}.</li>
     * <li>If there is exactly one private constructor, a factory wrapping the constructor will be returned. Otherwise an
     * {@link IllegalStateException} is thrown.</li>
     * </ul>
     * <p>
     * 
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified type
     */
    @SuppressWarnings("unchecked")
    // Todo rename to make (or just of....) Nej, syntes maaske den er find med find()...
    public static <T> Factory<T> find(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return (Factory<T>) CLASS_CACHE.get(implementation);
    }

    /**
     * This method is equivalent to {@link #find(Class)} except taking a type literal.
     *
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        if (!ModuleAccess.util().isCanonicalized(implementation)) {
            // We cache factories for all "new TypeLiteral<>(){}"
            return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.type();
        if (t instanceof Class) {
            return (Factory<T>) find((Class<?>) t);
        } else {
            return new Factory<>(FactoryFindInjectableExecutable.find(implementation));
        }
    }

    /**
     * Returns a factory that returns the specified instance every time the factory is used.
     * <p>
     * If the specified instance makes use of field or method injection the returned factory should not be used more than
     * once. As these fields and members will be injected every time, possible concurrently, an instance is provided by the
     * factory.
     * 
     * @param <T>
     *            the type of instances created by the factory
     * @param instance
     *            the instance to return every time a new instance is requested
     * @return the factory
     */
    public static <T> Factory<T> fromInstance(T instance) {
        return new Factory<>(new FactorySupport<>(InstanceFactoryHandle.of(instance), List.of()));
    }
}

//Ideen er vi dropper disse. Fordi all metoder virker...
//Men instanse metoder skal man binde...
//Factory.findMethod(Doo.class, "dooo").bind(new Doo());
class XDeprecatedMethodInstances {

    static <T> Factory<T> findInstanceMethod(Object onInstance, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    static <T> Factory<T> findInstanceMethod(Object onInstance, Class<T> returnType, String name) {
        throw new UnsupportedOperationException();
    }

    // Man kunne ogsaa bare sige man tog instance metoder...
    // Man saa skal man binde receiveren.....
    //// Dvs for instans metoder, saa bliver selve instancen en dependency...
    public static <T> Factory<T> fromMethodInstance(Object onInstance, Method method, Class<T> returnType) {
        requireNonNull(returnType, "returnType is null");
        return fromMethodInstance(onInstance, method, TypeLiteral.of(returnType));
    }

    public static <T> Factory<T> fromMethodInstance(Object onInstance, Method method, TypeLiteral<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");
        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }

    // How we skal have
    public static <T> Factory<T> fromMethodInstance(Object onInstance, TypeLiteral<T> returnType, String name, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }
}

//static <T> Factory<T> fromMethodHandle(MethodHandle mh) {
//  // We don't support this because annotations and generic information are stripped from MethodHandles.
//  throw new UnsupportedOperationException();
//}
//Virker kun med noget der
//final MethodHandle toMethodHandle() {
////How does this method handle prime annotations????
////It does not, so maybe just jinx it...
//throw new UnsupportedOperationException();
//}

//
///**
//* Returns a new bindable factory.
//*
//* @return a new bindable factory
//*/
//public BindableFactory<T> bindable() {
//// bindable, newBindable, toBindable()
//return new BindableFactory<>(factory);
//}

//Finde metoder paa statisk vs instance....
//Hvis vi kun tillader @Inject paa de statiske, bliver algoritmerne lidt for forskellige..

class XFac2 {

    // either one annotated with inject, or just one method, for example lambda..

    /**
     * This method will attempt to find exactly one static method annotated with inject. Either because there is only one
     * method that returns returnType or because there is one annotated with {@link Inject}
     *
     * @param implementation
     *            the implementation
     * @param returnType
     *            the
     * @return a factory wrapping the st
     * @throws IllegalArgumentException
     *             if there was not exactly one static method annotated with {@link Inject} or if the return type of the
     *             method did not match the specified return type
     */
    public static <T> Factory<T> findMethod(Class<?> implementation, Class<T> returnType) {
        return findMethod(implementation, TypeLiteral.of(returnType));
    }

    public static <T> Factory<T> findMethod(Class<?> implementation, TypeLiteral<T> returnType) {
        // I think scan from exactly one 1 inject method
        throw new UnsupportedOperationException();
    }

    // Will look for a single static method annotated with Inject...
    public static <T> Factory<T> findMethod(Class<T> type) {
        return findMethod(requireNonNull(type), TypeLiteral.of(type));
    }

    // Ideen er lidt at der kun maa vaere en metode med det navn...
    // Ellers fejler vi
    public static <T> Factory<T> findNamed(Class<T> key, String name) {
        return findMethod(requireNonNull(key), TypeLiteral.of(key));
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances.
     *
     * @param constructor
     *            the constructor used for creating new instances
     * @return the new factory
     */
    public static <T> Factory<T> fromConstructor(Constructor<? extends T> constructor) {
        throw new UnsupportedOperationException();
        // MirrorOfClass<T> mirror = (MirrorOfClass<T>) MirrorOfClass.fromImplementation(requireNonNull(constructor,
        // "constructor is null").getDeclaringClass());
        // return InternalExecutableFactory.from(mirror, mirror.constructors().match(constructor.getParameterTypes()));
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances. Compared to the simpler
     * {@link #fromConstructor(Constructor)} method this method takes a type literal that can be used to create factories
     * with a generic signature:
     *
     * <pre>
     * Factory<List<String>> f = Factory.fromConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {
     * });
     * </pre>
     *
     * @param constructor
     *            the constructor used from creating an instance
     * @param type
     *            a type literal
     * @return the new factory
     * @see #fromConstructor(Constructor)
     */
    public static <T> Factory<T> fromConstructor(Constructor<? extends T> constructor, TypeLiteral<? extends T> type) {
        return fromConstructor(constructor);
    }

    public static <T> Factory<T> fromConstructor(Constructor<?> constructor, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> fromConstructor(ConstructorDescriptor<T> constructor) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a factory by looking at the specified type for a constructor with the specified parameter types. For example,
     * the following example will return a factory that uses {@link String#String(StringBuilder)} to create new instances:
     *
     * <pre>
     * Factory<String> factory = fromConstructor(String.class, StringBuilder.class);
     * </pre>
     *
     * @param constructor
     *            the constructor
     * @param key
     *            the keft to register under
     * @return the new factory
     * @throws IllegalArgumentException
     *             if a constructor with the specified parameter types does not exist on the specified type
     */
    public static <T> Factory<T> fromConstructor(ConstructorDescriptor<T> constructor, Key<T> key) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> fromMethod(Class<?> implementation, Class<T> returnType, String name, Class<?>... parameters) {
        return fromMethod(implementation, TypeLiteral.of(requireNonNull(returnType, "returnType is null")), name, parameters);
    }

    // How we skal have
    public static <T> Factory<T> fromMethod(Class<?> implementation, TypeLiteral<T> returnType, String name, Class<?>... parameters) {
        throw new UnsupportedOperationException();
    }

    // new Factory<String>(SomeMethod);
    // How we skal have
    public static <T> Factory<T> fromMethod(Method method, Class<T> returnType) {
        return fromMethod(method, TypeLiteral.of(requireNonNull(returnType, "returnType is null")));
    }

    // Den her sletter evt. Qualifier paa metoden...
    public static <T> Factory<T> fromMethod(Method method, TypeLiteral<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");
        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> fromMethod(MethodDescriptor method, Class<T> returnType) {
        // Syntes vi skal omnavngive den til
        // fromMethod + fromMethodInstance
        throw new UnsupportedOperationException();
    }

}