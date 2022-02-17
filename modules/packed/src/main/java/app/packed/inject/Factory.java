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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
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
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

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

// Det er vigtigt at vi binder og ikke injecter. Altsaa goer klar at vi udelukkene binder noget til den ene parameter.
// Its friend the abstract class Procedure... like Factory but no return..
// Then move it to base...
// Not a Function because it takes annotations...
// I think we might want an AbstractFactory...
public abstract class Factory<R> {

    /**
     * The factory's method handle. Is initial null for factories that need access to a {@link Lookup} object.
     * <p>
     * With respect to the Java Memory Model, any method handle will behave as if all of its (internal) fields are final
     * variables. This means that any method handle made visible to the application will always be fully formed. This is
     * true even if the method handle is published through a shared variable in a data race.
     */
    // So we cache it.. But access checks must be performed every time...
    // Only if it is a class..
    // Maybe we need a flag... like... You can use this method handle without doing any kind of
    // checks. Or you need to verify usage of this factory with the realm.
    // Basically that the underlying class is open to whatever realm there is
    private MethodHandle methodHandle;

    /** The type of objects this factory creates. */
    private final TypeToken<R> typeLiteral;

    /**
     * Used by {@link CapturingFactory} constructor, because we cannot call (an instance method) {@link Object#getClass()}
     * before calling this constructor.
     */
    @SuppressWarnings("unchecked")
    Factory() {
        this.typeLiteral = (TypeToken<R>) CapturingFactory.CACHE.get(getClass());
        this.methodHandle = null;
    }

    @SuppressWarnings("unchecked")
    Factory(boolean isCapturingFactory, Object functionInstance) {
        this.typeLiteral = (TypeToken<R>) CapturingFactory.CACHE.get(getClass());
        this.methodHandle = null;

        // Find CapturingFactoryMeta in a ClassValueCache
    }

    Factory(TypeToken<R> typeLiteralOrKey) {
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        this.typeLiteral = typeLiteralOrKey;
        this.methodHandle = null;
    }

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
        List<InternalDependency> dependencies = dependencies();
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
            dd[i] = dependencies().get(i);
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

        return new BoundFactory<>(this, position, dd, args);
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

    // taenker vi laver den her public og saa bare caster...
    List<InternalDependency> dependencies() {
        return List.of();
    }

//    /**
//     * The key under which If this factory is registered as a service. This method returns the (default) key that will be
//     * used, for example, when regist Returns the (default) key to which this factory will bound to if using as If this
//     * factory is used to register a service.
//     *
//     * @return the key under which this factory will be registered unless
//     * @see #withKey(Key)
//     */
//    public final Key<T> key() {
//        return key;
//    }

    /**
     * Returns an immutable list of all variables (typically fields or parameters) that needs to be successfully injected in
     * order for the factory to provide a new value.
     * <p>
     * The list returned by this method is affected by any previous bindings to specific variables. For example, via
     * {@link #bind(int, Object, Object...)}.
     * <p>
     * Factories created via {@link #ofConstant(Object)} always return an empty list.
     * 
     * @return any variables that was used to construct the factory
     */
    // input, output...
    @SuppressWarnings({ "rawtypes", "unchecked", "exports" })
    public final List<InternalDependency> dependenciesOld() {
        return (List) dependencies();
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
        return false;
    }

    /**
     * Returns a new factory that will perform the specified action immediately after the factory has constructed an object.
     * And before the constructed object is returned to the runtime.
     * 
     * @param action
     *            the action to run
     * @return the new factory
     */
    public final Factory<R> peek(Consumer<? super R> action) {
        return new PeekableFactory<>(this, action);
    }

    /**
     * Returns the (raw) type of values this factory provide. This is also the type that is used for annotation scanning,
     * for example, for finding fields annotated with {@link Inject}.
     *
     * @return the raw type of the type of objects this factory provide
     * @see #typeLiteral()
     */
    public final Class<?> rawType() {
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
     * @param lookup
     *            a lookup that can be used to unreflect fields, constructors or methods.
     * @return a new method handle
     */
    MethodHandle toMethodHandle(Lookup lookup) {
        return methodHandle;
    }

    /**
     * Returns the type of the type of objects this factory provide.
     *
     * @return the type of the type of objects this factory provide
     * @see #rawType()
     */
    public final TypeToken<R> typeLiteral() {
        return typeLiteral;
    }

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
        return dependencies().size();
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
        if (this instanceof ReflectiveFactory.ExecutableFactory || this instanceof ReflectiveFactory.FieldFactory) {
            return new LookedUpFactory<>(this, toMethodHandle(lookup));
        }
        throw new UnsupportedOperationException(
                "This method is only supported by factories created from a field, constructor or method. And must be applied as the first operation after creating the factory");
    }

