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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionLinked;
import app.packed.container.ExtensionSidecar;
import app.packed.container.InternalExtensionException;
import app.packed.container.MemberOfExtension;
import app.packed.container.WireletFind;
import app.packed.hook.OnHook;
import app.packed.lifecycle.LifecycleContext;
import packed.internal.hook.BaseHookQualifierList;
import packed.internal.hook.OnHookModel;
import packed.internal.lifecycle2.LifecycleDefinition;
import packed.internal.reflect.MethodHandleBuilder;
import packed.internal.reflect.OpenClass;
import packed.internal.sidecar.SidecarModel;
import packed.internal.sidecar.SidecarTypeMeta;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of an Extension (sidecar). */
public final class ExtensionModel extends SidecarModel implements Comparable<ExtensionModel> {

    /** A cache of extension models. */
    private static final ClassValue<ExtensionModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionModel computeValue(Class<?> type) {
            // First, check that the user has specified an actual sub type of Extension to
            // ContainerConfiguration#use() or Bundle#use()
            if (type == Extension.class) {
                throw new IllegalArgumentException(Extension.class.getSimpleName() + ".class is not a valid argument.");
            } else if (!Extension.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(type) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
            }
            return ExtensionModelLoader.load((Class<? extends Extension>) type);
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
            String[] dependencies = type.getAnnotation(ExtensionSidecar.class).optionalDependencies();

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
                        throw new InternalExtensionException("@" + ExtensionSidecar.class.getSimpleName() + " " + StringFormatter.format(type)
                                + " cannot specify Extension.class as an optional dependency, for " + StringFormatter.format(c));
                    } else if (!Extension.class.isAssignableFrom(c)) {
                        throw new InternalExtensionException("@" + ExtensionSidecar.class.getSimpleName() + " " + StringFormatter.format(type)
                                + " specified an invalid extension " + StringFormatter.format(c));
                    }

