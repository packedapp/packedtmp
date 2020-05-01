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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import app.packed.base.reflect.VariableDescriptor;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.Factory1;
import app.packed.inject.Factory2;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 *
 */
//FunctionFactory????
public class BaseFactory<T> implements Factory<T> {

    /** A cache of factories used by {@link #find(Class)}. */
    private static final ClassValue<Factory<?>> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            return new BaseFactory(FactoryFindInjectableExecutable.find(implementation));
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
            return new BaseFactory(FactoryFindInjectableExecutable.find(ModuleAccess.base().toTypeLiteral(t)));
        }
    };

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeLiteral.class);

    /** The internal instance that all calls delegate to. */
    public final FactorySupport<T> factory;

    /**
     * Used by {@link Factory2#Factory2(BiFunction)} because we cannot refer to an instance method {@link Object#getClass()}
     * before calling this constructor.
     *
     * @param function
     *            the function used to create new instances
     */
    public BaseFactory(BiFunction<?, ?, ? extends T> function) {
        this.factory = Factory2FactoryHandle.create(getClass(), function);
    }

    /**
     * Creates a new factory by wrapping an internal factory.
     *
     * @param factory
     *            the internal factory to wrap.
     */
    BaseFactory(FactorySupport<T> factory) {
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
    public BaseFactory(Function<?, ? extends T> function) {
        this.factory = (FactorySupport<T>) Factory1FactoryHandle.create(getClass(), function);
    }

    /**
     * Used by {@link Factory0#Factory0(Supplier)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param supplier
     *            the supplier used to create new instances
     */
    @SuppressWarnings("unchecked")
    public BaseFactory(Supplier<? extends T> supplier) {
        this.factory = (FactorySupport<T>) Factory0FactoryHandle.create(getClass(), supplier);
    }

//    /** {@inheritDoc} */
//    @Override
//    public final <S> Factory<T> bind(Class<S> key, @Nullable S instance) {
//
//        // Do we allow binding non-matching keys???
//        // Could be useful from Prime annotations...
//
//        // Tror vi skal have to forskellige
//
//        // bindParameter(int index).... retains index....
//        // Throws
//
//        // bindWithKey();
//
//        // bindRaw(); <---- Only takes a class, ignores nullable.....
//
//        // Hvordan klarer vi Foo(String firstName, String lastName)...
//        // Eller
//
//        // Hvordan klarer vi Foo(String firstName, SomeComposite sc)...
//
//        // Det eneste der er forskel er parameter index'et...
//        // Maaske bliver man bare noedt til at lave en statisk metoder....
//
//        // Skal vi have en speciel MemberFactory?????
//
//        //
//
//        // bindTo? Det er jo ikke et argument hvis det f.eks. er et field...
//
//        // resolveDependency()...
//        // Its not really an argument its a dependency that we resolve...
//
//        // withArgumentSupplier
//        throw new UnsupportedOperationException();
//    }

//    /** {@inheritDoc} */
//    // Required/Optional - Key - Variable?
//    // Requirement
//
//    // FactoryDescriptor.of(Factory f) <--- in devtools???
//
//    @Override
//    public final <S> Factory<T> bind(Key<S> key, @Nullable S instance) {
//        throw new UnsupportedOperationException();
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    public final Factory<T> bind(Object instance) {
//        requireNonNull(instance, "instance is null");
//        return bind((Class) instance.getClass(), instance);
//
//        // someExtension()
//        // install(Factory.of(Foo.class).withArgument(this))).
//
//        // There is going to be some automatic support for injecting extensions into
//        // services installed by them. We are just not quite there yet.
//        // Will bind to any assignable parameter...
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public final <S> Factory<T> bindSupplier(Class<S> key, Supplier<?> supplier) {
//        // Altsaa vi kan vel bruge et andet factory????
//        // En mulig usecase f.eks. for Factory1 er at kunne mappe dependencies...
//        // f.eks. fra Foo(CardReader) -> new Factory0<
//        // new Factory0<>(e->e);
//        // withArgumentSupplier
//        throw new UnsupportedOperationException();
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public final <S> Factory<T> bindSupplier(Key<S> key, Supplier<?> supplier) {
//        throw new UnsupportedOperationException();
//    }

    public final List<?> dependencies() {
        // What if have Factory f = Factory.of(Foo(String x, String y));
        // f.bindVariable(0, "FooBar");
        // Now the first parameter (with Key String) is bound.
        // But not the second parameter (also with Key String)
        // What if we bind String now??? Only too second parameter?

        throw new UnsupportedOperationException();

        // Factory<T> narrow() <- removes bound dependencies/parameters()...
    }

    /** {@inheritDoc} */
    @Override
    public final <K> Factory<T> inject(Class<K> key, BiConsumer<? super T, ? super K> action) {
        return inject(Key.of(key), action);
    }

    /** {@inheritDoc} */
    @Override
    public final Factory<T> inject(Consumer<? super T> action) {
        // Bare u
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final <K> Factory<T> inject(Key<K> key, BiConsumer<? super T, ? super K> action) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return factory.key;
    }

    final <R> Factory<R> mapTo(Class<R> key, Function<? super T, ? extends R> mapper) {

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
    final <R> Factory<R> mapTo(TypeLiteral<R> type, Function<? super T, ? extends R> mapper) {
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

    final <R> Factory<R> mapTo0(Factory1<? super T, ? extends R> mapper) {
        // Factory<String> f = null;
        // @SuppressWarnings({ "null", "unused" })
        // Create a factory by taking the output and mapping it...
        // Factory<Integer> fi = f.mapTo0(new Factory1<>(e -> e.length()) {});
        throw new UnsupportedOperationException();
    }

    final boolean needsLookup() {
        // Tror ikke rigtig den fungere...
        // Det skal jo vaere relativt til en klasse...
        // F.eks. hvis X en public klasse, med en public constructor.
        // Og X er readable til A, men ikke B.
        // Saa har A ikke brug for et Lookup Object, men B har.
        // Ved ikke rigtig hvad denne skal bruges til....
        // Maa betyde om man skal
        return false;
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public final TypeLiteral<T> typeLiteral() {
        // Passer ikke hvis vi bruger map()...
        return factory.handle.returnType();
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public final List<VariableDescriptor> variables() {
        // this list is static...

        // Returns empty list for type variables for now...
        throw new UnsupportedOperationException();

        // If we have a List<VariableDescriptor> unboundVariables()...
        // How would composite + primed be treated...
    }

    /** {@inheritDoc} */
    @Override
    public final Factory<T> withKey(Key<?> key) {
        // Must be compatible with key in some way
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
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
    @Override
    public final Factory<T> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        return new BaseFactory<>(new FactorySupport<T>(factory.handle.withLookup(lookup), factory.dependencies));
    }

    /** {@inheritDoc} */
    @Override
    public final Factory<T> withVariable(int index, @Nullable Object argument) {
        // Problemet med at fjerne ting fra #variables() er at saa bliver index'et lige pludselig aendret.
        // F.eks. for dooo(String x, String y)
        // Og det gider vi ikke....
        // Saa variables stay the same -> Why shouldn't we able to bind them...

        // Maybe add isVariableBound(int index)

        // Rebinding? Ja hvorfor ikke... maaske have en #unbindable()

        // Har vi en optional MemberDescriptor?????

        // Hvis man nu vil injecte en composite....

        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final Factory<T> withVariableSupplier(int index, Supplier<?> supplier) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
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
    // Can cache it with a Class[] array corresponding to type parameters...
    public static <T> Factory<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        if (!ModuleAccess.base().isCanonicalized(implementation)) {
            // We cache factories for all "new TypeLiteral<>(){}"
            return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.type();
        if (t instanceof Class) {
            return (Factory<T>) find((Class<?>) t);
        } else {
            return new BaseFactory<>(FactoryFindInjectableExecutable.find(implementation));
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
        return new BaseFactory<>(new FactorySupport<>(InstanceFactoryHandle.of(instance), List.of()));
    }
}
