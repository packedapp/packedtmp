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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.hook.OnHook;
import packed.internal.container.ContainerModel;
import packed.internal.container.ExtensionModel;
import packed.internal.container.LazyExtensionActivationMap;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hook.HookRequest;
import packed.internal.hook.HookRequestBuilder;
import packed.internal.hook.MemberUnreflector;
import packed.internal.reflect.OpenClass;
import packed.internal.sidecar.Model;
import packed.internal.util.ThrowableUtil;

/**
 * A model of a container, a cached instance of this class is acquired via
 * {@link ContainerModel#componentModelOf(Class)}.
 */
public final class ComponentModel extends Model {

    /** An array of any extensions with relevant {@link OnHook} methods. */
    private final ExtensionRequestPair[] extensionHooks;

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    /// Should we have a little of cache simpleName0, simpleName1, ...
    private String simpleName;

    /** Any methods annotated with {@link OnHook} on the container source. */
    @Nullable
    private final HookRequest sourceHook;

    /**
     * Creates a new descriptor.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private ComponentModel(ComponentModel.Builder builder) {
        super(builder.cp.type());

        try {
            this.sourceHook = builder.csb == null ? null : builder.csb.build();
        } catch (Throwable ee) {
            throw ThrowableUtil.orUndeclared(ee);
        }

        // There should probably be some order we call extensions in....
        /// Other first, packed lasts?
        /// Think they need an order id....
        // Boer vaere lowest dependency id first...
        // Preferable deterministic

        this.extensionHooks = builder.extensionBuilders.entrySet().stream().map(e -> {
            HookRequest r;
            try {
                r = e.getValue().build();
            } catch (Throwable ee) {
                throw ThrowableUtil.orUndeclared(ee);
            }
            return new ExtensionRequestPair(e.getKey(), r);
        }).toArray(i -> new ExtensionRequestPair[i]);
    }

    /**
     * Returns the default prefix for the component, if no name is explicitly set by the user.
     * 
     * @return the default prefix for the component, if no name is explicitly set by the user
     */
    String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = type().getSimpleName();
        }
        return s;
    }

    <T> PackedComponentContext invokeOnHookOnInstall(Object cs, PackedComponentContext acc) {
        try {
            // First invoke any OnHook methods on the container source (bundle)
            if (sourceHook != null) {
                sourceHook.invoke(cs, acc);
            }

            // Next, invoke any OnHook methods on relevant extensions.
            for (ExtensionRequestPair he : extensionHooks) {
                // Finds (possible installing) the extension with @OnHook methods
                Extension extension = acc.container().use(he.extensionType);

                // Invoke each method annotated with @OnHook on the extension instance
                he.request.invoke(extension, acc);
            }
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return acc;
    }

    /**
     * Creates a new component model instance.
     * 
     * @param csm
     *            a model of the container source that is trying to install the component
     * @param cp
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static ComponentModel newInstance(ContainerModel csm, OpenClass cp) {
        return new Builder(csm, cp).build();
    }

    /** A builder object for a component model. */
    private static final class Builder {

        private final OpenClass cp;

        HookRequestBuilder csb;

        final ContainerModel csm;

        /** A map of builders for every activated extension. */
        private final IdentityHashMap<Class<? extends Extension>, HookRequestBuilder> extensionBuilders = new IdentityHashMap<>();

        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        private Builder(ContainerModel csm, OpenClass cp) {
            this.csm = requireNonNull(csm);
            this.cp = requireNonNull(cp);
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        private ComponentModel build() {
            final LazyExtensionActivationMap activatorMap = csm.activatorMap;
            Class<?> componentType = cp.type();

            try (MemberUnreflector htp = new MemberUnreflector(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                this.csb = csm.hooks() == null ? null : new HookRequestBuilder(csm.hooks(), htp);

                findAssinableTo(htp, activatorMap, componentType);
                findAnnotatedTypes(htp, activatorMap, componentType);
                // Inherited annotations???
                cp.findMethodsAndFields(method -> findAnnotatedMethods(htp, activatorMap, method), field -> findAnnotatedFields(htp, activatorMap, field));
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
            return new ComponentModel(this);
        }

        private void findAssinableTo(MemberUnreflector htp, LazyExtensionActivationMap activatorMap, Class<?> componentType) throws Throwable {
            HashSet<Class<?>> seen = new HashSet<>();
            for (Class<?> current = componentType; current != Object.class; current = current.getSuperclass()) {
                putInto(htp, activatorMap, seen, componentType, current);
            }
        }

        public void putInto(MemberUnreflector htp, LazyExtensionActivationMap activatorMap, HashSet<Class<?>> seen, Class<?> actualType, Class<?> clazz)
                throws Throwable {
            for (Class<?> cl : clazz.getInterfaces()) {
                if (seen.add(cl)) {
                    findAssinableTo0(htp, cl, actualType, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(cl));
                }

                if (activatorMap != null) {
                    findAssinableTo0(htp, cl, actualType, activatorMap.onAssignableTo(cl));
                }
                if (csb != null) {
                    csb.onAssignableTo(cl, actualType);
                }
                putInto(htp, activatorMap, seen, actualType, cl);
            }
        }

        private void findAssinableTo0(MemberUnreflector hookProcessor, Class<?> hookType, Class<?> actualType, Set<Class<? extends Extension>> extensionTypes)
                throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAssignableTo(hookType, actualType);
                }
            }
        }

        private void findAnnotatedFields(MemberUnreflector htp, LazyExtensionActivationMap activatorMap, Field field) throws Throwable {
            for (Annotation a : field.getAnnotations()) {
                findAnnotatedFields0(htp, a, field, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(a.annotationType()));
                if (activatorMap != null) {
                    findAnnotatedFields0(htp, a, field, activatorMap.onAnnotatedMethod(a.annotationType()));
                }
                if (csb != null) {
                    csb.onAnnotatedField(field, a);
                }
            }
        }

        private void findAnnotatedFields0(MemberUnreflector hookProcessor, Annotation a, Field field, Set<Class<? extends Extension>> extensionTypes)
                throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAnnotatedField(field, a);
                }
            }
        }

        private void findAnnotatedMethods(MemberUnreflector htp, LazyExtensionActivationMap activatorMap, Method method) throws Throwable {
            for (Annotation a : method.getAnnotations()) {
                findAnnotatedMethods0(htp, a, method, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(a.annotationType()));
                if (activatorMap != null) {
                    findAnnotatedMethods0(htp, a, method, activatorMap.onAnnotatedMethod(a.annotationType()));
                }
                if (csb != null) {
                    csb.onAnnotatedMethod(method, a);
                }
            }
        }

        private void findAnnotatedMethods0(MemberUnreflector hookProcessor, Annotation a, Method method, Set<Class<? extends Extension>> extensionTypes)
                throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAnnotatedMethod(method, a);
                }
            }
        }

        private void findAnnotatedTypes(MemberUnreflector htp, LazyExtensionActivationMap activatorMap, Class<?> componentType) throws Throwable {
            for (Annotation a : componentType.getAnnotations()) {
                findAnnotatedTypes0(htp, componentType, a, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(a.annotationType()));
                if (activatorMap != null) {
                    findAnnotatedTypes0(htp, componentType, a, activatorMap.onAnnotatedType(a.annotationType()));
                }
                if (csb != null) {
                    csb.onAnnotatedType(componentType, a);
                }
            }
        }

        private void findAnnotatedTypes0(MemberUnreflector hookProcessor, Class<?> componentType, Annotation a, Set<Class<? extends Extension>> extensionTypes)
                throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAnnotatedType(componentType, a);
                }
            }
        }
    }

    private static final class ExtensionRequestPair {

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        private final HookRequest request;

        private ExtensionRequestPair(Class<? extends Extension> extensionType, HookRequest request) {
            this.extensionType = requireNonNull(extensionType);
            this.request = requireNonNull(request);
        }
    }
}