    static void checkReturnValue(Class<?> expectedType, Object value, Object supplierOrFunction) {
        if (!expectedType.isInstance(value)) {
            String type = Supplier.class.isAssignableFrom(supplierOrFunction.getClass()) ? "supplier" : "function";
            if (value == null) {
                // NPE???
                throw new FactoryException("The " + type + " '" + supplierOrFunction + "' must not return null");
            } else {
                // throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " +
                // instance.getClass());
                throw new FactoryException("The \" + type + \" '" + supplierOrFunction + "' was expected to return instances of type " + expectedType.getName()
                        + " but returned a " + value.getClass().getName() + " instance");
            }
        }
    }

    // new Factory<String>(SomeMethod);
    // How we skal have
    // Maaske kan vi

    // If the specified method is an instance method
    // variables will include a dependenc for it as the first
    // parameters

    /**
     * Returns a factory that returns the specified instance every time the factory must provide a value.
     * <p>
     * If the specified instance makes use of field or method injection the returned factory should not be used more than
     * once. As these fields and members will be injected every time, possible concurrently, an instance is provided by the
     * factory.
     * 
     * @param <T>
     *            the type of value returned by the factory
     * @param constant
     *            the instance to return on every request
     * @return the factory
     */
    // What about annotations and type variables
    // ofConstant(List<int> l)
    public static <T> Factory<T> ofConstant(T constant) {
        requireNonNull(constant, "constant is null");
        return new ConstantFactory<T>(constant);
    }

    public static <T> Factory<T> ofMethodHandle(MethodHandle methodHandle) {
        throw new UnsupportedOperationException();
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    // A simple version of Binding... Maybe just only have one
    private static final class BoundFactory<T> extends Factory<T> {

        private final Object[] arguments;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        private final List<InternalDependency> dependencies;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final int index;

        private BoundFactory(Factory<T> delegate, int index, InternalDependency[] dd, Object[] arguments) {
            super(delegate.typeLiteral);
            this.index = index;
            this.delegate = requireNonNull(delegate);
            this.arguments = arguments;
            this.dependencies = List.of(dd);
        }

        /** {@inheritDoc} */
        @Override
        List<InternalDependency> dependencies() {
            return dependencies;
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = delegate.toMethodHandle(lookup);
            return MethodHandles.insertArguments(mh, index, arguments);
        }
    }

    /** A factory that provides the same value every time, used by {@link Factory#ofConstant(Object)}. */
    private static final class ConstantFactory<T> extends Factory<T> {

        /** The value that is returned every time. */
        private final T instance;

        @SuppressWarnings("unchecked")
        private ConstantFactory(T instance) {
            super((TypeToken<T>) TypeToken.of(instance.getClass()));
            this.instance = instance;
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
        List<InternalDependency> dependencies() {
            return delegate.dependencies();
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle toMethodHandle(Lookup ignore) {
            return methodHandle;
        }
    }

    /** A factory for {@link #peek(Consumer)}}. */
    private static final class PeekableFactory<T> extends Factory<T> {

        /** A method handle for {@link Function#apply(Object)}. */
        private static final MethodHandle ACCEPT = LookupUtil.lookupStatic(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

        /** The method handle that was unreflected. */
        private final MethodHandle consumer;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        private PeekableFactory(Factory<T> delegate, Consumer<? super T> action) {
            super(delegate.typeLiteral);
            this.delegate = delegate;
            MethodHandle mh = ACCEPT.bindTo(requireNonNull(action, "action is null"));
            this.consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(rawType(), rawType()));
        }

        /** {@inheritDoc} */
        @Override
        List<InternalDependency> dependencies() {
            return delegate.dependencies();
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = delegate.toMethodHandle(lookup);
            mh = MethodHandles.filterReturnValue(mh, consumer);
            return MethodHandleUtil.castReturnType(mh, rawType());
        }

        @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
        private static Object accept(Consumer consumer, Object object) {
            consumer.accept(object);
            return object;
        }
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