                    result.add((Class<? extends Extension>) c);
                }
            }
            return result;
        }
    };

    final MethodHandle bundleBuilderMethod;

    /** The depth of this extension. Defined as 0 if no dependencies otherwise max(all dependencies depth) + 1. */
    private final int depth;

    /** This extension's direct dependencies (on other extensions). */
    private final Set<Class<? extends Extension>> directDependencies;

    /** Whether or not is is only any immediately parent that will be linked. */
    final boolean extensionLinkedDirectChildrenOnly;

    /** A method handle to an optional method annotated with {@link ExtensionLinked} on the extension. */
    @Nullable
    final MethodHandle extensionLinkedToAncestorExtension; // will have an extensionLinkedToAncestorService in the future

    final BaseHookQualifierList hooksNonActivating;

    @Nullable
    private final OnHookModel hooksOnHookModel;

    /** A unique id of the extension. */
    final int id; //

    /** The canonical name of the extension. Used when needing to deterministically sort extensions. */
    private final String nameUsedForSorting;

    /** An optional containing the extension type. To avoid excessive creation of them for {@link Component#extension()}. */
    public final Optional<Class<? extends Extension>> optional; // can go away with Valhalla

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
        this.directDependencies = Set.copyOf(builder.dependenciesDirect);
        this.optional = Optional.of(extensionType()); // No need to create an optional every time we need this
        this.nameUsedForSorting = requireNonNull(extensionType().getCanonicalName());

        this.extensionLinkedToAncestorExtension = builder.li;
        this.extensionLinkedDirectChildrenOnly = builder.callbackOnlyDirectChildren;

        this.hooksOnHookModel = builder.onHookModel;
        this.hooksNonActivating = hooksOnHookModel == null ? null : LazyExtensionActivationMap.findNonExtending(hooksOnHookModel);
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

    /**
     * Returns a set of all the direct dependencies of this extension as specified via {@link ExtensionSidecar}.
     * 
     * @return a set of all the direct dependencies of this extension
     */
    public Set<Class<? extends Extension>> directDependencies() {
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
    Extension newInstance(PackedExtensionContext context) {
        try {
            return (Extension) constructor.invokeExact(context);
        } catch (Throwable e) {
//            ThrowableUtil.throwIfUnchecked(e);
            throw new InternalExtensionException("Instantiation of Extension failed", e);
        }
    }

    /**
     * Returns any value of {@link MemberOfExtension} annotation.
     * 
     * @param type
     *            the type look for an ExtensionMember annotation on
     * @return an extension the specified type is a member of
     * @throws InternalExtensionException
     *             if an annotation is present and the specified is not in the same module as the extension specified in
     *             {@link MemberOfExtension#value()}
     */
    @Nullable
    public static Class<? extends Extension> findAnyExtensionMember(Class<?> type) {
        MemberOfExtension ue = type.getAnnotation(MemberOfExtension.class);
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

    /** A builder for {@link ExtensionModel}. */
    static final class Builder extends SidecarModel.Builder {

        /** Meta data about the extension sidecar. */
        public static final SidecarTypeMeta STM = new SidecarTypeMeta(ExtensionSidecar.class, LifecycleDefinition.of(ExtensionSidecar.INSTANTIATING,
                ExtensionSidecar.NORMAL_USAGE, ExtensionSidecar.CHILD_LINKING, ExtensionSidecar.GUESTS_DEFINITIONS));

        // Whether or not it is only children... Or all ancestors
        private boolean callbackOnlyDirectChildren;

        /** A list of dependencies on other extensions. */
        private Set<Class<? extends Extension>> dependenciesDirect = new HashSet<>();

        /** The depth of the extension relative to other extensions. */
        private int depth;

        int id;

        MethodHandle li;

        Method linked;

        /** The loader used to load the extension. */
        private final ExtensionModelLoader loader;

        /** A builder for all methods annotated with {@link OnHook} on the extension. */
        private OnHookModel onHookModel;

        /**
         * Creates a new builder.
         * 
         * @param extensionType
         *            the type of extension we are building a model for
         */
        Builder(Class<? extends Extension> extensionType, ExtensionModelLoader loader) {
            super(extensionType, STM);
            this.loader = requireNonNull(loader);
        }

        private void addDependency(Class<? extends Extension> dependencyType) {
            if (dependencyType == sidecarType) {
                throw new InternalExtensionException("Extension " + sidecarType + " cannot depend on itself via " + ExtensionSidecar.class);
            }
            ExtensionModel model = ExtensionModelLoader.load(this, dependencyType, loader);
            depth = Math.max(depth, model.depth + 1);
            dependenciesDirect.add(dependencyType);
        }

        protected void addExtensionContextElements(MethodHandleBuilder builder, int index) {
            builder.addKey(ExtensionConfiguration.class, index);
            builder.addKey(LifecycleContext.class, PackedExtensionContext.MH_LIFECYCLE_CONTEXT, index);
            builder.addAnnoClassMapper(WireletFind.class, PackedExtensionContext.MH_FIND_WIRELET, index);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        ExtensionModel build() {
            // See if the extension is annotated with @ExtensionSidecar
            ExtensionSidecar em = sidecarType.getAnnotation(ExtensionSidecar.class);
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

            MethodHandleBuilder mhbConstructor = MethodHandleBuilder.of(Extension.class, PackedExtensionContext.class);
            addExtensionContextElements(mhbConstructor, 0);

            OpenClass cp = prep(mhbConstructor);
            this.onHookModel = OnHookModel.newModel(cp, false, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, ContainerConfiguration.class);

            if (linked != null) {
                // ancestor extension, descendant extension context, descendant extension
                MethodHandleBuilder iss = MethodHandleBuilder.of(void.class, Extension.class, PackedExtensionContext.class, Extension.class);

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
            ExtensionLinked da = m.getAnnotation(ExtensionLinked.class);
            if (da != null) {
                if (linked != null) {
                    throw new IllegalStateException(
                            "Multiple methods annotated with " + ExtensionLinked.class + " on " + m.getDeclaringClass() + ", only 1 allowed.");
                }
                linked = m;
                callbackOnlyDirectChildren = da.onlyDirectLink();
            }
        }
    }

//    static class ExtensionComparator implements Comparator<ExtensionModel> {
//
//        /** {@inheritDoc} */
//        @Override
//        public int compare(ExtensionModel m1, ExtensionModel m2) {
//            if (m1.depth == m2.depth) {
//                int c = m1.
//            }
//            return m1.depth - m2.depth;
//        }
//
//    }
}
