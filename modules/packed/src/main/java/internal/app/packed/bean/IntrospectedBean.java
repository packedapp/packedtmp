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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint.MethodHook;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.base.devtools.PackedDevToolsIntegration;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.oldservice.inject.BeanInjectionManager;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.ConstructorOperationTarget;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.ThrowableUtil;

/**
 * This class represents a single bean being introspected.
 */
public final class IntrospectedBean {

    /** We never process classes that are located in the {@code java.base} module. */
    static final Module JAVA_BASE_MODULE = Object.class.getModule();

    /** A handle for invoking the protected method {@link BeanIntrospector#initialize()}. */
    private static final MethodHandle MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(),
            BeanIntrospector.class, "initialize", void.class, ExtensionSetup.class, BeanSetup.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "newBeanIntrospector", BeanIntrospector.class);

    /** An internal lookup object. */
    private static final MethodHandles.Lookup PACKED = MethodHandles.lookup();

    /** The bean that is being introspected. */
    public final BeanSetup bean;

    /** Non-null if a introspector was set via {@link BeanHandle.BeanInstaller#introspectWith(BeanIntrospector)}. */
    @Nullable
    private final BeanIntrospector beanIntrospector;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    // I think we sort in BeanFields...
    // But then should we sort annotations as well?
    /** Every extension that is activated by a hook. */
    private final LinkedHashMap<Class<? extends Extension<?>>, Contributor> extensions = new LinkedHashMap<>();

    // Should be made lazily??? I think
    // I think we embed once we gotten rid of use cases outside of this introspector
    final OpenClass oc;

    IntrospectedBean(BeanSetup bean, @Nullable BeanIntrospector beanIntrospector) {
        this.bean = bean;
        this.beanIntrospector = beanIntrospector;
        this.oc = new OpenClass(PACKED, bean.beanClass);
    }

    Contributor computeContributor(Class<? extends Extension<?>> extensionType, boolean fullAccess) {
        return extensions.computeIfAbsent(extensionType, c -> {
            // Get the extension (installing it if necessary)
            ExtensionSetup extension = bean.container.safeUseExtensionSetup(extensionType, null);

            BeanIntrospector introspector;
            if (beanIntrospector != null && bean.installedBy.extensionType == extensionType) {
                // A special introspector has been set, don't
                introspector = beanIntrospector;
            } else {
                try {
                    introspector = (BeanIntrospector) MH_EXTENSION_NEW_BEAN_INTROSPECTOR.invokeExact(extension.instance());
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            }

            try {
                MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, extension, bean);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }
            // Notify the bean introspector that is being used
            introspector.onIntrospectionStop();
            return new Contributor(extension, introspector, fullAccess);
        });
    }

    /** Find a constructor on the bean and create an operation for it. */
    private void findConstructor() {
        IntrospectedBeanConstructor constructor = IntrospectedBeanConstructor.CACHE.get(bean.beanClass);

        MethodHandle mh = oc.unreflectConstructor(constructor.constructor());

        OperationSetup os = new OperationSetup(bean, constructor.operationType(), bean.installedBy,
                new ConstructorOperationTarget(mh, constructor.constructor()), null);
        bean.operations.add(os);
    }

    /** Introspect the bean. */
    void introspect() {

        // First, we process all annotations on the class
        introspectClass();

        // If a we have a (instantiating) class source, we need to find a constructor we can use
        if (bean.sourceKind == BeanSourceKind.CLASS && bean.beanKind.hasInstances()) {
            findConstructor();
        }

        bean.injectionManager = new BeanInjectionManager(bean); // legacy

        // Introspect all fields on the bean and its super classes
        IntrospectedBeanField.introspectAllFields(this, bean.beanClass);

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
        Class<?> beanClass = bean.beanClass;

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

        // Introspection of members are done.
        // Now run through all operation bindings that have not been resolved

        for (OperationSetup o : bean.operations) {
            resolveOperation(o);
        }

        // Call into every BeanScanner and tell them its all over
        for (Contributor e : extensions.values()) {
            e.introspector.onIntrospectionStart();
        }
    }

    // We need it for calling into nested
    void resolveOperation(OperationSetup operation) {
        for (int i = 0; i < operation.bindings.length; i++) {
            if (operation.bindings[i] == null) {
                IntrospectedBeanParameter.bind(this, operation, i);
            }
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
                Contributor contributor = computeContributor(fh.extensionType, false);

                IntrospectedBeanMethod pbm = new IntrospectedBeanMethod(IntrospectedBean.this, contributor, method, annotations, fh.isInvokable);

                contributor.introspector.onMethod(pbm);
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

    /**
     * An instance of this class is created per extension that participates in the introspection.
     */
    public record Contributor(ExtensionSetup extension, BeanIntrospector introspector, boolean hasFullAccess) {}

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

    /**
     * An open class is a thin wrapper for a single class and a {@link Lookup} object.
     * <p>
     * This class is not safe for use with multiple threads.
     */
    // TODO should we know whether or the lookup is Packed one or a user supplied??
    // lookup.getClass().getModule==OpenClass.getModule...? nah virker ikke paa classpath
    final class OpenClass {

        /** The app.packed.base module. */
        private static final Module APP_PACKED_BASE_MODULE = OpenClass.class.getModule();

        /** A lookup object that can be used to access {@link #type}. */
        private final MethodHandles.Lookup lookup;

        /** A lookup that can be used on non-public members. */
        private MethodHandles.Lookup privateLookup;

        /** Whether or not the private lookup has been initialized. */
        private boolean privateLookupInitialized;

        /** The class that is wrapped. */
        private final Class<?> type;

        OpenClass(MethodHandles.Lookup lookup, Class<?> clazz) {
            this.lookup = requireNonNull(lookup);
            this.type = requireNonNull(clazz);
        }

        Lookup lookup(Member member) {
            // If we already have made a private lookup object, lets just use it. Even if could do with Public lookup
            MethodHandles.Lookup p = privateLookup;
            if (p != null) {
                return p;
            }

            // See if we need private access, otherwise just return ordinary lookup.

            // Needs private lookup, unless class is public or protected and member is public
            // We are comparing against the members declaring class..
            // We could store boolean isPublicOrProcected in a field.
            // But do not know how it would work with abstract super classes in other modules...

            // See if we need private access, otherwise just return ordinary lookup.
            if (!needsPrivateLookup(member)) {
                return lookup;
            }

            if (!privateLookupInitialized) {
                String pckName = type.getPackageName();
                if (!type.getModule().isOpen(pckName, APP_PACKED_BASE_MODULE)) {
                    String otherModule = type.getModule().getName();
                    String m = APP_PACKED_BASE_MODULE.getName();
                    throw new InaccessibleBeanMemberException("In order to access '" + StringFormatter.format(type) + "', the module '" + otherModule
                            + "' must be open to '" + m + "'. This can be done, for example, by adding 'opens " + pckName + " to " + m
                            + ";' to the module-info.java file of " + otherModule);
                }
                // Should we use lookup.getdeclaringClass???
                if (!APP_PACKED_BASE_MODULE.canRead(type.getModule())) {
                    APP_PACKED_BASE_MODULE.addReads(type.getModule());
                }
                privateLookupInitialized = true;
            }

            // Create and cache a private lookup.
            try {
                // Fjernede lookup... Skal vitterligt have samlet det i en klasse
                return privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /* lookup */);
            } catch (IllegalAccessException e) {
                throw new InaccessibleBeanMemberException("Could not create private lookup [type=" + type + ", Member = " + member + "]", e);
            }
        }

        MethodHandle unreflectConstructor(Constructor<?> constructor) {
            Lookup lookup = lookup(constructor);

            try {
                return lookup.unreflectConstructor(constructor);
            } catch (IllegalAccessException e) {
                throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
            }
        }

        MethodHandle unreflectGetter(Field field) {
            Lookup lookup = lookup(field);

            try {
                return lookup.unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
            }
        }

        private static boolean needsPrivateLookup(Member m) {
            // Needs private lookup, unless class is public or protected and member is public
            // We are comparing against the members declaring class..
            // We could store boolean isPublicOrProcected in a field.
            // But do not know how it would work with abstract super classes in other modules...
            int classModifiers = m.getDeclaringClass().getModifiers();
            return !((Modifier.isPublic(classModifiers) || Modifier.isProtected(classModifiers)) && Modifier.isPublic(m.getModifiers()));
        }
    }

    /**
     * 
     */
    void resolveOperations() {}
}
