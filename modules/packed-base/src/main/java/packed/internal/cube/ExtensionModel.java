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
package packed.internal.cube;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import app.packed.base.Nullable;
import app.packed.component.WireletConsume;
import app.packed.cube.ConnectExtensions;
import app.packed.cube.Extension;
import app.packed.cube.Extension.Subtension;
import app.packed.cube.ExtensionConfiguration;
import app.packed.cube.ExtensionDescriptor;
import app.packed.cube.ExtensionMember;
import app.packed.cube.ExtensionSetup;
import app.packed.cube.InternalExtensionException;
import packed.internal.base.attribute.ProvidableAttributeModel;
import packed.internal.classscan.MethodHandleBuilder;
import packed.internal.classscan.OpenClass;
import packed.internal.inject.FindInjectableConstructor;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/** A model of an Extension. */
public final class ExtensionModel implements ExtensionDescriptor {

    /** A cache of extension models. */
    private static final ClassValue<ExtensionModel> EXTENSIONS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionModel computeValue(Class<?> type) {
            // We have a number of checks here that the requested extension type is actually valid.
            // We do them here, because it is faster then checking the extension type every time it is requested.
            if (type == Extension.class) {
                throw new IllegalArgumentException(Extension.class.getSimpleName() + ".class is not a valid argument to this method.");
            } else if (!Extension.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(type) + "' must extend '" + StringFormatter.format(Extension.class) + "'");
            }

