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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import app.packed.attribute.ExposeAttribute;
import app.packed.base.Nullable;
import app.packed.component.UseWirelet;
import app.packed.container.ConnectExtensions;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionDescriptor;
import app.packed.container.InternalExtensionException;
import packed.internal.base.attribute.PackedAttributeModel;
import packed.internal.classscan.ClassMemberAccessor;
import packed.internal.classscan.MethodHandleBuilder;
import packed.internal.inject.FindInjectableConstructor;
import packed.internal.util.StringFormatter;

/** A model of an {@link Extension}. Exposed to end-users as {@link ExtensionDescriptor}. */
public final class ExtensionModel implements ExtensionDescriptor {

    /** A cache of extension models. */
    private static final ClassValue<ExtensionModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionModel computeValue(Class<?> extensionClass) {
            // We have a number of checks here to check that the extension class is actually valid.
            // We do them here, because it is faster then checking the extension type every time it is requested.
            if (extensionClass == Extension.class) {
                throw new IllegalArgumentException(Extension.class.getSimpleName() + ".class is not a valid argument to this method.");
            } else if (!Extension.class.isAssignableFrom(extensionClass)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(extensionClass) + "' must extend '" + StringFormatter.format(Extension.class) + "'");
            }

            // The creation of an Extension model is a bit complex because we need to
            // create the models of dependencies recursively while also checking for cyclic dependencies
            return Loader.load((Class<? extends Extension>) extensionClass, null);
        }
    };

    /** A model of any attributes defined on the extension type. */
    private final PackedAttributeModel attributes;// Nullable??

    /** The {@link ExtensionDescriptor#depth() depth} of this extension. */
    private final int depth;

    /** The direct dependencies of the extension. */
    private final ExtensionDependencySet directDependencies;

    /** The extension we model. */
    private final Class<? extends Extension> extensionClass;

    /** Whether or not is is only any immediately parent that will be linked. */
    final boolean extensionLinkedDirectChildrenOnly;

    /** A method handle for creating extension instances. */
    private final MethodHandle mhConstructor; // (ExtensionSetup)Extension

    /** A method handle to an optional method annotated with {@link ConnectExtensions} on the extension. */
    @Nullable
    final MethodHandle mhExtensionLinked; // will have an extensionLinkedToAncestorService in the future

    /** The default component name of the extension. */
    public final String nameComponent;

    /** The canonical name of the extension. Used to deterministically sort extensions. */
    private final String nameFull;

    /** The simple name of the extension as returned by {@link Class#getSimpleName()}. */
    private final String nameSimple;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionModel(Builder builder) {
        this.extensionClass = builder.extensionClass;
        this.mhConstructor = builder.mhConstructor;
        this.depth = builder.depth;
        // All direct dependencies of this extension
        this.directDependencies = ExtensionDependencySet.of(builder.dependencies);

        // Cache some frequently used strings.
        this.nameFull = extensionClass.getCanonicalName();
        this.nameSimple = extensionClass.getSimpleName();
        this.nameComponent = "." + nameSimple;

        this.mhExtensionLinked = builder.li;
        this.extensionLinkedDirectChildrenOnly = builder.callbackOnlyDirectChildren;
        this.attributes = builder.pam;
    }

    /**
     * Returns a model of any attributes the extension defines.
     * 
     * @return a model of any attributes the extension defines
     * @see ExposeAttribute
     */
    public PackedAttributeModel attributes() {
        return attributes;
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
        if (m.extensionClass == extensionClass) {
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
        return directDependencies;
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return depth;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> extensionClass() {
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

    public static void bootstrapAddDependency(Class<?> callerClass, Class<? extends Extension> dependencyType) {
        Loader.forAccess(callerClass).addStaticDependency(dependencyType);
    }

    /**
     * Returns an model for the specified extension class.
     * 
     * @param extensionClass
     *            the extension class to return a model for
     * @return an extension model for the specified extension class
     * @throws InternalExtensionException
     *             if a valid model could not be created
     */
    public static ExtensionModel of(Class<? extends Extension> extensionClass) {
        return MODELS.get(extensionClass);
    }

    /**
     * A bootstrap class for the extension model. Is used in order to register various things from the class initializer of
     * an Extension.
     * <p>
     * // Hmm if static initalizer fails this stays around // This is also a problem if we have more complex objects... // I
     * don't know how much of a problem it is... // If the static initializer fails it does so with an error...
     */
    // Maaske kan vi dropper den her bootstrap, og bare smide direkte ind i builder...
    // Det er maaske fedt nok at instantiere sine dependencies fra class initializer...
    private static final class Bootstrap {

        /** A set of extension this extension depends on. */

        /** All dependencies of the extension */

        // Jeg godt vi vil lave det om saa vi faktisk loader extensionen naar man kalder addDependency
        private Set<Class<? extends Extension>> dependencies = Collections.newSetFromMap(new WeakHashMap<>());

        final Class<? extends Extension> extensionClass;

        Bootstrap(Class<? extends Extension> extensionClass) {
            this.extensionClass = requireNonNull(extensionClass);
        }

        void addStaticDependency(Class<? extends Extension> dependencyType) {
            if (extensionClass == dependencyType) {
                throw new InternalExtensionException("Extension " + extensionClass + " cannot depend on itself");
            }
            dependencies.add(dependencyType);
        }
    }

    /** A builder of {@link ExtensionModel}. */
    private static final class Builder {

        // Whether or not it is only children... Or all ancestors
        private boolean callbackOnlyDirectChildren;

        /** A set of extension this extension depends on (does not include transitive extensions). */
        private Set<Class<? extends Extension>> dependencies = new HashSet<>();

        /** The depth of the extension relative to other extensions. */
        private int depth;

        /** The extension we are building a model for. */
        private final Class<? extends Extension> extensionClass;

        private MethodHandle li;

        private Method linked;

        /** The extension loader used to load the extension. */
        private final Loader loader;

        /** A handle for creating new extension instances. */
        private MethodHandle mhConstructor; // (ExtensionSetup)Extension

        /** A model of all methods that provide attributes. */
        private PackedAttributeModel pam;

        /**
         * Creates a new builder.
         * 
         * @param extensionClass
         *            the type of extension we are building a model for
         */
        Builder(Class<? extends Extension> extensionClass, Loader loader) {
            this.extensionClass = requireNonNull(extensionClass);
            this.loader = requireNonNull(loader);
        }

        private void addExtensionContextElements(MethodHandleBuilder builder, int index) {
            builder.addKey(ExtensionConfiguration.class, index);
            builder.addAnnoClassMapper(UseWirelet.class, ExtensionSetup.MH_FIND_WIRELET, index);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        ExtensionModel build(@Nullable Bootstrap bootstrap) {
            if (bootstrap != null) {
                for (Class<? extends Extension> dependencyType : bootstrap.dependencies) {
                    ExtensionModel model = Loader.load(dependencyType, loader);
                    depth = Math.max(depth, model.depth + 1);
                    dependencies.add(dependencyType);
                }
            }

            ClassMemberAccessor cp = scanClass();
            this.pam = PackedAttributeModel.analyse(cp);

            if (linked != null) {
                // ancestor extension, descendant extension context, descendant extension
                MethodHandleBuilder iss = MethodHandleBuilder.of(void.class, Extension.class, ExtensionSetup.class, Extension.class);

                // From the child's extension context
                addExtensionContextElements(iss, 1);

                // The child's extension instance
                iss.addKey(extensionClass, 2); // should perform an implicit cast

                li = iss.build(cp, linked);
            }
            return new ExtensionModel(this);
        }

        protected ClassMemberAccessor scanClass() {
            MethodHandleBuilder spec = MethodHandleBuilder.of(Extension.class, ExtensionSetup.class);
            addExtensionContextElements(spec, 0);
            ClassMemberAccessor cp = ClassMemberAccessor.of(MethodHandles.lookup(), extensionClass);

            // Find constructor and create method handle
            Constructor<?> constructor = FindInjectableConstructor.findConstructor(extensionClass, s -> new InternalExtensionException(s));
            if (Modifier.isPublic(constructor.getModifiers()) && Modifier.isPublic(extensionClass.getModifiers())) {
                throw new InternalExtensionException(
                        "Extensions that are public classes, must have a non-public constructor. As end-users should not be able to instantiate them explicitly, extension = "
                                + extensionClass);
            }
            this.mhConstructor = cp.resolve(spec, constructor);

            cp.findMethods(m -> {
                ConnectExtensions ce = m.getAnnotation(ConnectExtensions.class);
                if (ce != null) {
                    if (linked != null) {
                        throw new IllegalStateException(
                                "Multiple methods annotated with " + ConnectExtensions.class + " on " + m.getDeclaringClass() + ", only 1 allowed.");
                    }
                    linked = m;
                    callbackOnlyDirectChildren = ce.onlyDirectLink();
                }
            });

            return cp;
        }
    }

    /**
     * An (extension) loader is responsible for loading an extension and any of its dependencies (including transitive
     * dependencies) that have not already been loaded.
     * <p>
     * We do not currently attempt to load extension concurrently, but instead use a single global lock.
     */
    private static final class Loader {

        /** A map that contains */
        // Bootstrap, Builder or Throwable
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
                    Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
                    l.ensureInitialized(extensionClass);
                } catch (IllegalAccessException e) {
                    // TODO this is likely the first place we check the extension is readable to Packed
                    // Better error message
                    throw new InternalExtensionException("Extension is not readable for Packed", e);
                }

                // Get any bootstrap data that was create as part of the class initialization
                @Nullable
                Bootstrap b = (Bootstrap) DATA.get(extensionClass);

                ExtensionModel.Builder builder = new ExtensionModel.Builder(extensionClass, this);

                model = builder.build(b);
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

        static Bootstrap forAccess(Class<?> callerClass) {
            if (!Extension.class.isAssignableFrom(callerClass)) {
                throw new InternalExtensionException("This method can only be called directly from a subclass of Extension, caller was " + callerClass);
            }
            @SuppressWarnings("unchecked")
            Class<? extends Extension> extensionClass = (Class<? extends Extension>) callerClass;
            GLOBAL_LOCK.lock();
            try {
                Object m = DATA.get(callerClass);
                if (m == null) {
                    Bootstrap b = new Bootstrap(extensionClass);
                    DATA.put(extensionClass, b);
                    return b;
                } else if (m instanceof Bootstrap b) {
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

// We probably want to add int id at some point

///** A unique id of the extension. */
//final int id; // We don't currently use it... Ideen er at kunne indexere en extension istedet for en hashtable

// Code for optional extensions

///** A class value that contains optional dependencies of an extension. */
//private static ClassValue<?> OPTIONALS = new ClassValue<>() {
//
//  @Override
//  protected Object computeValue(Class<?> type) {
//      try {
//          return computeValue0(type);
//      } catch (Throwable t) {
//          return t;
//      }
//  }
//
//  @SuppressWarnings("unchecked")
//  private List<Class<? extends Extension>> computeValue0(Class<?> type) {
//      String[] dependencies = type.getAnnotation(UsesExtensions.class).optionalDependencies();
//
//      ArrayList<Class<? extends Extension>> result = new ArrayList<>();
//      ClassLoader cl = type.getClassLoader(); // PrividligeAction???
//      for (String s : dependencies) {
//          Class<?> c = null;
//          try {
//              c = Class.forName(s, false, cl);//
//          } catch (ClassNotFoundException ignore) {}
//
//          if (c != null) {
//              // We check this in models also...
//              if (Extension.class == c) {
//                  throw new InternalExtensionException("@" + UsesExtensions.class.getSimpleName() + " " + StringFormatter.format(type)
//                          + " cannot specify Extension.class as an optional dependency, for " + StringFormatter.format(c));
//              } else if (!Extension.class.isAssignableFrom(c)) {
//                  throw new InternalExtensionException("@" + UsesExtensions.class.getSimpleName() + " " + StringFormatter.format(type)
//                          + " specified an invalid extension " + StringFormatter.format(c));
//              }
//              result.add((Class<? extends Extension>) c);
//          }
//      }
//      return result;
//  }
//};

///** A class value that contains optional dependencies of an extension. */
//private static ClassValue<?> OPTIONALS = new ClassValue<>() {
//
//  @Override
//  protected Object computeValue(Class<?> type) {
//      try {
//          return computeValue0(type);
//      } catch (Throwable t) {
//          return t;
//      }
//  }
//
//  @SuppressWarnings("unchecked")
//  private List<Class<? extends Extension>> computeValue0(Class<?> type) {
//      String[] dependencies = type.getAnnotation(UsesExtensions.class).optionalDependencies();
//
//      ArrayList<Class<? extends Extension>> result = new ArrayList<>();
//      ClassLoader cl = type.getClassLoader(); // PrividligeAction???
//      for (String s : dependencies) {
//          Class<?> c = null;
//          try {
//              c = Class.forName(s, false, cl);//
//          } catch (ClassNotFoundException ignore) {}
//
//          if (c != null) {
//              // We check this in models also...
//              if (Extension.class == c) {
//                  throw new InternalExtensionException("@" + UsesExtensions.class.getSimpleName() + " " + StringFormatter.format(type)
//                          + " cannot specify Extension.class as an optional dependency, for " + StringFormatter.format(c));
//              } else if (!Extension.class.isAssignableFrom(c)) {
//                  throw new InternalExtensionException("@" + UsesExtensions.class.getSimpleName() + " " + StringFormatter.format(type)
//                          + " specified an invalid extension " + StringFormatter.format(c));
//              }
//              result.add((Class<? extends Extension>) c);
//          }
//      }
//      return result;
//  }
//};