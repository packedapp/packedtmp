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
import app.packed.container.DescendentAdded;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.ExtensionMember;
import app.packed.container.InternalExtensionException;
import app.packed.container.WireletSupply;
import app.packed.hook.OnHook;
import app.packed.lifecycle.LifecycleContext;
import app.packed.sidecar.ExtensionSidecar;
import packed.internal.hook.BaseHookQualifierList;
import packed.internal.hook.OnHookModel;
import packed.internal.reflect.MethodHandleBuilder;
import packed.internal.reflect.OpenClass;
import packed.internal.sidecar.SidecarModel;
import packed.internal.sidecar.SidecarTypeMeta;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of an Extension (sidecar). */
public final class ExtensionModel extends SidecarModel implements Comparable<ExtensionModel> {

    /** A cache of models. */
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

    static final int ON_CHILDREN_DONE = 2;

    static final int ON_INSTANTIATION = 0;

    static final int ON_MAIN = 1;

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

    public final boolean callbackOnlyDirectChildren;

    /** The depth of this extension. Defined as 0 if no dependencies otherwise max(all dependencies depth) + 1. */
    private final int depth;

    /** A set of this extension's direct dependencies of other extensions. */
    private final Set<Class<? extends Extension>> directDependencies;

    /** A unique id of the extension. */
    final int id; //

    /** The canonical name of the extension. */
    private final String name;

    final BaseHookQualifierList nonActivatingHooks;

    @Nullable
    private final OnHookModel onHookModel;

    /** An optional containing the extension type. To avoid excessive creation of them for {@link Component#extension()}. */
    public final Optional<Class<? extends Extension>> optional;

    @Nullable
    public final MethodHandle parentExtensionLinked;

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
        this.name = requireNonNull(extensionType().getCanonicalName());

        this.onHookModel = builder.onHookModel;
        this.nonActivatingHooks = onHookModel == null ? null : LazyExtensionActivationMap.findNonExtending(onHookModel);

        this.parentExtensionLinked = builder.li;
        this.callbackOnlyDirectChildren = builder.callbackOnlyDirectChildren;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionModel m) {
        // id < #baseExtension return id-id;
        // otherwise non base extension
        int d = depth - m.depth;
        return d == 0 ? name.compareTo(m.name) : d;
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
    public Extension newExtensionInstance(PackedExtensionContext context) {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        try {
            return (Extension) constructor.invokeExact(context);
        } catch (Throwable e) {
//            ThrowableUtil.throwIfUnchecked(e);
            throw new InternalExtensionException("Instantiation of Extension failed", e);
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
    public static Class<? extends Extension> findIfMember(Class<?> type) {
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
        return of(extensionType).onHookModel;
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
        public static final SidecarTypeMeta STM = new SidecarTypeMeta(ExtensionSidecar.class, ExtensionSidecar.INSTANTIATION, ExtensionSidecar.ON_PREEMBLE,
                ExtensionSidecar.CHILDREN_CONFIGURED, ExtensionSidecar.GUESTS_CONFIGURED);

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
                throw new InternalExtensionException("Extension " + sidecarType + " cannot dependend on itself via " + ExtensionSidecar.class);
            }
            ExtensionModel model = ExtensionModelLoader.load(this, dependencyType, loader);
            depth = Math.max(depth, model.depth + 1);
            dependenciesDirect.add(dependencyType);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        ExtensionModel build() {

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
            mhbConstructor.addKey(ExtensionContext.class, 0);
            mhbConstructor.addKey(LifecycleContext.class, PackedExtensionContext.MH_LIFECYCLE_CONTEXT, 0);
            mhbConstructor.addAnnoClassMapper(WireletSupply.class, PackedExtensionContext.MH_FIND_WIRELET, 0);

            OpenClass cp = prep(mhbConstructor);
            this.onHookModel = OnHookModel.newModel(cp, false, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, ContainerConfiguration.class);

            if (linked != null) {
                // ancestor extension, descendant extension context, descendant extension
                MethodHandleBuilder iss = MethodHandleBuilder.of(void.class, Extension.class, PackedExtensionContext.class, Extension.class);

                // From the child's extension context
                iss.addKey(ExtensionContext.class, 1);
                iss.addKey(LifecycleContext.class, PackedExtensionContext.MH_LIFECYCLE_CONTEXT, 1);
                iss.addAnnoClassMapper(WireletSupply.class, PackedExtensionContext.MH_FIND_WIRELET, 1);

                // The child's extension instance
                iss.addKey(sidecarType, 2); // should perform an implicit cast

                li = iss.build(cp, linked);
            }
            return new ExtensionModel(this);
        }

        @Override
        protected void decorateOnSidecar(MethodHandleBuilder builder) {
            builder.addKey(ExtensionContext.class, 1);
            builder.addKey(LifecycleContext.class, PackedExtensionContext.MH_LIFECYCLE_CONTEXT, 1);
            builder.addAnnoClassMapper(WireletSupply.class, PackedExtensionContext.MH_FIND_WIRELET, 1);
        }

        @Override
        protected void onMethod(Method m) {
            DescendentAdded da = m.getAnnotation(DescendentAdded.class);
            if (da != null) {
                if (linked != null) {
                    throw new IllegalStateException(
                            "Multiple methods annotated with " + DescendentAdded.class + " on " + m.getDeclaringClass() + ", only 1 allowed.");
                }
                linked = m;
                callbackOnlyDirectChildren = da.onlyDirectChildren();
            }
        }
    }
}
