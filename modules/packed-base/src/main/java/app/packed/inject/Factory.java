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

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import app.packed.base.invoke.UncheckedIllegalAccessException;
import app.packed.base.reflect.VariableDescriptor;
import packed.internal.inject.BaseFactory;
import packed.internal.util.BaseSupport;

/**
 * An object that creates other objects. factory is an immutable that creates this
 * 
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class does not expose any methods that actually create new objects, this is all hidden in the internals of
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
public interface Factory<T> {

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
    <S> Factory<T> bind(Class<S> key, @Nullable S instance);

    <S> Factory<T> bind(Key<S> key, S instance);

    Factory<T> bind(Object instance);

    <S> Factory<T> bindSupplier(Class<S> key, Supplier<?> supplier);

    <S> Factory<T> bindSupplier(Key<S> key, Supplier<?> supplier);

    /**
     * Binds the specified argument to a variable with the specified index. This method is typically used to bind arguments
     * to parameters on a method or constructors when key-based binding is not sufficient. A typical example is a
     * constructor with two arguments..
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
    Factory<T> bindVariable(int index, Object argument);

    Factory<T> bindVariableSupplier(int index, Supplier<?> supplier);

    List<?> dependencies();

    /**
     * Returns a new factory that will perform the specified injection action after a factory has produced an object.
     * 
     * @param action
     *            the injection action
     * @return the new factory
     */
    Factory<T> inject(Consumer<? super T> action);

    <K> Factory<T> inject(Class<K> key, BiConsumer<? super T, ? super K> action);

    /**
     * Returns a new factory that will perform the specified injection action after a factory has produced an object.
     * 
     * @param <K>
     *            the type of service to inject
     * @param key
     *            the key of the dependency to inject
     * @param action
     *            the manual injection action
     * @return the new factory to return
     */
    <K> Factory<T> inject(Key<K> key, BiConsumer<? super T, ? super K> action);

    /**
     * The key under which If this factory is registered as a service. This method returns the (default) key that will be
     * used, for example, when regist Returns the (default) key to which this factory will bound to if using as If this
     * factory is used to register a service.
     *
     * @return the key under which this factory will be registered unless
     * @see #withKey(Key)
     */
    Key<T> key();

    boolean needsLookup();

    /**
     * Returns the raw type of the objects this factory creates.
     *
     * @return the raw type of the objects this factory creates
     */
    Class<? super T> rawType();

    /**
     * Returns the type of objects this factory creates.
     *
     * @return the type of objects this factory creates
     */
    TypeLiteral<T> typeLiteral();

    Factory<T> useExactType(Class<? extends T> type);

    /**
     * Returns an immutable list of variables (typically fields or parameters) that was used to construct this factory.
     * <p>
     * If this factory was created using {@link #fromInstance(Object)} the returned list is empty.
     * 
     * @return any variables that was used to construct the factory
     */
    List<VariableDescriptor> variables();

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
    Factory<T> withKey(Key<?> key);

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
    // Igen der er kun et problem, hvis metoden
    // Maaske skal vi tillade stacked MethodHandles..
    // Maaske skal vi endda have en SelectiveMethodHandle
    //// Ideen er at man kan pakke en method handle ind...
    // Stacked lookups..
    // Vi skal have en hel section omkring method handlers.

    // Lookup object paa et factory. Kan bruges til alle metoder....Ikke kun dem med inject
    // Giver ikke mening andet...
    Factory<T> withLookup(MethodHandles.Lookup lookup);

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
    // Todo rename to make (or just of....) Nej, syntes maaske den er find med find()...
    // Rename of()... syntes det er fint den hedder of()... og saa er det en fejl situation
    //
    public static <T> Factory<T> find(Class<T> implementation) {
        return BaseFactory.find(implementation);
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
    public static <T> Factory<T> find(TypeLiteral<T> implementation) {
        return BaseFactory.find(implementation);
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
        return BaseFactory.fromInstance(instance);
    }
}
