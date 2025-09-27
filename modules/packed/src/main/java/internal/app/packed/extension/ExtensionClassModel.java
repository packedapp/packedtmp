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
package internal.app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import app.packed.Framework;
import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.FrameworkExtension;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.types.ClassUtil;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 * A model of an {@link Extension}. Exposed to end-users as {@link ExtensionDescriptor}.
 *
 * @implNote This could have been a record, but there are so many fields that that we get a better overview as a plain
 *           class.
 */
// Kan kalde dem Info klasser istedet for, hvis vi vil brug model externt i APIen
public final class ExtensionClassModel implements ExtensionDescriptor {

    /** A cache of all encountered extension models. */
    private static final ClassValue<ExtensionClassModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected ExtensionClassModel computeValue(Class<?> extensionClass) {
            // new Exception().printStackTrace();
            ClassUtil.checkProperSubclass(Extension.class, extensionClass, s -> new InternalExtensionException(s));
            // Check that framework extensions are in a framework module
            if (FrameworkExtension.class.isAssignableFrom(extensionClass)) {
                Module m = extensionClass.getModule();
                if (m.isNamed() && !Framework.moduleNames().contains(m.getName())) {
                    throw new InternalExtensionException("Extension " + extensionClass + " extends " + FrameworkExtension.class
                            + " but is not located in module(s) " + Framework.moduleNames() + " or the unnamed module");
                }
            }
            return Loader.load((Class) extensionClass, null);
        }
    };

    /** The direct dependencies of the extension. */
    private final Set<Class<? extends Extension<?>>> dependencies;

    /** The extension we model. */
    private final Class<? extends Extension<?>> extensionClass;

    /** A method handle for creating new instances of extensionClass. */
    private final MethodHandle mhConstructor; // (ExtensionSetup)Extension

    /** The (simple) name of the extension as returned by {@link Class#getSimpleName()}. */
    private final String name;

    /** The (canonical) full name of the extension. Used to deterministically sort extensions. */
    private final String nameFull;

    /** The {@link ExtensionDescriptor#orderingDepth() depth} of this extension. */
    private final int orderingDepth;

    private final ComponentRealm realm;

    /**
     * Creates a new extension model from the specified builder.
     *
     * @param builder
     *            the builder of the model
     */
    private ExtensionClassModel(Builder builder) {
        this.extensionClass = builder.extensionClass;
        this.realm = ComponentRealm.extension(extensionClass);
        this.mhConstructor = builder.mhConstructor;
        this.orderingDepth = builder.depth;
        this.dependencies = Set.copyOf(builder.dependencies);

        // Cache some frequently used strings.
        this.name = extensionClass.getSimpleName();
        this.nameFull = extensionClass.getCanonicalName();
    }

    /**
     * Checks that the specified extension is the same module as the extension represented by this model.
     *
     * @param eType
     *            the type look for an ExtensionMember annotation on
     * @return an extension the specified type is a member of
     * @throws InternalExtensionException
     *             if an annotation is present and the specified is not in the same module as the extension specified in
     *             next
     */
    @Nullable
    public Class<? extends Extension<?>> checkSameModule(Class<? extends Extension<?>> eType) {
        if (extensionClass.getModule() != eType.getModule()) {
            throw new InternalExtensionException("The extension " + eType + " and type " + extensionClass + " must be defined in the same module, was "
                    + eType.getModule() + " and " + extensionClass.getModule());
        }
        of(eType); // Make sure a valid model for the extension has been created
        return eType;
    }

    /** {@inheritDoc} */
    public int compareTo(ExtensionDescriptor descriptor) {
        ExtensionClassModel m = (ExtensionClassModel) descriptor;
        // if (m.getClassLoader()!=this.getClassLoader())
        // Start comparing by name

        // First we compare the depth of each extension
        // We do this first because it is fast
        int d = orderingDepth - m.orderingDepth;
        if (d != 0) {
            return d;
        }

        // Compare String lengths first (fast integer comparison)
        // Only if the strings are the same length do we need to do full string comparison
        int lengthDiff = nameFull.length() - m.nameFull.length();
        if (lengthDiff != 0) {
            return lengthDiff;
        }

        // Then we compare the full name (class.getCanonicalName());

        // A lot faster
        int c = nameFull.compareTo(m.nameFull);
        if (c != 0) {
            return c;
        }

        // Okay same depth and name, is the same extension
        if (m.extensionClass == extensionClass) { // class names are always interned
            return 0;
        }

        // Same canonical name and depth but loaded with two different class loaders.
        // We do not support this
        // Maybe not support extensions with same name independently of weather the depth is equivalent
        throw new IllegalArgumentException("Cannot compare two extensions with the same depth '" + orderingDepth + "' and fullname '" + nameFull
                + "' but loaded by different class loaders. " + "ClassLoader(this) = " + extensionClass.getClassLoader() + ", ClassLoader(other) = "
                + m.extensionClass.getClassLoader());
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> dependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public String fullName() {
        return nameFull;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    public static Class<? extends Extension<?>> extractE(TypeVariableExtractor tve, Class<?> type) {
        // Extract the type of extension from ExtensionMirror<E>
        Type t = tve.extractType(type, InternalExtensionException::new);

        @SuppressWarnings("unchecked")
        Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) t; //

        // Check that we are a proper subclass of
        ClassUtil.checkProperSubclass(Extension.class, extensionClass, InternalExtensionException::new);

        // Check that the mirror is in the same module as the extension itself
        if (extensionClass.getModule() != type.getModule()) {
            throw new InternalExtensionException("The extension support class " + type + " must in the same module (" + extensionClass.getModule() + ") as "
                    + extensionClass + ", but was in '" + type.getModule() + "'");
        }

        ExtensionClassModel.of(extensionClass); // Check that the extension is valid

        return extensionClass;
    }

    /**
     * Creates a new instance of the extension.
     *
     * @param extension
     *            the setup of the extension
     * @return a new extension instance
     */
    Extension<?> newInstance(ExtensionSetup extension) {
        ExtensionHandle<?> handle = new PackedExtensionHandle<>(extension);
        try {
            return (Extension<?>) mhConstructor.invokeExact(handle);
        } catch (Throwable e) {
            throw new InternalExtensionException("An instance of the extension " + nameFull + " could not be created.", e);
        }
    }

    /**
     * Returns the depth of the extension in the extension dependency graph.
     * <p>
     * Extensions without any dependencies always have depth 0. Otherwise the depth of an extension it is the maximum depth
     * of any of its direct dependencies plus 1. This has the nice property, that any dependencies (including transitive
     * dependencies) of an extension will always have a depth that is less than the depth of the extension itself.
     *
     * @return the depth of the extension
     */
    public int orderingDepth() {
        return orderingDepth;
    }

    public ComponentRealm realm() {
        return realm;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nameFull);
        moduleVersion().ifPresent(v -> sb.append("[" + v + "]"));
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> type() {
        return extensionClass;
    }

    /**
     * @param callerClass
     *            the calling class (must a proper subclass of Extension)
     * @return
     */
    public static Builder bootstrap(Class<?> callerClass) {
        return Loader.forBootstrapAccess(callerClass);
    }

    /**
     * Returns an model for the specified extension type.
     *
     * @param extensionType
     *            the extension type to return a model for
     * @return an extension model for the specified extension type
     * @throws InternalExtensionException
     *             if a valid model for the extension could not be created
     */
    public static ExtensionClassModel of(Class<? extends Extension<?>> extensionType) {
        return MODELS.get(extensionType);
    }

    /** A builder of {@link ExtensionModel}. Public to allow bootstrapping from {@link Extension}. */
    // I think we don't need to be public any more
    public static final class Builder {

        /** A set of extension this extension depends on (does not include transitive extensions). */
        private Set<Class<? extends Extension<?>>> dependencies = new HashSet<>();

        /** The depth of the extension relative to other extensions. */
        private int depth;

        /** The extension we are building a model for. */
        private final Class<? extends Extension<?>> extensionClass;

        /** A handle for creating new extension instances. */
        private MethodHandle mhConstructor; // (ExtensionSetup)Extension

        /**
         * Jeg godt vi vil lave det om saa vi faktisk loader extensionen naar man kalder addDependency. Skal lige gennemtaenkes,
         * det er lidt kompliceret classloader
         */
        private Set<Class<? extends Extension<?>>> pendingLoadDependencies = Collections.newSetFromMap(new WeakHashMap<>());

        private Builder(Class<? extends Extension<?>> extensionClass) {
            this.extensionClass = requireNonNull(extensionClass);
        }

        /**
         * Builds and returns an extension model.
         *
         * @return the extension model
         */
        private ExtensionClassModel build(Loader loader) {
            DependsOn depende = extensionClass.getAnnotation(DependsOn.class);
            if (depende != null) {
                dependsOn(false, depende.extensions());
            }
            if (extensionClass != BaseExtension.class) {
                pendingLoadDependencies.add(BaseExtension.class);
            }
            for (Class<? extends Extension<?>> dependencyType : pendingLoadDependencies) {
                ExtensionClassModel model = Loader.load(dependencyType, loader);
                depth = Math.max(depth, model.orderingDepth + 1);
                dependencies.add(dependencyType);
            }

            if (Modifier.isAbstract(extensionClass.getModifiers())) {
                throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot be an abstract class");
            } else if (ClassUtil.isInnerOrLocal(extensionClass)) {
                throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot be an an inner or local class");
            }

            // An extension must provide an empty constructor
            Constructor<?>[] constructors = extensionClass.getDeclaredConstructors();
            if (constructors.length != 1) {
                throw new InternalExtensionException(StringFormatter.format(extensionClass) + " must declare exactly 1 constructor");
            }

            Constructor<?> constructor = constructors[0];
            if (constructor.getParameterCount() != 1 || constructor.getParameterTypes()[0] != ExtensionHandle.class) {
                throw new InternalExtensionException(extensionClass + " must provide a constructor taking ExtensionHandle, but constructor required "
                        + Arrays.toString(constructor.getParameters()));
            }

            // The constructor must be non-public
            if (Modifier.isPublic(constructor.getModifiers())) {
                throw new InternalExtensionException(extensionClass + " cannot declare a public constructor");
            }

            // Create a MethodHandle for the constructor
            try {
                Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
                MethodHandle mh = l.unreflectConstructor(constructor);
                // cast from (ExtensionClass) -> (Extension)
                this.mhConstructor = MethodHandles.explicitCastArguments(mh, MethodType.methodType(Extension.class, ExtensionHandle.class));
            } catch (IllegalAccessException e) {
                throw new InternalExtensionException(extensionClass + " must be open to '" + getClass().getModule().getName() + "'", e);
            }

            return new ExtensionClassModel(this);
        }

        /**
         * Adds the specified dependency to the caller class if valid.
         *
         * @param extensions
         *            the extensions
         * @see Extension#$dependsOn(Class...)
         */
        @SuppressWarnings("unchecked")
        public void dependsOn(boolean optionally, Class<? extends Extension<?>>... extensions) {
            requireNonNull(extensions, "extensions is null");
            for (Class<? extends Extension<?>> dependencyType : extensions) {
                requireNonNull(dependencyType);
                if (dependencyType == BaseExtension.class) {
                    throw new InternalExtensionException(BaseExtension.class.getSimpleName() + " is a mandated dependency, and cannot be declared using @"
                            + DependsOn.class.getSimpleName() + " on extension " + extensionClass.getCanonicalName());
                } else if (extensionClass == dependencyType) {
                    throw new InternalExtensionException("Extension " + extensionClass + " cannot depend on itself");
                } else if (pendingLoadDependencies.contains(dependencyType)) {
                    throw new InternalExtensionException("A dependency on " + dependencyType + " has already been added");
                }
                pendingLoadDependencies.add(dependencyType);
            }
        }

        @SuppressWarnings("unchecked")
        public Optional<Class<? extends Extension<?>>> dependsOnOptionally(String extension) {
            ClassLoader cl = extensionClass.getClassLoader();
            Class<?> c = null;
            try {
                c = Class.forName(extension, true, cl);
            } catch (ClassNotFoundException ignore) {
                return Optional.empty();
            }
            // We check this in models also...
            if (Extension.class == c) {
                throw new InternalExtensionException("The specified string \"" + extension + "\" cannot specify Extension.class as an optional dependency, for "
                        + StringFormatter.format(c));
            } else if (!Extension.class.isAssignableFrom(c)) {
                throw new InternalExtensionException(
                        "The specified string \"" + extension + "\" " + " specified an invalid extension " + StringFormatter.format(c));
            }
            Class<? extends Extension<?>> dependency = (Class<? extends Extension<?>>) c;

            ExtensionClassModel.of(dependency);
            dependsOn(true, dependency);
            return Optional.of(dependency);
        }
    }

    /**
     * An extension loader is responsible for loading an extension and any of its dependencies (including transitive
     * dependencies) that have not already been loaded.
     * <p>
     * We do not currently attempt to load multiple extensions concurrently, but instead use a global lock.
     */
    private static final class Loader {

        /** A map that contains Bootstrap, Builder or Throwable */
        private static final WeakHashMap<Class<? extends Extension<?>>, Object> DATA = new WeakHashMap<>();

        /** A lock used for making sure that we only load one extension (and its dependencies) at a time. */
        private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

        /**
         * A stack used for checking for cyclic dependencies between extension. We do not expect deep stacks (or at least very
         * few of them), so it is okay to check for membership in linear time.
         */
        private final ArrayDeque<Class<? extends Extension<?>>> stack = new ArrayDeque<>();

        private ExtensionClassModel loadLocked(Class<? extends Extension<?>> extensionClass) {
            // Check for cyclic dependencies between extensions
            if (stack.peek() != extensionClass) {
                if (!stack.isEmpty() && stack.contains(extensionClass)) {
                    String st = stack.stream().map(e -> e.getCanonicalName()).collect(Collectors.joining(" -> "));
                    throw new InternalExtensionException(
                            "Cyclic dependencies between extensions encountered: " + st + " -> " + extensionClass.getCanonicalName());
                }
            }
            stack.push(extensionClass);

            ExtensionClassModel model;
            try {
                // Ensure that the class initializer of the extension has been run before we progress
                try {
                    ExtensionClassModel.class.getModule().addReads(extensionClass.getModule());
                    Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
                    l.ensureInitialized(extensionClass);
                } catch (IllegalAccessException e) {
                    // TODO this is likely the first place we check the extension is readable to Packed
                    // Better error message..
                    // Maybe we have other stuff that we need to check here...
                    // We need to be open.. In order to create the extension...
                    // So probably no point in just checking for Readable...
                    throw new InternalExtensionException("Extension is not readable for Packed", e);
                }

                // Get any bootstrap data that was create as part of the class initialization

                Object object = DATA.get(extensionClass);
                if (object instanceof ExtensionClassModel m) {
                    // Typically from call to l.ensureInitialized(extensionClass);
                    // where the class initializer tries to create
                    // FooExtension
                    // static final ExtensionDescriptor.of(FooExtension.class;
                    return m;
                }
                @Nullable
                Builder b = (Builder) object;
                if (b == null) {
                    b = new Builder(extensionClass);
                }

                model = b.build(this);
            } catch (Throwable t) {
                // We failed to either load this extension, or one of the extensions
                // dependencies failed to load.

                // We save the throwable in case we try to use the extension from somewhere else
                DATA.put(extensionClass, t);
                throw t;
            } finally {
                stack.pop();
            }

            // The dependency and all of its dependencies have been successfully loaded.
            // We store the model in DATA so that ExtensionModel#MODELS can access it when
            // needed. In case the model for a transitive dependency is being calculated
            // This might not be immediately.
            DATA.put(extensionClass, model);

            return model;
        }

        private static Builder forBootstrapAccess(Class<?> callerClass) {
            // Think
            if (!Extension.class.isAssignableFrom(callerClass) || callerClass == Extension.class) {
                throw new InternalExtensionException("This method can only be called directly from a subclass of Extension, caller was " + callerClass);
            }
            @SuppressWarnings("unchecked")
            Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) callerClass;
            GLOBAL_LOCK.lock();
            try {
                Object m = DATA.get(callerClass);
                if (m == null) {
                    Builder b = new Builder(extensionClass);
                    DATA.put(extensionClass, b);
                    return b;
                } else if (m instanceof Builder b) {
                    return b;
                } else {
                    throw new InternalExtensionException(
                            "This method must be called from within the class initializer of an extension, extension = " + callerClass);
                }
            } finally {
                GLOBAL_LOCK.unlock();
            }
        }

        private static ExtensionClassModel load(Class<? extends Extension<?>> extensionClass, @Nullable Loader loader) {
            // The creation of an Extension model is a bit complex because we need to
            // create the models of dependencies recursively while also checking for cyclic dependencies

            GLOBAL_LOCK.lock();
            try {
                // Check if we have attempted to load (possible unsuccessful) the extension
                Object data = DATA.get(extensionClass);
                if (data instanceof ExtensionClassModel model) {
                    return model;
                } else if (data instanceof Throwable t) {
                    throw new InternalExtensionException("Extension " + extensionClass + " failed to load previously", t);
                }

                // Create a new loader if we are not already part of one
                return (loader == null ? new Loader() : loader).loadLocked(extensionClass);
            } finally {
                GLOBAL_LOCK.unlock();
            }
        }
    }

    static class Wrapper {

        @Nullable
        ExtensionSetup setup;

        private Wrapper(ExtensionSetup setup) {
            this.setup = setup;
        }
    }
}
