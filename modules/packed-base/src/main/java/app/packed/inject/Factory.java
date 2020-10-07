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
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import app.packed.introspection.ExecutableDescriptor;
import app.packed.introspection.FieldDescriptor;
import app.packed.introspection.VariableDescriptor;
import packed.internal.classscan.util.ConstructorUtil;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.sidecar.ModuleAccess;
import packed.internal.invoke.typevariable.TypeVariableExtractor;
import packed.internal.methodhandle.LookupUtil;

/**
 * An object that creates other objects. Factories are always immutable and any method that returnsfactory is an
 * immutable that creates this
 * 
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class does not expose any methods that actually create new objects, this is all hidden in the internals of
 * Packed. This might change in the future, but for now users can only create factories, and not consume their output.
 * 
 * @apiNote In the future, if the Java language permits, {@link Factory} may become a {@code sealed} interface, which
 *          would prohibit subclassing except by explicitly permitted types.
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

// This class used to provide some bind methods...
// But we don't do that no more. Because it was just impossible to
// see what was what...

// Its friend the abstract class Procedure... like Factory but no return..
// Then move it to base...
public abstract class Factory<T> {
    //////// TYPES (Raw)
    // ExactType... -> Instance, Constructor
    // LowerBoundType, Field, Method
    // PromisedType -> Fac0,Fac1,Fac2,

    /// TypeLiteral<- Always the promised, key must be assignable via raw type
    ///////////////

    // TypeLiteral
    // actual type

    // Correctness
    // Instance -> Lowerbound correct, upper correct
    // Executable -> Lower bound maybe correct (if exposedType=return type), upper correct if final return type
    // Rest, unknown all
    // Bindable -> has no effect..

    // static {
    // Dependency.of(String.class);// Initializes InternalApis for InternalFactory
    // }

    // Ideen er her. at for f.eks. Factory.of(XImpl, X) saa skal der stadig scannes paa Ximpl og ikke paa X

    /** A cache of extracted type variables from subclasses of this class. */
    static final ClassValue<TypeLiteral<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })

        protected TypeLiteral<?> computeValue(Class<?> type) {
            return TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0);
        }
    };

    /** A cache of factories used by {@link #of(Class)}. */
    private static final ClassValue<Factory<?>> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */

        protected Factory<?> computeValue(Class<?> implementation) {

            ExecutableDescriptor executable = ExecutableDescriptor.from(ConstructorUtil.findInjectableIAE(implementation));
            return new ExecutableFactory<>(TypeLiteral.of(implementation), executable, DependencyDescriptor.fromExecutable(executable));
        }
    };

    /**
     * A cache of factories used by {@link #of(TypeLiteral)}. This cache is only used by subclasses of TypeLiteral, never
     * literals that are manually constructed.
     */
    private static final ClassValue<Factory<?>> TYPE_LITERAL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */

        protected Factory<?> computeValue(Class<?> implementation) {
            Type t = TYPE_LITERAL_TV_EXTRACTOR.extract(implementation);
            return ExecutableFactory.fromTypeLiteral(ModuleAccess.base().toTypeLiteral(t));
        }
    };

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeLiteral.class);

    final Class<?> actualType;

    public final Key<T> key;

    /** The dependencies for this factory. */
    private final Class<? super T> type;

    /** The type of objects this factory creates. */
    public final TypeLiteral<T> typeLiteral;

    /**
     * Used by the various FactoryN constructor, because we cannot call {@link Object#getClass()} before calling a
     * constructor in this (super) class.
     */
    @SuppressWarnings("unchecked")
    Factory() {
        this.typeLiteral = (TypeLiteral<T>) CACHE.get(getClass());
        this.key = Key.fromTypeLiteral(typeLiteral);
        this.type = typeLiteral.rawType();
        this.actualType = requireNonNull(type);
    }

    private Factory(Class<T> type) {
        this(TypeLiteral.of(type), type);
    }

    private Factory(TypeLiteral<T> typeLiteralOrKey) {
        this(typeLiteralOrKey, typeLiteralOrKey.rawType());
    }

    private Factory(TypeLiteral<T> typeLiteralOrKey, Class<?> actualType) {
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        this.typeLiteral = typeLiteralOrKey;
        this.key = Key.fromTypeLiteral(typeLiteral);
        this.type = typeLiteral.rawType();
        this.actualType = requireNonNull(actualType);
    }

    /**
     * Binds the specified argument to a variable with the specified index as returned by {@link #variables()}. This method
     * is typically used to bind arguments to parameters on a method or constructors when key-based binding is not
     * sufficient. A typical example is a constructor with two parameters of the same type.
     * 
     * @param index
     *            the index of the variable to bind
     * @param argument
     *            the (nullable) argument to bind
     * @return a new factory
     * @throws IndexOutOfBoundsException
     *             if the specified index does not represent a valid variable in {@link #variables()}
     * @throws ClassCastException
     *             if the specified argument is not compatible with the actual type of the variable
     * @throws NullPointerException
     *             if the specified argument is null and the variable does not represent a reference type
     */

    public final Factory<T> bind(int index, @Nullable Object argument) {
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

        throw new UnsupportedOperationException();
    }

    public final Factory<T> bind(int index, Supplier<?> supplier) {
        throw new UnsupportedOperationException();
    }

    protected T checkLowerbound(T instance) {
        if (!type.isInstance(instance)) {
            // TODO I think this should probably be a Make Exception....
            // IDeen er at de har "løjet" om hvad de returnere.
            throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " + instance.getClass());
        }
        return instance;
    }

    abstract List<DependencyDescriptor> dependencies();

    final List<?> dependenciesx() {
        // What if have Factory f = Factory.of(Foo(String x, String y));
        // f.bindVariable(0, "FooBar");
        // Now the first parameter (with Key String) is bound.
        // But not the second parameter (also with Key String)
        // What if we bind String now??? Only too second parameter?

        throw new UnsupportedOperationException();

        // Factory<T> narrow() <- removes bound dependencies/parameters()...
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
        return key;
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
    // Kan vi finde en usecase???
    final <R> Factory<R> mapTo0(Factory1<? super T, ? extends R> mapper) {
        // Factory<String> f = null;
        // @SuppressWarnings({ "null", "unused" })
        // Create a factory by taking the output and mapping it...
        // Factory<Integer> fi = f.mapTo0(new Factory1<>(e -> e.length()) {});
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new factory that will perform the specified action after the factory has produced an object. But before the
     * instance is used anywhere.
     * 
     * @param action
     *            the post construction action
     * @return the new factory
     */
    public final Factory<T> postConstruction(Consumer<? super T> action) {
        return new PostConstructionFactory<>(this, action);
    }

    /**
     * Returns the raw type of the type of objects this factory provide. This is also the type that is used for annotation
     * scanning, for example, for finding fields annotated with {@link Inject}.
     *
     * @return the raw type of the type of objects this factory provide
     * @see #typeLiteral()
     */

    public final Class<? super T> rawType() {
        return typeLiteral().rawType();
    }

//    final boolean needsLookup() {
    // Needs Realm?

//        // Tror ikke rigtig den fungere...
//        // Det skal jo vaere relativt til en klasse...
//        // F.eks. hvis X en public klasse, med en public constructor.
//        // Og X er readable til A, men ikke B.
//        // Saa har A ikke brug for et Lookup Object, men B har.
//        // Ved ikke rigtig hvad denne skal bruges til....
//        // Maa betyde om man skal
//        return false;
//    }

    /**
     * Returns the type of objects this operation returns on invocation.
     *
     * @return the type of objects this operation returns on invocation
     */
    final TypeLiteral<T> returnType() {
        return typeLiteral;
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

    abstract MethodHandle toMethodHandle(Lookup lookup);

    /**
     * Returns the type of the type of objects this factory provide.
     *
     * @return the type of the type of objects this factory provide
     * @see #rawType()
     */

    public final TypeLiteral<T> typeLiteral() {
        // Passer ikke hvis vi bruger map()...
        return typeLiteral;
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
     * Returns an immutable list of all variables (typically fields or parameters) that needs to be successfully injected in
     * order for the factory to provide a new value.
     * <p>
     * The list returned by this method is unaffected by any previous bindings to specific variables. For example, via
     * {@link #bind(int, Object)}.
     * <p>
     * Any factory created via {@link #ofInstance(Object)} will return an empty list.
     * 
     * @return any variables that was used to construct the factory
     */
    // input, output...

    public final List<VariableDescriptor> variables() {
        // this list is static...

        // Returns empty list for type variables for now...
        throw new UnsupportedOperationException();

        // If we have a List<VariableDescriptor> unboundVariables()...
        // How would composite + primed be treated...
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
     * the specified lookup object to access any underlying member whenever this framework needs to access.
     * <p>
     * This method is useful, for example, to make a factory publically available for an class that does not have a public
     * constructor.
     * <p>
     * The specified lookup object will always be preferred, even when, for example, being registered with a bundle who has
     * its own lookup object.
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
    public final Factory<T> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        if (this instanceof ExecutableFactory || this instanceof FieldFactory) {
            return new LookedUpFactory<>(this, toMethodHandle(lookup));
        }
        throw new UnsupportedOperationException(
                "This method is only supported by factories created from a field, constructor or method. And must be applied as the first operation after creating the factory");
    }

    /**
     * Tries to find a single static method or constructor on the specified class using the following rules:
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
    // Todo rename to make (or just of....) Nej, syntes maaske den er find med find()...
    // Rename of()... syntes det er fint den hedder of()... og saa er det en fejl situation
    // Eneste er vi generalt returnere en optional for find metoder...
    // Har droppet at kalde den find... Fordi find generelt returnere en Optional...
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> of(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return (Factory<T>) CLASS_CACHE.get(implementation);
    }

    /**
     * This method is equivalent to {@link #of(Class)} except taking a type literal.
     *
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> of(TypeLiteral<T> implementation) {
        // Can cache it with a Class[] array corresponding to type parameters...
        requireNonNull(implementation, "implementation is null");
        if (!ModuleAccess.base().isCanonicalized(implementation)) {
            // We cache factories for all "new TypeLiteral<>(){}"
            return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.type();
        if (t instanceof Class) {
            return (Factory<T>) of((Class<?>) t);
        } else {
            return ExecutableFactory.fromTypeLiteral(implementation);
        }
    }

    /**
     * Returns a factory that returns the specified instance every time the factory most provide a value.
     * <p>
     * If the specified instance makes use of field or method injection the returned factory should not be used more than
     * once. As these fields and members will be injected every time, possible concurrently, an instance is provided by the
     * factory.
     * 
     * @param <T>
     *            the type of value returned by the factory
     * @param instance
     *            the instance to return for every request
     * @return the factory
     */
    public static <T> Factory<T> ofInstance(T instance) {
        requireNonNull(instance, "instance is null");
        return new InstanceFactory<T>(instance);
    }

    /** A factory that wraps a method or constructor. */
    static final class ExecutableFactory<T> extends Factory<T> {

        /**
         * Whether or not we need to check the lower bound of the instances we return. This is only needed if we allow, for
         * example to register CharSequence fooo() as String.class. And I'm not sure we allow that..... Maybe have a special
         * Factory.overrideMethodReturnWith(), and then not allow it as default..
         */
        final boolean checkLowerBound;

        private final List<DependencyDescriptor> dependencies;

        /** A factory with an executable as a target. */
        public final ExecutableDescriptor executable;

        private final Object instance = null;

        private ExecutableFactory(TypeLiteral<T> key, ExecutableDescriptor executable, List<DependencyDescriptor> dependencies) {
            super(key);
            this.executable = executable;
            this.checkLowerBound = false;
            this.dependencies = dependencies;
        }

        @Override
        List<DependencyDescriptor> dependencies() {
            return dependencies;
        }

        /** {@inheritDoc} */

        @Override
        MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle methodHandle;
            try {
                if (Modifier.isPrivate(executable.getModifiers())) {
                    lookup = lookup.in(executable.getDeclaringClass());
                }
                methodHandle = executable.unreflect(lookup);
            } catch (IllegalAccessException e) {
                throw new InaccessibleMemberException(
                        "No access to the " + executable.descriptorTypeName() + " " + executable + " with the specified lookup object", e);
            }

            MethodHandle mh = methodHandle;
            if (executable.isVarArgs()) {
                mh = mh.asFixedArity();
            }
            if (instance != null) {
                return mh.bindTo(instance);
            }
            return mh;
        }

        @Override
        public String toString() {
            return executable.toString();
        }

        // Should we have a strict type? For example, a static method on MyExtension.class
        // must return MyExtension... Det maa de sgu alle.. Den anden er findMethod()...
        // MyExtension.class create()

        static <T> Factory<T> fromTypeLiteral(TypeLiteral<T> implementation) {
            ExecutableDescriptor executable = ExecutableDescriptor.from(ConstructorUtil.findInjectableIAE(implementation.rawType()));
            return new ExecutableFactory<>(implementation, executable, DependencyDescriptor.fromExecutable(executable));
        }
    }

    /** An invoker that can read and write fields. */
    static final class FieldFactory<T> extends Factory<T> {

        /** The field we invoke. */
        private final FieldDescriptor field;

        private final Object instance = null;

        @SuppressWarnings("unchecked")
        FieldFactory(FieldDescriptor field) {
            super((TypeLiteral<T>) field.getTypeLiteral());
            this.field = field;
        }

        /** {@inheritDoc} */

        @Override
        List<DependencyDescriptor> dependencies() {
            return List.of();
        }

        /**
         * Compiles the code to a single method handle.
         * 
         * @return the compiled method handle
         */

        @Override
        MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle handle;
            try {
                if (Modifier.isPrivate(field.getModifiers())) {
                    // vs MethodHandles.private???
                    lookup = lookup.in(field.getDeclaringClass());
                }
                handle = field.unreflectGetter(lookup);
            } catch (IllegalAccessException e) {
                throw new InaccessibleMemberException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
            }
            if (instance != null) {
                handle = handle.bindTo(instance);
            }
            return handle;
        }
    }

    /** A factory that provides the same value every time, used by {@link Factory#ofInstance(Object)}. */
    private static final class InstanceFactory<T> extends Factory<T> {

        /** The value that is returned every time. */
        private final T instance;

        @SuppressWarnings("unchecked")
        private InstanceFactory(T instance) {
            super((Class<T>) instance.getClass());
            this.instance = instance;
        }

        /** {@inheritDoc} */
        @Override
        public List<DependencyDescriptor> dependencies() {
            return List.of();
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle toMethodHandle(Lookup ignore) {
            return MethodHandles.constant(instance.getClass(), instance);
        }
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    private static final class LookedUpFactory<T> extends Factory<T> {

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        /** The method handle that was unreflected. */
        private final MethodHandle methodHandle;

        private LookedUpFactory(Factory<T> delegate, MethodHandle methodHandle) {
            super(delegate.typeLiteral);
            this.delegate = delegate;
            this.methodHandle = requireNonNull(methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        List<DependencyDescriptor> dependencies() {
            return delegate.dependencies();
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle toMethodHandle(Lookup ignore) {
            return methodHandle;
        }
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    private static final class PostConstructionFactory<T> extends Factory<T> {

        /** A method handle for {@link Function#apply(Object)}. */
        private static final MethodHandle ACCEPT = LookupUtil.lookupVirtualPublic(Consumer.class, "accept", void.class, Object.class);

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        /** The method handle that was unreflected. */
        private final Consumer<? super T> action;

        private PostConstructionFactory(Factory<T> delegate, Consumer<? super T> action) {
            super(delegate.typeLiteral);
            this.delegate = delegate;
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        List<DependencyDescriptor> dependencies() {
            return delegate.dependencies();
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle toMethodHandle(Lookup ignore) {
            System.out.println(ACCEPT);
            System.out.println(action);
            throw new UnsupportedOperationException();
        }
    }
}

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
//// FactoryDescriptor.of(Factory f) <--- in devtools???
//
//@Override
//public final <S> Factory<T> bind(Key<S> key, @Nullable S instance) {
//  throw new UnsupportedOperationException();
//}
//
///** {@inheritDoc} */
//@Override
//@SuppressWarnings({ "rawtypes", "unchecked" })
//public final Factory<T> bind(Object instance) {
//  requireNonNull(instance, "instance is null");
//  return bind((Class) instance.getClass(), instance);
//
//  // someExtension()
//  // install(Factory.of(Foo.class).withArgument(this))).
//
//  // There is going to be some automatic support for injecting extensions into
//  // services installed by them. We are just not quite there yet.
//  // Will bind to any assignable parameter...
//}
//
///** {@inheritDoc} */
//@Override
//public final <S> Factory<T> bindSupplier(Class<S> key, Supplier<?> supplier) {
//  // Altsaa vi kan vel bruge et andet factory????
//  // En mulig usecase f.eks. for Factory1 er at kunne mappe dependencies...
//  // f.eks. fra Foo(CardReader) -> new Factory0<
//  // new Factory0<>(e->e);
//  // withArgumentSupplier
//  throw new UnsupportedOperationException();
//}
//
///** {@inheritDoc} */
//@Override
//public final <S> Factory<T> bindSupplier(Key<S> key, Supplier<?> supplier) {
//  throw new UnsupportedOperationException();
//}