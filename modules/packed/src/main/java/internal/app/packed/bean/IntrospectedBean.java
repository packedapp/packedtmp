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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.framework.devtools.PackedDevToolsIntegration;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.ConstructorOperationSetup;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.operation.binding.BindingSetup;
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

    final BeanHookModel hookModel;

    // Should be made lazily??? I think
    // I think we embed once we gotten rid of use cases outside of this introspector
    final OpenClass oc;

    final ArrayDeque<OperationSetup> unBoundOperations = new ArrayDeque<>();

    IntrospectedBean(BeanSetup bean, @Nullable BeanIntrospector beanIntrospector) {
        this.bean = bean;
        this.hookModel = bean.container.assembly.assemblyModel.hookModel;
        this.beanIntrospector = beanIntrospector;
        this.oc = new OpenClass(PACKED, bean.beanClass);
    }

    /**
     * @param extensionType
     * @param fullAccess
     * @return the contributor
     */
    Contributor computeContributor(Class<? extends Extension<?>> extensionType, boolean fullAccess) {
        return extensions.computeIfAbsent(extensionType, c -> {
            // Get the extension (installing it if necessary)
            ExtensionSetup extension = bean.container.useExtension(extensionType, null);

            final BeanIntrospector introspector;

            // if a special bean introspected has been set from the extension that is installing the bean
            // Use this bean introspector, otherwise create a new one
            if (beanIntrospector != null && bean.installedBy.extensionType == extensionType) {
                introspector = beanIntrospector;
            } else {
                try {
                    introspector = (BeanIntrospector) MH_EXTENSION_NEW_BEAN_INTROSPECTOR.invokeExact(extension.instance());
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            }

            // Call BeanIntrospector#initialize
            try {
                MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, extension, bean);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }

            // Notify the bean introspector that it is being used
            introspector.onIntrospectionStart();
            return new Contributor(extension, introspector, fullAccess);
        });
    }

    /** Find a constructor on the bean and create an operation for it. */
    private void findConstructor() {
        IntrospectedBeanConstructor constructor = IntrospectedBeanConstructor.CACHE.get(bean.beanClass);

        MethodHandle mh = oc.unreflectConstructor(constructor.constructor());

        OperationSetup os = new ConstructorOperationSetup(bean.installedBy, bean, constructor.constructor(), mh);
        os.invocationType = (PackedOperationTemplate) os.invocationType.withReturnType(constructor.constructor().getDeclaringClass());
        bean.operations.add(os);
        unBoundOperations.add(os);
        resolveOperations();
    }

    /** Introspect the bean. */
    void introspect() {
        bean.introspecting = this;
        // First, we process all annotations on the class
        introspectClass();

        // If a we have a (instantiating) class source, we need to find a constructor we can use
        if (bean.sourceKind == BeanSourceKind.CLASS && bean.beanKind.hasInstances()) {
            findConstructor();
        }

        if (bean.realm instanceof ExtensionTreeSetup e) {
            if (bean.beanKind == BeanKind.CONTAINER) {
                bean.ownedBy.injectionManager.addBean(bean);
            }
        }
        if (bean.sourceKind == BeanSourceKind.INSTANCE) {
            bean.lifetimePoolAccessor = new LifetimeAccessor.ConstantAccessor(bean.source);
        } else if (bean.beanKind == BeanKind.CONTAINER) {
            DynamicAccessor da = bean.container.lifetime.pool.reserve(bean.beanClass);
            bean.lifetimePoolAccessor = da;
            bean.lifetimePoolAccessIndex = da.index();
        } else if (bean.beanKind == BeanKind.LAZY) {
            throw new UnsupportedOperationException();
        }

        // Only create an instance node if we have instances
        if (bean.sourceKind != BeanSourceKind.INSTANCE && bean.beanKind.hasInstances()) {
            bean.container.sm.injectionManager.addConsumer(bean.operations.get(0), bean.lifetimePoolAccessor);
        }

        // See also java.lang.PublicMethods

        // Introspect all fields on the bean and its super classes
        introspectAllFields(this, bean.beanClass);

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
                IntrospectedBeanMethod.introspectMethodForAnnotations(this, m);
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
                        IntrospectedBeanMethod.introspectMethodForAnnotations(this, m);
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
                    IntrospectedBeanMethod.introspectMethodForAnnotations(this, m);
                }
            }
        }
        // Should be empty... Maybe just an assert
        resolveOperations();

        bean.introspecting = null; // move up down?
        
        // Call into every BeanIntrospector and tell them it is all over
        for (Contributor e : extensions.values()) {
            e.introspector.onIntrospectionStop();
        }
    }

    private void introspectClass() {}

    /**
     * 
     */
    void resolveOperations() {
        for (OperationSetup operation = unBoundOperations.pollFirst(); operation != null; operation = unBoundOperations.pollFirst()) {
            resolveOperation(operation);
        }
    }

    void resolveOperation(OperationSetup operation) {
        // System.out.println(operation.target + " " + operation.bindings.length);
        // System.out.println(operation.type);
        for (int i = 0; i < operation.bindings.length; i++) {
            BindingSetup binding = operation.bindings[i];
            if (binding == null) {
                IntrospectedBeanParameter.resolveParameter(this, operation, i);
            }
        }
    }

    private static void introspectAllFields(IntrospectedBean introspector, Class<?> clazz) {
        // We never process classes in the "java.base" module.
        if (clazz.getModule() != IntrospectedBean.JAVA_BASE_MODULE) {

            // Recursively call into superclass, before processing own fields
            introspectAllFields(introspector, clazz.getSuperclass());

            // PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);

            // Iterate over all declared fields
            for (Field field : clazz.getDeclaredFields()) {
                IntrospectedBeanField.introspectFieldForAnnotations(introspector, field);
            }
        }
    }

    /**
     * An instance of this class is created per extension that participates in the introspection. The main purpose of the
     * class is to make sure that the extension points to the same bean introspector for the whole of the introspection.
     */
    public record Contributor(ExtensionSetup extension, BeanIntrospector introspector, boolean hasFullAccess) {}

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
        @SuppressWarnings("unused")
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
                // Hmm
                // return lookup;
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
}
