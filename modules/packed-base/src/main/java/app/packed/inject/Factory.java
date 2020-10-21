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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.OldVariable;
import app.packed.base.TypeToken;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.FindInjectableConstructor;
import packed.internal.invoke.typevariable.TypeVariableExtractor;
import packed.internal.util.BasePackageAccess;
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
// Its friend the abstract class Procedure... like Factory but no return..
// Then move it to base...
// Not a Function because it takes annotations...
public abstract class Factory<T> {

  /** A cache of extracted type variables from subclasses of this class. */
  static final ClassValue<TypeToken<?>> CACHE = new ClassValue<>() {

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })

    protected TypeToken<?> computeValue(Class<?> type) {
      return TypeToken.fromTypeVariable((Class) type, Factory.class, 0);
    }
  };

  /** A cache of factories used by {@link #of(Class)}. */
  private static final ClassValue<ExecutableFactory<?>> CLASS_CACHE = new ClassValue<>() {

    /** {@inheritDoc} */
    protected ExecutableFactory<?> computeValue(Class<?> implementation) {
      return new ExecutableFactory<>(TypeToken.of(implementation), implementation);
    }
  };

  /** A method handle for invoking {@link #create(Supplier, Class)}. */
  private static final MethodHandle CREATE = LookupUtil.lookupStatic(MethodHandles.lookup(), "create", Object.class, Supplier.class, Class.class);

  /**
   * A cache of factories used by {@link #of(TypeToken)}. This cache is only used by subclasses of TypeLiteral, never
   * literals that are manually constructed.
   */
  private static final ClassValue<ExecutableFactory<?>> TYPE_LITERAL_CACHE = new ClassValue<>() {

    /** {@inheritDoc} */
    protected ExecutableFactory<?> computeValue(Class<?> implementation) {
      Type t = TYPE_LITERAL_TV_EXTRACTOR.extract(implementation);
      TypeToken<?> tl = BasePackageAccess.base().toTypeLiteral(t);
      return new ExecutableFactory<>(tl, tl.rawType());
    }
  };

  /** A type variable extractor. */
  private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeToken.class);

  private final Key<T> key;

  /**
   * The factory's method handle. Is initial null for factories that need access to a {@link Lookup} object.
   * <p>
   * With respect to the Java Memory Model, any method handle will behave as if all of its (internal) fields are final
   * variables. This means that any method handle made visible to the application will always be fully formed. This is
   * true even if the method handle is published through a shared variable in a data race.
   */
  private MethodHandle methodHandle;

  /** The type of objects this factory creates. */
  private final TypeToken<T> typeLiteral;

  // private Object cache; // maaske er det en general cache.. der ogsaa kan indeholde Key
  // Hov, vi bliver ogsaa noedt til at cache MethodHandle fra field/method/ect...

  /**
   * Used by the various FactoryN constructor, because we cannot call {@link Object#getClass()} before calling a
   * constructor in this (super) class.
   */
  @SuppressWarnings("unchecked")
  Factory() {
    this.typeLiteral = (TypeToken<T>) CACHE.get(getClass());
    this.key = Key.fromTypeLiteral(typeLiteral);
    this.methodHandle = null;
  }

  /**
   * Creates a new factory, that use the specified supplier to provide values.
   *
   * @param supplier
   *          the supplier that will provide the actual values. The supplier should never return null, but should instead
   *          throw a relevant exception if unable to provide a value
   * @throws FactoryException
   *           if the type variable R could not be determined. Or if R does not represent a valid key, for example,
   *           {@link Optional}
   */
  @SuppressWarnings("unchecked")
  protected Factory(Supplier<? extends T> supplier) {
    requireNonNull(supplier, "supplier is null");
    this.typeLiteral = (TypeToken<T>) CACHE.get(getClass());
    this.key = Key.fromTypeLiteral(typeLiteral);
    MethodHandle mh = CREATE.bindTo(supplier).bindTo(rawType()); // (Supplier, Class)Object -> ()Object
    this.methodHandle = MethodHandleUtil.castReturnType(mh, rawType()); // ()Object -> ()R
  }

  private Factory(TypeToken<T> typeLiteralOrKey) {
    requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
    this.typeLiteral = typeLiteralOrKey;
    this.key = Key.fromTypeLiteral(typeLiteral);
    this.methodHandle = null;
  }

  /**
   * Binds the specified argument(s) to a variable with the specified index as returned by {@link #variables()}. This
   * method is typically used to bind arguments to parameters on a method or constructors when key-based binding is not
   * sufficient. A typical example is a constructor with two parameters of the same type.
   * 
   * @param position
   *          the index of the variable to bind
   * @param argument
   *          the (nullable) argument to bind
   * @param additionalArguments
   *          any additional (nullable) arguments to bind
   * @return a new factory
   * @throws IndexOutOfBoundsException
   *           if the specified index does not represent a valid variable in {@link #variables()}
   * @throws ClassCastException
   *           if an argument does not match the corresponding variable type.
   * @throws IllegalArgumentException
   *           if (@code position) is less than {@code 0} or greater than {@code N - 1 - L} where {@code N} is the number
   *           of dependencies and {@code L} is the length of the additional argument array.
   * @throws NullPointerException
   *           if the specified argument is null and the variable does not represent a reference type
   */
  public final Factory<T> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
    requireNonNull(additionalArguments, "additionalArguments is null");
    List<DependencyDescriptor> dependencies = dependencies();
    Objects.checkIndex(position, dependencies.size());
    int len = 1 + additionalArguments.length;
    int newLen = dependencies.size() - len;
    if (newLen < 0) {
      throw new IllegalArgumentException(
          "Cannot specify more than " + (len - position) + " arguments for position = " + position + ", but arguments array was size " + len);
    }

    // Removing dependencies that are being replaced
    DependencyDescriptor[] dd = new DependencyDescriptor[newLen];
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

    return new BindingFactory<>(this, position, dd, args);
  }

  /**
   * Binds the first variable to the specified argument.
   * <p>
   * 
   * @param argument
   *          the argument to bind.
   * @return the new factory
   */
  public final Factory<T> bind(@Nullable Object argument) {
    return bind(0, argument);
  }

  final Factory<T> bindSupplier(int index, Supplier<?> supplier) {
    throw new UnsupportedOperationException();
  }

  // taenker vi laver den her public og saa bare caster...
  List<DependencyDescriptor> dependencies() {
    return List.of();
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

    return mapTo(TypeToken.of(key), mapper);
  }

  /**
   * Returns a new factory that maps every object this factory create using the specified mapper.
   * 
   * @param <R>
   *          the type of result to map to
   * @param type
   *          the type of the mapped value
   * @param mapper
   *          the mapper used to map the result
   * @return a new mapped factory
   */
  // How do we handle key??? Think we might need a version that also takes a key.
  final <R> Factory<R> mapTo(TypeToken<R> type, Function<? super T, ? extends R> mapper) {
    // MappingFactoryHandle<T, R> f = new MappingFactoryHandle<>(type, factory.handle, mapper);
    // return new Factory<>(new FactorySupport<>(f, factory.dependencies));
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a new factory that maps every object this factory create using the specified mapper.
   * 
   * @param <R>
   *          the type of result to map to
   * @param mapper
   *          the mapper used to map the result
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
   *          the post construction action
   * @return the new factory
   */
  public final Factory<T> postConstruction(Consumer<? super T> action) {
    return new PostConstructionFactory<>(this, action);
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

  /**
   * @param lookup
   *          a lookup that can be used to unreflect fields, constructors or methods.
   * @return a new method handle
   */
  MethodHandle toMethodHandle(Lookup lookup) {
    return methodHandle;
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
   * Returns the type of the type of objects this factory provide.
   *
   * @return the type of the type of objects this factory provide
   * @see #rawType()
   */
  public final TypeToken<T> typeLiteral() {
    return typeLiteral;
  }

  final Factory<T> useExactType(Class<? extends T> type) {
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
   * Returns the number of variables this factory has.
   * 
   * @return the number of variables this factory has
   */
  public final int variableCount() {
    return dependencies().size();
  }

  /**
   * Returns an immutable list of all variables (typically fields or parameters) that needs to be successfully injected in
   * order for the factory to provide a new value.
   * <p>
   * The list returned by this method is affected by any previous bindings to specific variables. For example, via
   * {@link #bind(int, Object, Object...)}.
   * <p>
   * Factories created via {@link #ofInstance(Object)} always return an empty list.
   * 
   * @return any variables that was used to construct the factory
   */
  // input, output...
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public final List<OldVariable> variables() {
    return (List) dependencies();
  }

  /**
   * Returns a new factory retaining all of the existing properties of this factory. Except that the key returned by
   * {@link #key()} will be changed to the specified key.
   * 
   * @param key
   *          the key under which to bind the factory
   * @return the new factory
   * @throws ClassCastException
   *           if the type of the key does not match the type of instances this factory provides
   * @see #key()
   */

  public final Factory<T> withKey(Key<?> key) {
    // Just make a new KeyedFactory
    // Hvor kun noeglen er aendret....
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
   *          the lookup object
   * @return a new factory with uses the specified lookup object when accessing the underlying member
   * @throws InaccessibleMemberException
   *           if the specified lookup object does not give access to the underlying member
   * @throws UnsupportedOperationException
   *           if this factory was not created from either a field, constructor or method.
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

  /**
   * Supplies a value.
   * 
   * @param <T>
   *          the type of value supplied
   * @param supplier
   *          the supplier that supplies the actual value
   * @param expectedType
   *          the type we expect the supplier to return
   * @return the value that was supplied by the specified supplier
   * @throws FactoryException
   *           if the created value is null or not assignable to the raw type of the factory
   */
  @SuppressWarnings("unused") // only invoked via #CREATE
  private static <T> T create(Supplier<? extends T> supplier, Class<?> expectedType) {
    T value = supplier.get();
    checkReturnValue(expectedType, value, supplier);
    return value;
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
   *          the implementation type
   * @param implementation
   *          the implementation type
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
   *          the implementation type
   * @param implementation
   *          the implementation type
   * @return a factory for the specified implementation type
   */
  @SuppressWarnings("unchecked")
  public static <T> Factory<T> of(TypeToken<T> implementation) {
    // Can cache it with a Class[] array corresponding to type parameters...
    requireNonNull(implementation, "implementation is null");
    if (!BasePackageAccess.base().isCanonicalized(implementation)) {
      // We cache factories for all "new TypeLiteral<>(){}"
      return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
    }
    Type t = implementation.type();
    if (t instanceof Class) {
      return (Factory<T>) of((Class<?>) t);
    } else {
      ExecutableFactory<?> f = CLASS_CACHE.get(implementation.rawType());
      return new ExecutableFactory<>(f, implementation);
    }
  }

  public static <T> Factory<T> ofConstructor(Constructor<?> constructor, Class<T> type) {
    requireNonNull(type, "type is null");
    return ofConstructor(constructor, TypeToken.of(type));
  }

  // new Factory<String>(SomeMethod);
  // How we skal have
  // Maaske kan vi

  // If the specified method is an instance method
  // variables will include a dependenc for it as the first
  // parameters

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
   *          the constructor used from creating an instance
   * @param type
   *          a type literal
   * @return the new factory
   * @see #of(Constructor)
   */
  @SuppressWarnings("javadoc")
  public static <T> Factory<T> ofConstructor(Constructor<?> constructor, TypeToken<T> type) {
    requireNonNull(constructor, "constructor is null");
    // TODO we probably need to validate the type literal here
    return new ExecutableFactory<>(type, constructor);
  }

  /**
   * Creates a new factory that uses the specified constructor to create new instances.
   *
   * @param constructor
   *          the constructor used for creating new instances
   * @return the new factory
   */
  public static <T> Factory<T> ofConstructor(Constructor<T> constructor) {
    requireNonNull(constructor, "constructor is null");
    TypeToken<T> tl = TypeToken.of(constructor.getDeclaringClass());
    return new ExecutableFactory<>(tl, constructor);
  }

  /**
   * Returns a factory that returns the specified instance every time the factory most provide a value.
   * <p>
   * If the specified instance makes use of field or method injection the returned factory should not be used more than
   * once. As these fields and members will be injected every time, possible concurrently, an instance is provided by the
   * factory.
   * 
   * @param <T>
   *          the type of value returned by the factory
   * @param instance
   *          the instance to return for every request
   * @return the factory
   */
  public static <T> Factory<T> ofInstance(T instance) {
    requireNonNull(instance, "instance is null");
    return new InstanceFactory<T>(instance);
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
   * first variable. Use {@link #bind(Object)} to bind an instance of the declaring class.
   * 
   * @param <T>
   *          the type of value returned by the method
   * @param method
   *          the method to wrap
   * @param returnType
   *          the type of value returned by the method
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

  /** A special factory created via {@link #withLookup(Lookup)}. */
  // A simple version of Binding... Maybe just only have one
  private static final class BindingFactory<T> extends Factory<T> {

    private final Object[] arguments;

    /** The ExecutableFactor or FieldFactory to delegate to. */
    private final Factory<T> delegate;

    private final List<DependencyDescriptor> dependencies;

    /** The ExecutableFactor or FieldFactory to delegate to. */
    private final int index;

    private BindingFactory(Factory<T> delegate, int index, DependencyDescriptor[] dd, Object[] arguments) {
      super(delegate.typeLiteral);
      this.index = index;
      this.delegate = requireNonNull(delegate);
      this.arguments = arguments;
      this.dependencies = List.of(dd);
    }

    /** {@inheritDoc} */
    @Override
    List<DependencyDescriptor> dependencies() {
      return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    MethodHandle toMethodHandle(Lookup lookup) {
      MethodHandle mh = delegate.toMethodHandle(lookup);
      return MethodHandles.insertArguments(mh, index, arguments);
    }
  }

  /** A factory that wraps a method or constructor. */
  private static final class ExecutableFactory<T> extends Factory<T> {

    private final List<DependencyDescriptor> dependencies;

    /** A factory with an executable as a target. */
    public final Executable executable;

    private ExecutableFactory(ExecutableFactory<?> from, TypeToken<T> key) {
      super(key);
      this.executable = from.executable;
      this.dependencies = from.dependencies;
    }

    private ExecutableFactory(TypeToken<T> key, Class<?> findConstructorOn) {
      super(key);
      this.executable = FindInjectableConstructor.findConstructorIAE(findConstructorOn);
      this.dependencies = DependencyDescriptor.fromExecutable(executable);
    }

    private ExecutableFactory(TypeToken<T> key, Constructor<?> constructor) {
      super(key);
      this.executable = constructor;
      this.dependencies = DependencyDescriptor.fromExecutable(executable);
    }

    /** {@inheritDoc} */
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
        if (executable instanceof Constructor) {
          methodHandle = lookup.unreflectConstructor((Constructor<?>) executable);
        } else {
          methodHandle = lookup.unreflect((Method) executable);
        }

      } catch (IllegalAccessException e) {
        String name = executable instanceof Constructor ? "constructor" : "method";
        throw new InaccessibleMemberException("No access to the " + name + " " + executable + " with the specified lookup object", e);
      }

      MethodHandle mh = methodHandle;
      if (executable.isVarArgs()) {
        mh = mh.asFixedArity();
      }
      return mh;
    }

    @Override
    public String toString() {
      return executable.toString();
    }
  }

  /** An invoker that can read and write fields. */
  private static final class FieldFactory<T> extends Factory<T> {

    /** The field we invoke. */
    private final Field field;

    @SuppressWarnings("unchecked")
    private FieldFactory(Field field) {
      super((TypeToken<T>) TypeToken.fromField(field));
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
        handle = lookup.unreflectGetter(field);
      } catch (IllegalAccessException e) {
        throw new InaccessibleMemberException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
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
    List<DependencyDescriptor> dependencies() {
      return delegate.dependencies();
    }

    /** {@inheritDoc} */
    @Override
    MethodHandle toMethodHandle(Lookup ignore) {
      return methodHandle;
    }
  }

  /** A special factory created for {@link #postConstruction(Consumer)}}. */
  private static final class PostConstructionFactory<T> extends Factory<T> {

    /** A method handle for {@link Function#apply(Object)}. */
    private static final MethodHandle ACCEPT = LookupUtil.lookupStatic(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

    /** The method handle that was unreflected. */
    private final MethodHandle consumer;

    /** The ExecutableFactor or FieldFactory to delegate to. */
    private final Factory<T> delegate;

    private PostConstructionFactory(Factory<T> delegate, Consumer<? super T> action) {
      super(delegate.typeLiteral);
      this.delegate = delegate;
      MethodHandle mh = ACCEPT.bindTo(requireNonNull(action, "action is null"));
      this.consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(rawType(), rawType()));
    }

    /** {@inheritDoc} */
    @Override
    List<DependencyDescriptor> dependencies() {
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