            if (type.isAnnotationPresent(ExtensionMember.class)) {
                throw new IllegalArgumentException("An extension is trivially member of itself, so cannot use @" + ExtensionMember.class.getSimpleName()
                        + " annotation, for  '" + StringFormatter.format(type));
            }
            return Loader.load((Class<? extends Extension>) type, null);
        }
    };

    /** The direct dependencies of the extension. */
    private final PackedOrderedExtensionSet dependencies;

    /**
     * The depth of this extension in a global . Defined as 0 if no dependencies otherwise max(all dependencies depth) + 1.
     */
    // Depth is the length of the path to its BaseExtension
    // 0 for BaseExtension otherwise the length of the longest path to BaseExtension
    // -> Dependencies of a given extension always have a depth that is less than the given extension.
    // Det er jo ikke et trae... Men en graph. Giver depth mening?
    // Man kan argumentere med at man laver en masse hylder, hvor de enkelte extensions saa er.
    private final int depth;

    /** Whether or not is is only any immediately parent that will be linked. */
    final boolean extensionLinkedDirectChildrenOnly;

    /** A unique id of the extension. */
    final int id; // We don't currently use it...

    /** A method handle for creating a new extension instance. type = (ExtensionBuild)Extension. */
    private final MethodHandle mhConstructor;

    /** A method handle to an optional method annotated with {@link ConnectExtensions} on the extension. */
    @Nullable
    final MethodHandle mhExtensionLinked; // will have an extensionLinkedToAncestorService in the future

    /** The default component name of the extension. */
    public final String nameComponent;

    /** The canonical name of the extension. Used when needing to deterministically sort extensions. */
    private final String nameFull;

    /** The simple name of the extension, as returned by {@link Class#getSimpleName()}. */
    private final String nameSimple;

    /** A model of any attributes defined on the extension type. */
    private final ProvidableAttributeModel pam;// Nullable??

    /** The type of extension this instance models. */
    private final Class<? extends Extension> type;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionModel(Builder builder) {
        this.type = builder.extensionType;
        this.mhConstructor = builder.mhConstructor;
        this.id = builder.id;
        this.depth = builder.depth;
        // All direct dependencies of this extension
        this.dependencies = PackedOrderedExtensionSet.of(builder.dependencies);

        // Cache some frequently used strings.
        this.nameFull = type.getCanonicalName();
        this.nameSimple = type.getSimpleName();
        this.nameComponent = "." + nameSimple;

        this.mhExtensionLinked = builder.li;
        this.extensionLinkedDirectChildrenOnly = builder.callbackOnlyDirectChildren;
        this.pam = builder.pam;
    }

    /**
     * Returns a model of any attributes the extension defines.
     * 
     * @return a model of any attributes the extension defines
     */
    public ProvidableAttributeModel attributes() {
        return pam;
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
        if (m.type == type) {
            return 0;
        }

        // Same canonical name and depth but loaded with two different class loaders.
        // We do not support this
        throw new IllegalArgumentException(
                "Cannot compare two extensions with the same depth '" + depth + "' and fullname '" + nameFull + "' but loaded by different class loaders. "
                        + "ClassLoader(this) = " + type.getClassLoader() + ", ClassLoader(other) = " + m.type.getClassLoader());
    }

    boolean isDirectDependency(Class<? extends Extension> extensionType) {
        return dependencies.contains(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> dependencies() {
        return Set.copyOf(dependencies.extensions);
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return depth;
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
     * Creates a new instance of the extension.
     * 
     * @param context
     *            the extension context that can be constructor injected into the extension
     * @return a new instance of the extension
     */
    Extension newInstance(ExtensionBuild context) {
        try {
            return (Extension) mhConstructor.invokeExact(context);
        } catch (Throwable e) {
            throw new InternalExtensionException("Extension (" + nameSimple + ")  could not be created", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Subtension>> subtensionType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> type() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> unresolvedDependencies() {
        return Set.of();
    }

    /**
     * Returns any value of {@link ExtensionMember} annotation.
     * 
     * @param type
     *            the type look for an ExtensionMember annotation on
     * @return an extension the specified type is a member of
     * @throws InternalExtensionException
     *             if an annotation is present and the specified is not in the same module as the extension specified in
     *             {@link ExtensionMember#value()}
     */
    @Nullable
    public static Class<? extends Extension> getExtensionMemberOf(Class<?> type) {
        ExtensionMember ue = type.getAnnotation(ExtensionMember.class);
        if (ue != null) {
            Class<? extends Extension> eType = ue.value();
            if (type.getModule() != eType.getModule()) {
                throw new InternalExtensionException("The extension " + eType + " and type " + type + " must be defined in the same module, was "
                        + eType.getModule() + " and " + type.getModule());
            }
            of(eType); // Make sure a valid model for the extension has been created
            return eType;
        }
        return null;
    }

    /**
     * Returns an extension model for the specified extension type.
     * 
     * @param extensionType
     *            the type of extension to return a model for
     * @return an extension model for the specified extension type
     * @throws InternalExtensionException
     *             if a valid model could not be created
     */
    public static ExtensionModel of(Class<? extends Extension> extensionType) {
        return EXTENSIONS.get(extensionType);
    }

    /** A builder of {@link ExtensionModel}. */
    private static final class Builder {

        /** A class value that contains optional dependencies of an extension. */
        private static ClassValue<?> OPTIONALS = new ClassValue<>() {

            @Override
            protected Object computeValue(Class<?> type) {
                try {
                    return computeValue0(type);
                } catch (Throwable t) {
                    return t;
                }
            }

            @SuppressWarnings("unchecked")
            private List<Class<? extends Extension>> computeValue0(Class<?> type) {
                String[] dependencies = type.getAnnotation(ExtensionSetup.class).optionalDependencies();

                ArrayList<Class<? extends Extension>> result = new ArrayList<>();
                ClassLoader cl = type.getClassLoader(); // PrividligeAction???
                for (String s : dependencies) {
                    Class<?> c = null;
                    try {
                        c = Class.forName(s, false, cl);
                    } catch (ClassNotFoundException ignore) {}

                    if (c != null) {
                        // We check this in models also...
                        if (Extension.class == c) {
                            throw new InternalExtensionException("@" + ExtensionSetup.class.getSimpleName() + " " + StringFormatter.format(type)
                                    + " cannot specify Extension.class as an optional dependency, for " + StringFormatter.format(c));
                        } else if (!Extension.class.isAssignableFrom(c)) {
                            throw new InternalExtensionException("@" + ExtensionSetup.class.getSimpleName() + " " + StringFormatter.format(type)
                                    + " specified an invalid extension " + StringFormatter.format(c));
                        }
                        result.add((Class<? extends Extension>) c);
                    }
                }
                return result;
            }
        };

        // Whether or not it is only children... Or all ancestors
        private boolean callbackOnlyDirectChildren;

        /** A set of extension this extension depends on. */
        private Set<Class<? extends Extension>> dependencies = new HashSet<>();

        /** The depth of the extension relative to other extensions. */
        private int depth;

        private final Class<? extends Extension> extensionType;

        private final int id;

        private MethodHandle li;

        private Method linked;

        /** The loader used to load the extension. */
        private final Loader loader;

        /** A method handle used to create new extension instances. */
        private MethodHandle mhConstructor;

        private ProvidableAttributeModel pam;

        /**
         * Creates a new builder.
         * 
         * @param extensionType
         *            the type of extension we are building a model for
         */
        Builder(Class<? extends Extension> extensionType, Loader loader, int id) {
            this.extensionType = requireNonNull(extensionType);
            this.loader = requireNonNull(loader);
            this.id = id;
        }

        private void addDependency(Class<? extends Extension> dependencyType) {
            if (dependencyType == extensionType) {
                throw new InternalExtensionException("Extension " + extensionType + " cannot depend on itself via " + ExtensionSetup.class);
            }
            ExtensionModel model = Loader.load(dependencyType, loader);
            depth = Math.max(depth, model.depth + 1);
            dependencies.add(dependencyType);
        }

        private void addExtensionContextElements(MethodHandleBuilder builder, int index) {
            builder.addKey(ExtensionConfiguration.class, index);
            builder.addAnnoClassMapper(WireletConsume.class, ExtensionBuild.MH_FIND_WIRELET, index);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        @SuppressWarnings("unchecked")
        ExtensionModel build() {
            // See if the extension is annotated with @ExtensionSidecar
            ExtensionSetup em = extensionType.getAnnotation(ExtensionSetup.class);
            if (em != null) {
                for (Class<? extends Extension> dependencyType : em.dependencies()) {
                    addDependency(dependencyType);
                }
                if (em.optionalDependencies().length > 0) {
                    Object result = OPTIONALS.get(extensionType);
                    if (!(result instanceof List)) {
                        return ThrowableUtil.throwReturn((Throwable) result);
                    }
                    for (Class<? extends Extension> dependencyType : (List<Class<? extends Extension>>) result) {
                        addDependency(dependencyType);
                    }
                }
            }

            OpenClass cp = scanClass();
            this.pam = ProvidableAttributeModel.analyse(cp);

            if (linked != null) {
                // ancestor extension, descendant extension context, descendant extension
                MethodHandleBuilder iss = MethodHandleBuilder.of(void.class, Extension.class, ExtensionBuild.class, Extension.class);

                // From the child's extension context
                addExtensionContextElements(iss, 1);

                // The child's extension instance
                iss.addKey(extensionType, 2); // should perform an implicit cast

                li = iss.build(cp, linked);
            }
            return new ExtensionModel(this);
        }

        protected OpenClass scanClass() {
            MethodHandleBuilder spec = MethodHandleBuilder.of(Extension.class, ExtensionBuild.class);
            addExtensionContextElements(spec, 0);
            OpenClass cp = new OpenClass(MethodHandles.lookup(), extensionType, true);

            // Find constructor and create method handle
            Constructor<?> constructor = FindInjectableConstructor.findConstructor(extensionType, s -> new InternalExtensionException(s));
            if (Modifier.isPublic(constructor.getModifiers()) && Modifier.isPublic(extensionType.getModifiers())) {
                throw new InternalExtensionException(
                        "Extensions that are defined as public classes, must have a non-public constructor. As end-users should never instantiate them themself, extensionType = "
                                + extensionType);
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

    /** This loader is responsible for loading an extension and any dependencies that have not already been loaded. */
    private static final class Loader {

        // Maaske skal vi baade have id, og depth... Eller er depth ligegyldigt???
        // final static Map<String, String> baseExtensions = Map.of();

        private static final WeakHashMap<Class<? extends Extension>, Throwable> ERRORS = new WeakHashMap<>();

        private static final WeakHashMap<Class<? extends Extension>, ExtensionModel> EXTENSIONS = new WeakHashMap<>();

        /** A lock used for making sure that we only load one extension tree at a time. */
        private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

        private static int nextExtensionId;

        private final ArrayDeque<Class<? extends Extension>> stack = new ArrayDeque<>();

        private ExtensionModel load1(Class<? extends Extension> extensionType) {
            // Den eneste grund til at vi gennem en exception er pga
            if (stack.contains(extensionType)) {
                String st = stack.stream().map(e -> e.getCanonicalName()).collect(Collectors.joining(" -> "));
                throw new InternalExtensionException("Cyclic dependencies between extensions encountered: " + st + " -> " + extensionType.getCanonicalName());
            }
            stack.push(extensionType);

            ExtensionModel m;
            try {
                ExtensionModel.Builder builder = new ExtensionModel.Builder(extensionType, this, nextExtensionId++);

                // TODO move this to the builder when it has loaded all its dependencies...
                // And maybe make nextExtension it local to Loader and only update the static one
                // when every extension has been successfully loaded...

                // ALSO nextExtensionID should probably be reset...
                m = builder.build();
            } catch (Throwable t) {
                ERRORS.put(extensionType, t);
                throw t;
            } finally {
                stack.pop();
            }

            // All dependencies have been successfully validated before we add the actual extension
            // and any of its pipelines to permanent storage
            EXTENSIONS.put(extensionType, m);

            return m;
        }

        // taenker vi godt kan flytte global lock en class valuen...

        private static ExtensionModel load(Class<? extends Extension> extensionType, @Nullable Loader loader) {
            GLOBAL_LOCK.lock();
            try {
                // First lets see if we have created the model before
                ExtensionModel m = EXTENSIONS.get(extensionType);
                if (m != null) {
                    return m;
                }

                // Lets then see if we have tried to create the model before, but failed
                Throwable t = ERRORS.get(extensionType);
                if (t != null) {
                    throw new InternalExtensionException("Extension " + extensionType + " failed to be configured previously", t);
                }

                // Create a new loader if we are not already part of one
                if (loader == null) {
                    loader = new Loader();
                }
                return loader.load1(extensionType);
            } finally {
                GLOBAL_LOCK.unlock();
            }
        }
    }
}
