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
import java.lang.reflect.Method;
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
import app.packed.component.WireletHandler;
import app.packed.container.ComponentLinked;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionMember;
import app.packed.container.ExtensionSet;
import app.packed.container.ExtensionSetup;
import app.packed.container.InternalExtensionException;
import app.packed.hook.OnHook;
import app.packed.statemachine.LifecycleContext;
import packed.internal.base.attribute.ProvidableAttributeModel;
import packed.internal.component.OldPackedComponentDriver;
import packed.internal.component.PackedComponentDriver;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hook.BaseHookQualifierList;
import packed.internal.hook.OnHookModel;
import packed.internal.invoke.MethodHandleBuilder;
import packed.internal.invoke.OpenClass;
import packed.internal.lifecycle.old.LifecycleDefinition;
import packed.internal.sidecar.old.SidecarModel;
import packed.internal.sidecar.old.SidecarTypeMeta;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/** A model of an Extension (sidecar). */
public final class ExtensionModel extends SidecarModel implements Comparable<ExtensionModel> {

    /** A cache of extension models. */
    private static final ClassValue<ExtensionModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionModel computeValue(Class<?> type) {
            // First, check that the user has specified an actual sub type of Extension to ExtensionModel.of()
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

    static final int ON_0_INSTANTIATION = 0;

    static final int ON_1_MAIN = 1;

    static final int ON_2_CHILDREN_DONE = 2;

    /**  */
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

    final MethodHandle bundleBuilderMethod;

    /**
     * The depth of this extension in a global . Defined as 0 if no dependencies otherwise max(all dependencies depth) + 1.
     */
    // Depth is the length of the path to its BaseExtension
    // 0 for BaseExtension otherwise the length of the longest path to BaseExtension
    // -> Dependencies of a given extension always have a depth that is less than the given extension.
    // Det er jo ikke et trae... Men en graph. Giver depth mening?
    // Man kan argumentere med at man laver en masse hylder, hvor de enkelte extensions saa er.
    private final int depth;

    /** This extension's direct dependencies (on other extensions). */
    private final ExtensionSet directDependencies;

    /** Whether or not is is only any immediately parent that will be linked. */
    final boolean extensionLinkedDirectChildrenOnly;

    /** A method handle to an optional method annotated with {@link ComponentLinked} on the extension. */
    @Nullable
    final MethodHandle extensionLinkedToAncestorExtension; // will have an extensionLinkedToAncestorService in the future

    final BaseHookQualifierList hooksNonActivating;

    @Nullable
    private final OnHookModel hooksOnHookModel;

    /** A unique id of the extension. */
    final int id; //

    /** The canonical name of the extension. Used when needing to deterministically sort extensions. */
    private final String nameUsedForSorting;

    /** An optional containing the extension type. To avoid excessive creation of them at runtime. */
    public final Optional<Class<? extends Extension>> optional; // can go away with Valhalla

    /** The default component name of the extension. */
    public final String defaultComponentName;

    private final OldPackedComponentDriver<?> driver;

    private final ProvidableAttributeModel pam;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionModel(Builder builder) {
        super(builder);
        this.id = builder.id;
        this.depth = builder.depth;
        this.bundleBuilderMethod = builder.builderMethod;
        this.directDependencies = ExtensionSet.of(builder.dependenciesDirect);// Set.copyOf(builder.dependenciesDirect);
        this.optional = Optional.of(extensionType()); // No need to create an optional every time we need this
        this.nameUsedForSorting = requireNonNull(extensionType().getCanonicalName());
        this.driver = PackedComponentDriver.extensionDriver(this);
        this.defaultComponentName = "." + extensionType().getSimpleName();
        this.extensionLinkedToAncestorExtension = builder.li;
        this.extensionLinkedDirectChildrenOnly = builder.callbackOnlyDirectChildren;
        this.pam = builder.pam;

        this.hooksOnHookModel = builder.onHookModel;
        this.hooksNonActivating = hooksOnHookModel == null ? null : LazyExtensionActivationMap.findNonExtending(hooksOnHookModel);
    }

    public ProvidableAttributeModel pam() {
        return pam;
    }

    public OldPackedComponentDriver<?> driver() {
        return driver;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionModel m) {
        // id < #baseExtension return id-id;
        // otherwise non base extension
        int d = depth - m.depth;
        if (d == 0) {
            int c = nameUsedForSorting.compareTo(m.nameUsedForSorting);
            if (c == 0) {
                // Cannot use two extension with the same name. But loaded via two different
                // classloaders
                /// Hmmmm har nogen gange hvor dette er
                // throw new IllegalStateException();
            }
            return c;
        } else {
            return d;
        }
//        return d == 0 ? nameUsedForSorting.compareTo(m.nameUsedForSorting) : d;
    }

    public int depth() {
        return depth;
    }

    /**
     * Returns a set of all the direct dependencies of this extension as specified via {@link ExtensionSetup}.
     * 
     * @return a set of all the direct dependencies of this extension
     */
    public ExtensionSet directDependencies() {
        return directDependencies;
    }

    /**
     * Returns the extension type of this model.
     * 
     * @return the extension type of this model
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Extension> extensionType() {
        return (Class<? extends Extension>) type();
    }

    /**
     * Creates a new instance of the extension.
     * 
     * @param context
     *            the extension context that can be constructor injected into the extension
     * @return a new instance of the extension
     */
    Extension newInstance(PackedExtensionConfiguration context) {
        try {
            return (Extension) constructor.invokeExact(context);
        } catch (Throwable e) {
            throw new InternalExtensionException("Extension (" + type().getSimpleName() + ")  could not be created", e);
        }
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
    public static Class<? extends Extension> findAnyExtensionMember(Class<?> type) {
        ExtensionMember ue = type.getAnnotation(ExtensionMember.class);
        if (ue != null) {
            Class<? extends Extension> eType = ue.value();
            if (type.getModule() != eType.getModule()) {
                throw new InternalExtensionException("The extension " + eType + " and type " + type + " must be defined in the same module, was "
                        + eType.getModule() + " and " + type.getModule());
            }
            of(eType); // Make sure the extension is valid
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
     */
    public static ExtensionModel of(Class<? extends Extension> extensionType) {
        return MODELS.get(extensionType);
    }

    /**
     * Returns a on hook model of all the methods annotated with {@link OnHook} on the extension. Or null if the extension
     * does not define any methods annotated with {@link OnHook}.
     * 
     * @param extensionType
     *            the extension type to return the model for
     * @return a hook model
     */
    @Nullable
    public static OnHookModel onHookModelOf(Class<? extends Extension> extensionType) {
        return of(extensionType).hooksOnHookModel;
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Extension>> resolveOptional(Class<?> extensionType) {
        Object result = OPTIONALS.get(extensionType);
        if (result instanceof List) {
            return (List<Class<? extends Extension>>) result;
        }
        return ThrowableUtil.throwReturn((Throwable) result);
    }

    /** A builder of {@link ExtensionModel}. */
    static final class Builder extends SidecarModel.Builder {

        /** Meta data about the extension sidecar. */
        public static final SidecarTypeMeta STM = new SidecarTypeMeta(ExtensionSetup.class, LifecycleDefinition.of(ExtensionSetup.INSTANTIATING,
                ExtensionSetup.NORMAL_USAGE, ExtensionSetup.CHILD_LINKING, ExtensionSetup.GUESTS_DEFINITIONS));

        // Whether or not it is only children... Or all ancestors
        private boolean callbackOnlyDirectChildren;

        /** A list of dependencies on other extensions. */
        private Set<Class<? extends Extension>> dependenciesDirect = new HashSet<>();

        /** The depth of the extension relative to other extensions. */
        private int depth;

        private final int id;

        private MethodHandle li;

        private Method linked;

        /** The loader used to load the extension. */
        private final Loader loader;

        /** A builder for all methods annotated with {@link OnHook} on the extension. */
        private OnHookModel onHookModel;

        private ProvidableAttributeModel pam;

        /**
         * Creates a new builder.
         * 
         * @param extensionType
         *            the type of extension we are building a model for
         */
        Builder(Class<? extends Extension> extensionType, Loader loader, int id) {
            super(extensionType, STM);
            this.loader = requireNonNull(loader);
            this.id = id;
        }

        private void addDependency(Class<? extends Extension> dependencyType) {
            if (dependencyType == sidecarType) {
                throw new InternalExtensionException("Extension " + sidecarType + " cannot depend on itself via " + ExtensionSetup.class);
            }
            ExtensionModel model = Loader.load(dependencyType, loader);
            depth = Math.max(depth, model.depth + 1);
            dependenciesDirect.add(dependencyType);
        }

        protected void addExtensionContextElements(MethodHandleBuilder builder, int index) {
            builder.addKey(ExtensionConfiguration.class, index);
            builder.addKey(LifecycleContext.class, PackedExtensionConfiguration.MH_LIFECYCLE_CONTEXT, index);
            builder.addAnnoClassMapper(WireletHandler.class, PackedExtensionConfiguration.MH_FIND_WIRELET, index);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        ExtensionModel build() {
            // See if the extension is annotated with @ExtensionSidecar
            ExtensionSetup em = sidecarType.getAnnotation(ExtensionSetup.class);
            if (em != null) {
                for (Class<? extends Extension> dependencyType : em.dependencies()) {
                    addDependency(dependencyType);
                }
                if (em.optionalDependencies().length > 0) {
                    for (Class<? extends Extension> dependencyType : resolveOptional(sidecarType)) {
                        addDependency(dependencyType);
                    }
                }
            }

            // I Would love to get rid of CONV

            MethodHandleBuilder mhbConstructor = MethodHandleBuilder.of(Extension.class, PackedExtensionConfiguration.class);
            addExtensionContextElements(mhbConstructor, 0);

            OpenClass cp = prep(mhbConstructor);
            this.onHookModel = OnHookModel.newModel(cp, false, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            this.pam = ProvidableAttributeModel.analyse(cp);

            if (linked != null) {
                // ancestor extension, descendant extension context, descendant extension
                MethodHandleBuilder iss = MethodHandleBuilder.of(void.class, Extension.class, PackedExtensionConfiguration.class, Extension.class);

                // From the child's extension context
                addExtensionContextElements(iss, 1);

                // The child's extension instance
                iss.addKey(sidecarType, 2); // should perform an implicit cast

                li = iss.build(cp, linked);
            }
            return new ExtensionModel(this);
        }

        @Override
        protected void decorateOnSidecar(MethodHandleBuilder builder) {
            addExtensionContextElements(builder, 1);
        }

        @Override
        protected void onMethod(Method m) {
            ComponentLinked da = m.getAnnotation(ComponentLinked.class);
            if (da != null) {
                if (linked != null) {
                    throw new IllegalStateException(
                            "Multiple methods annotated with " + ComponentLinked.class + " on " + m.getDeclaringClass() + ", only 1 allowed.");
                }
                linked = m;
                callbackOnlyDirectChildren = da.onlyDirectLink();
            }
        }
    }

    /** An (extension) loader is responsible for loading an extension and all of its (unloaded) dependencies. */
    private static final class Loader {

        // Maaske skal vi baade have id, og depth... Eller er depth ligegyldigt???
        // final static Map<String, String> baseExtensions = Map.of();

        private static final WeakHashMap<Class<? extends Extension>, Throwable> ERRORS = new WeakHashMap<>();

        private static final WeakHashMap<Class<? extends Extension>, ExtensionModel> EXTENSIONS = new WeakHashMap<>();

        /** A lock used for making sure that we only load one extension tree at a time. */
        private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

        private static int nextExtensionId;

        private final ArrayDeque<Class<? extends Extension>> stack = new ArrayDeque<>();

        private Loader() {}

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
