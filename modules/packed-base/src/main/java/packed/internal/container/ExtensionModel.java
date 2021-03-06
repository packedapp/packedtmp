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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import app.packed.base.Nullable;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import packed.internal.attribute.PackedAttributeModel;
import packed.internal.invoke.Infuser;
import packed.internal.invoke.OpenClass;
import packed.internal.util.ClassUtil;
import packed.internal.util.StringFormatter;

/**
 * A model of an {@link Extension}. Exposed to end-users as {@link ExtensionDescriptor}.
 * 
 * @implNote This could have been a record, but there are so many fields that that we get a better overview as a plain
 *           class.
 */
public final class ExtensionModel implements ExtensionDescriptor {

    /** A cache of all encountered extension models. */
    private static final ClassValue<ExtensionModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ExtensionModel computeValue(Class<?> extensionClass) {
            return Loader.load(ClassUtil.checkProperSubclass(Extension.class, extensionClass), null);
        }
    };

    /** A model of any attributes defined on the extension class. */
    @Nullable
    final PackedAttributeModel attributes;

    /** The direct dependencies of the extension. */
    private final ExtensionDependencySet dependencies;

    /** The {@link ExtensionDescriptor#depth() depth} of this extension. */
    private final int depth;

    /** The extension we model. */
    private final Class<? extends Extension> extensionClass;

    /** Whether or not is is only any immediately parent that will be linked. */
    final boolean extensionLinkedDirectChildrenOnly;

    /** A method handle for creating instances of extensionClass. */
    private final MethodHandle mhConstructor; // (ExtensionSetup)Extension

    /** The default component name of the extension. */
    public final String nameComponent;

    /** The (canonical) full name of the extension. Used to deterministically sort extensions. */
    private final String nameFull;

    /** The (simple) name of the extension as returned by {@link Class#getSimpleName()}. */
    private final String nameSimple;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder of the model
     */
    private ExtensionModel(Builder builder) {
        this.extensionClass = builder.extensionClass;
        this.mhConstructor = builder.mhConstructor;
        this.depth = builder.depth;
        this.dependencies = ExtensionDependencySet.of(builder.dependencies);

        // Cache some frequently used strings.
        this.nameFull = extensionClass.getCanonicalName();
        this.nameSimple = extensionClass.getSimpleName();
        this.nameComponent = "." + nameSimple;

        this.extensionLinkedDirectChildrenOnly = builder.connectParentOnly;
        this.attributes = builder.pam;
    }

    /**
     * Returns any value of nest annotation.
     * 
     * @param eType
     *            the type look for an ExtensionMember annotation on
     * @return an extension the specified type is a member of
     * @throws InternalExtensionException
     *             if an annotation is present and the specified is not in the same module as the extension specified in
     *             next
     */
    @Nullable
    public Class<? extends Extension> checkSameModule(Class<? extends Extension> eType) {
        if (extensionClass.getModule() != eType.getModule()) {
            throw new InternalExtensionException("The extension " + eType + " and type " + extensionClass + " must be defined in the same module, was "
                    + eType.getModule() + " and " + extensionClass.getModule());
        }
        of(eType); // Make sure a valid model for the extension has been created
        return eType;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionDescriptor descriptor) {
        ExtensionModel m = (ExtensionModel) descriptor;
        // if (m.getClassLoader()!=this.getClassLoader())
        // Start comparing by name

        // First we compare the depth of each extension
        int d = depth - m.depth;
        if (d != 0) {
            return d;
        }

        // Then we compare the full name (class.getCanonicalName());
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
        throw new IllegalArgumentException(
                "Cannot compare two extensions with the same depth '" + depth + "' and fullname '" + nameFull + "' but loaded by different class loaders. "
                        + "ClassLoader(this) = " + extensionClass.getClassLoader() + ", ClassLoader(other) = " + m.extensionClass.getClassLoader());
    }

    /** {@inheritDoc} */
    @Override
    public ExtensionDependencySet dependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return depth;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> type() {
        return extensionClass;
    }

    /** {@inheritDoc} */
    @Override
    public String fullName() {
        return nameFull;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return nameSimple;
    }

    /**
     * Creates a new extension instance.
     * 
     * @param extension
     *            the setup of the extension
     * @return a new extension instance
     */
    Extension newInstance(ExtensionSetup extension) {
        try {
            return (Extension) mhConstructor.invokeExact(extension);
        } catch (Throwable e) {
            throw new InternalExtensionException("An instance of the extension " + nameFull + " could not be created.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nameFull);
        moduleVersion().ifPresent(v -> sb.append("[" + v + "]"));
        return sb.toString();
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
    public static ExtensionModel of(Class<? extends Extension> extensionType) {
        return MODELS.get(extensionType);
    }

    /** A builder of {@link ExtensionModel}. Public to allow bootstrapping from {@link Extension}. */
    public static final class Builder {

        /** Whether or not we only connect to parent or all ancestors. */
        private boolean connectParentOnly;

        /** A set of extension this extension depends on (does not include transitive extensions). */
        private Set<Class<? extends Extension>> dependencies = new HashSet<>();

        /** The depth of the extension relative to other extensions. */
        private int depth;

        /** The extension we are building a model for. */
        private final Class<? extends Extension> extensionClass;

        /** A handle for creating new extension instances. */
        private MethodHandle mhConstructor; // (ExtensionSetup)Extension

        /** A model of all methods that provide attributes. */
        @Nullable
        private PackedAttributeModel pam;

        /**
         * Jeg godt vi vil lave det om saa vi faktisk loader extensionen naar man kalder addDependency. Skal lige gennemtaenkes,
         * det er lidt kompliceret classloader
         */
        private Set<Class<? extends Extension>> pendingLoadDependencies = Collections.newSetFromMap(new WeakHashMap<>());

        private Builder(Class<? extends Extension> extensionClass) {
            this.extensionClass = requireNonNull(extensionClass);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        private ExtensionModel build(Loader loader) {
            for (Class<? extends Extension> dependencyType : pendingLoadDependencies) {
                ExtensionModel model = Loader.load(dependencyType, loader);
                depth = Math.max(depth, model.depth + 1);
                dependencies.add(dependencyType);
            }

            Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), extensionClass, ExtensionSetup.class);
            builder.provide(ExtensionContext.class).adaptArgument(0);
            // If it is only ServiceExtension that ends up using it lets just dump it and have a single cast
            builder.provideHidden(ExtensionSetup.class).adaptArgument(0);
            // Den den skal nok vaere lidt andet end hidden. Kunne kunne klare Optional osv
            // MethodHandle mh =
            // ExtensionSetup.MH_INJECT_PARENT.asType(ExtensionSetup.MH_INJECT_PARENT.type().changeReturnType(extensionClass));
            // builder.provideHidden(extensionClass).invokeExact(mh, 0);

            // Find a method handle for the extension's constructor
            this.mhConstructor = builder.findConstructor(Extension.class, m -> new InternalExtensionException(m));

            // So far we scan for constructors
            OpenClass oc = OpenClass.of(MethodHandles.lookup(), extensionClass);

            // Okay maybe we need to scan for contracts...
            this.pam = PackedAttributeModel.analyse(oc);

            return new ExtensionModel(this);
        }

        /**
         * The extension will only connect is two containers are in a parent-child relationship. The default behavior is to
         * connect with any ancestor.
         * 
         * @see Extension#$connectParentOnly
         */
        public void connectParentOnly() {
            connectParentOnly = true;
        }

        /**
         * Adds the specified dependency to the caller class if valid.
         * 
         * @param extensions
         *            the extensions
         * @see Extension#$dependsOn(Class...)
         */
        @SuppressWarnings("unchecked")
        public void dependsOn(Class<? extends Extension>... extensions) {
            requireNonNull(extensions, "extensions is null");
            for (Class<? extends Extension> dependencyType : extensions) {
                requireNonNull(dependencyType);
                if (extensionClass == dependencyType) {
                    throw new InternalExtensionException("Extension " + extensionClass + " cannot depend on itself");
                } else if (this.pendingLoadDependencies.contains(dependencyType)) {
                    throw new InternalExtensionException("A dependency on " + dependencyType + " has already been added");
                }
                pendingLoadDependencies.add(dependencyType);
            }
        }

        @SuppressWarnings("unchecked")
        public Optional<Class<? extends Extension>> dependsOnOptionally(String extension) {
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
            Class<? extends Extension> dependency = (Class<? extends Extension>) c;

            ExtensionModel.of(dependency);
            dependsOn(dependency);
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
        private static final WeakHashMap<Class<? extends Extension>, Object> DATA = new WeakHashMap<>();

        /** A lock used for making sure that we only load one extension (and its dependencies) at a time. */
        private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

        /**
         * A stack used for checking for cyclic dependencies between extension. We do not expect deep stacks (or at least very
         * few of them), so it is okay to check for membership in linear time.
         */
        private final ArrayDeque<Class<? extends Extension>> stack = new ArrayDeque<>();

        private ExtensionModel loadLocked(Class<? extends Extension> extensionClass) {
            // Check for cyclic dependencies between extensions
            if (stack.peek() != extensionClass) {
                if (!stack.isEmpty() && stack.contains(extensionClass)) {
                    String st = stack.stream().map(e -> e.getCanonicalName()).collect(Collectors.joining(" -> "));
                    throw new InternalExtensionException(
                            "Cyclic dependencies between extensions encountered: " + st + " -> " + extensionClass.getCanonicalName());
                }
            }
            stack.push(extensionClass);

            ExtensionModel model;
            try {
                // Ensure that the class initializer of the extension has been run before we progress
                try {
                    ExtensionModel.class.getModule().addReads(extensionClass.getModule());
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
                @Nullable
                Builder b = (Builder) DATA.get(extensionClass);
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
            Class<? extends Extension> extensionClass = (Class<? extends Extension>) callerClass;
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

        private static ExtensionModel load(Class<? extends Extension> extensionClass, @Nullable Loader loader) {
            // The creation of an Extension model is a bit complex because we need to
            // create the models of dependencies recursively while also checking for cyclic dependencies

            GLOBAL_LOCK.lock();
            try {
                // Check if we have attempted to load (possible unsuccessful) the extension
                Object data = DATA.get(extensionClass);
                if (data instanceof ExtensionModel model) {
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
}
