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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import app.packed.base.Nullable;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.MethodHook;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.base.devtools.PackedDevToolsIntegration;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * This class is responsible for finding fields, methods, parameters that have hook annotations.
 */
public final class Introspector {

    /** We never process classes that are located in the {@code java.base} module. */
    public static final Module JAVA_BASE_MODULE = Object.class.getModule();

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(),
            BeanIntrospector.class, "initialize", void.class, ExtensionSetup.class, BeanSetup.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "newBeanIntrospector", BeanIntrospector.class);

    /** The bean that is being introspected. */
    public final BeanSetup bean;

    /** The class we are introspecting. */
    private final Class<?> beanClass;

    /** Non-null if a introspector was set via {@link BeanHandle.Installer#introspectWith(BeanIntrospector)}. */
    @Nullable
    private final BeanIntrospector beanHandleIntrospector;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    // I think we sort in BeanFields...
    /** Every extension that is activated by a hook. */
    private final LinkedHashMap<Class<? extends Extension<?>>, ExtensionEntry> extensions = new LinkedHashMap<>();

    // Should be made lazily??? I think
    final OpenClass oc;

    public Introspector(BeanSetup bean, @Nullable BeanIntrospector beanHandleIntrospector) {
        this.bean = bean;
        this.beanClass = bean.beanClass();
        this.beanHandleIntrospector = beanHandleIntrospector;
        this.oc = OpenClass.of(MethodHandles.lookup(), beanClass);
    }

    ExtensionEntry computeExtensionEntry(Class<? extends Extension<?>> extensionType, boolean fullAccess) {
        return extensions.computeIfAbsent(extensionType, c -> {
            // Get the extension (installing it if necessary)
            ExtensionSetup extension = bean.container.useExtensionSetup(extensionType, null);

            BeanIntrospector introspector;
            if (beanHandleIntrospector != null && bean.operator() == extensionType) {
                // A special introspector has been set, don't
                introspector = beanHandleIntrospector;
                try {
                    MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, extension, bean);
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            } else {
                try {
                    introspector = (BeanIntrospector) MH_EXTENSION_NEW_BEAN_INTROSPECTOR.invokeExact(extension.instance());
                    MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, extension, bean);
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            }

            // Notify the bean introspector that is being used
            introspector.onPreIntrospect();
            return new ExtensionEntry(extension, introspector, fullAccess);
        });
    }

    /** Introspects the bean. */
    public void introspect() {
        // Process all annotations on the class
        introspectClass();

        // Process all fields on the bean
        IntrospectorOnField.introspectFields(this, beanClass);

        // Process all methods on the bean
        record MethodHelper(int hash, String name, Class<?>[] parameterTypes) {

            MethodHelper(Method method) {
                this(method.getName(), method.getParameterTypes());
            }

            MethodHelper(String name, Class<?>[] parameterTypes) {
                this(name.hashCode() ^ Arrays.hashCode(parameterTypes), name, parameterTypes);
            }

            /** {@inheritDoc} */
            @Override
            public boolean equals(Object obj) {
                return obj instanceof MethodHelper h && name == h.name() && Arrays.equals(parameterTypes, h.parameterTypes);
            }

            /** {@inheritDoc} */
            @Override
            public int hashCode() {
                return hash;
            }
        }

        HashSet<Package> packages = new HashSet<>();
        HashMap<MethodHelper, HashSet<Package>> types = new HashMap<>();

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : beanClass.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(new MethodHelper(m), packages);
                introspectMethod(m);// move this to step 2???
            }
        }

        // Step 2 process all declared methods

        // Maybe some kind of detection if current type (c) switches modules.
        for (Class<?> c = beanClass; c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            Method[] methods = c.getDeclaredMethods();
            PackedDevToolsIntegration.INSTANCE.reflectMembers(c, methods);
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isStatic(mod)) {
                    if (c == beanClass && !Modifier.isPublic(mod)) { // we have already processed public static methods
                        // only include static methods in the top level class
                        // We do this, because it would be strange to include
                        // static methods on any interfaces this class implements.
                        // But it would also be strange to include static methods on sub classes
                        // but not include static methods on interfaces.
                        introspectMethod(m);
                    }
                } else if (!m.isBridge() && !m.isSynthetic()) { // TODO should we include synthetic methods??
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(new MethodHelper(m), key -> new HashSet<>());
                        if (pkg != packages && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new MethodHelper(m), packages) != null) {
                            continue; // method has been overridden by a super type
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Private methods are never overridden
                    }
                    introspectMethod(m);
                }
            }
        }

        // Call into every BeanScanner and tell them its all over
        for (ExtensionEntry e : extensions.values()) {
            e.introspector.onPostIntrospect();
        }
    }

    private void introspectClass() {}


    /**
     * Look for hook annotations on a single method.
     * 
     * @param method
     *            the method to look for annotations on
     */
    private void introspectMethod(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            MethodAnnotationCache fh = MethodAnnotationCache.CACHE.get(a1Type);
            if (fh != null) {
                ExtensionEntry ei = computeExtensionEntry(fh.extensionType, false);

                IntrospectorOnMethod pbm = new IntrospectorOnMethod(Introspector.this, ei.extension, method, annotations, fh.isInvokable);

                ei.introspector.onMethodHook(pbm);
            }
        }
    }

    static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }

    record ExtensionEntry(ExtensionSetup extension, BeanIntrospector introspector, boolean hasFullAccess) {}

    private record MethodAnnotationCache(Class<? extends Extension<?>> extensionType, boolean isInvokable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodAnnotationCache> CACHE = new ClassValue<>() {

            @Override
            protected MethodAnnotationCache computeValue(Class<?> type) {
                MethodHook h = type.getAnnotation(MethodHook.class);
                if (h == null) {
                    return null;
                }
                checkExtensionClass(type, h.extension());
                return new MethodAnnotationCache(h.extension(), h.allowInvoke());
            }
        };
    }
}
