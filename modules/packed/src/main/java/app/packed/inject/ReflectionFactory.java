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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.TypeToken;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.invoke.MemberScanner;
import packed.internal.invoke.typevariable.TypeVariableExtractor;
import packed.internal.util.BasePackageAccess;

/**
 *
 */
// Maaske returnere ReflectionFactory med en lookup
public abstract class ReflectionFactory<T> extends Factory<T> {

    private ReflectionFactory(TypeToken<T> typeLiteralOrKey) {
        super(typeLiteralOrKey);
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

    // * <pre>
//  * Factory<List<String>> f = Factory.ofConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {
//  * });
//  * </pre>
    public static <T> Factory<T> ofConstructor(Constructor<?> constructor, TypeToken<T> type) {
        requireNonNull(constructor, "constructor is null");
        // TODO we probably need to validate the type literal here
        return new ExecutableFactory<>(type, constructor);
    }

    /** A factory that wraps a method or constructor. */
    static final class ExecutableFactory<T> extends ReflectionFactory<T> {

        private final List<InternalDependency> dependencies;

        /** A factory with an executable as a target. */
        public final Executable executable;

        private ExecutableFactory(ExecutableFactory<?> from, TypeToken<T> key) {
            super(key);
            this.executable = from.executable;
            this.dependencies = from.dependencies;
        }

        private ExecutableFactory(TypeToken<T> key, Class<?> findConstructorOn) {
            super(key);
            this.executable = MemberScanner.getConstructor(findConstructorOn, true, e -> new IllegalArgumentException(e));
            this.dependencies = InternalDependency.fromExecutable(executable);
        }

        private ExecutableFactory(TypeToken<T> key, Constructor<?> constructor) {
            super(key);
            this.executable = constructor;
            this.dependencies = InternalDependency.fromExecutable(executable);
        }

        /** {@inheritDoc} */
        @Override
        List<InternalDependency> dependencies() {
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
                if (executable instanceof Constructor<?> c) {
                    methodHandle = lookup.unreflectConstructor(c);
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

    /** A cache of factories used by {@link #of(Class)}. */
    private static final ClassValue<ExecutableFactory<?>> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        protected ExecutableFactory<?> computeValue(Class<?> implementation) {
            return new ExecutableFactory<>(TypeToken.of(implementation), implementation);
        }
    };

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeToken.class);

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

    /** An invoker that can read and write fields. */
    static final class FieldFactory<T> extends ReflectionFactory<T> {

        /** The field we invoke. */
        private final Field field;

        @SuppressWarnings("unchecked")
        private FieldFactory(Field field) {
            super((TypeToken<T>) TypeToken.fromField(field));
            this.field = field;
        }

        /** {@inheritDoc} */

        @Override
        List<InternalDependency> dependencies() {
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
     * first variable. Use {@link #provide(Object)} to bind an instance of the declaring class.
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
    // Lad os se hvad der sker med Map og generiks
    // InjectSupport.defaultInjectable()
    
    // If @Initialize -> rename to findInitializer
    @SuppressWarnings("unchecked")
    public static <T> /* ReflectionFactory<T> */ Factory<T> of(Class<T> implementation) {
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
    // Hmm vi har jo ikke parameterized beans???
    public static <T> Factory<T> of(TypeToken<T> implementation) {
        // Can cache it with a Class[] array corresponding to type parameters...
        requireNonNull(implementation, "implementation is null");
        if (!implementation.isCanonicalized()) {
            // We cache factories for all "new TypeToken<>(){}"
            return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.type();
        if (t instanceof Class<?> cl) {
            return (Factory<T>) of(cl);
        } else {
            ExecutableFactory<?> f = CLASS_CACHE.get(implementation.rawType());
            return new ExecutableFactory<>(f, implementation);
        }
    }

    // ReflectionFactory.of
    public static <T> Factory<T> ofConstructor(Constructor<?> constructor, Class<T> type) {
        requireNonNull(type, "type is null");
        return ofConstructor(constructor, TypeToken.of(type));
    }

}
