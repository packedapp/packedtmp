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
import java.util.IdentityHashMap;
import java.util.Map;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.framework.devtools.PackedDevToolsIntegration;
import internal.app.packed.operation.OperationMemberTarget.OperationConstructorTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.ThrowableUtil;

/**
 * This class represents a single bean being introspected.
 */
public final class BeanScanner {

    /** We never process classes that are located in the {@code java.base} module. */
    static final Module JAVA_BASE_MODULE = Object.class.getModule();

    /** A handle for invoking the protected method {@link BeanIntrospector#initialize()}. */
    private static final MethodHandle MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), BeanIntrospector.class,
            "initialize", void.class, OperationalExtension.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class,
            "newBeanIntrospector", BeanIntrospector.class);

    /** An internal lookup object. */
    private static final MethodHandles.Lookup PACKED = MethodHandles.lookup();

    @Nullable
    public Map<Class<?>, Object> attachments;

    /** The bean that is being introspected. */
    public final BeanSetup bean;

    /** Non-null if a introspector was set via {@link BeanHandle.BeanInstaller#introspectWith(BeanIntrospector)}. */
    @Nullable
    private final BeanIntrospector beanIntrospector;

    /** Every extension that is activated by a hook. */
    // We sort it in the end
    private final IdentityHashMap<Class<? extends Extension<?>>, OperationalExtension> extensions = new IdentityHashMap<>();

    final BeanHookModel hookModel;

    // Should be made lazily??? I think
    // I think we embed once we gotten rid of use cases outside of this introspector
    final OpenClass oc;

    public final ArrayDeque<OperationSetup> unBoundOperations = new ArrayDeque<>();

    BeanScanner(BeanSetup bean, @Nullable BeanIntrospector beanIntrospector, @Nullable Map<Class<?>, Object> attachments) {
        this.bean = bean;
        this.hookModel = bean.container.assembly.assemblyModel.hookModel;
        this.beanIntrospector = beanIntrospector;
        this.oc = new OpenClass(PACKED, bean.beanClass);
        // We need to make a copy of attachments, as the the map may be updated in the BeanInstaller
        this.attachments = attachments == null ? null : new HashMap<>(attachments);
    }

    /**
     * @param extensionType
     * @param fullAccess
     * @return the contributor
     */
    OperationalExtension computeContributor(Class<? extends Extension<?>> extensionType) {
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
            OperationalExtension ce = new OperationalExtension(this, extension, introspector);

            // Call BeanIntrospector#initialize
            try {
                MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, ce);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }

            // Notify the bean introspector that it is being used
            introspector.beforeHooks();
            return ce;
        });
    }

    /** Find a constructor on the bean and create an operation for it. */
    private void findConstructor() {
        BeanScannerConstructor constructor = BeanScannerConstructor.CACHE.get(bean.beanClass);

        Constructor<?> con = constructor.constructor();

        Lookup lookup = oc.lookup(con);
        MethodHandle mh;
        try {
            mh = lookup.unreflectConstructor(constructor.constructor());
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        OperationTemplate ot;
        if (bean.lifetime.lifetimes().isEmpty()) {
            ot = OperationTemplate.defaults();
        } else {
            ot = bean.lifetime.lifetimes().get(0).template;
        }
        ot = ot.withReturnType(bean.beanClass);

        OperationSetup os = new MemberOperationSetup(bean.installedBy, bean, OperationType.ofExecutable(con), ot,
                new OperationConstructorTarget(constructor.constructor()), mh);
        bean.operations.add(os);
        resolveNow(os);
    }

    /** Introspect the bean. */
    void introspect() {
        bean.introspecting = this;
        // First, we process all annotations on the class

        // Can we add operations here???
        // In which case findConstructor while probably place its constructor on index!=0
        // We could use an ArrayDeque and use addFirst
        introspectClass();

        // We always have instances if we have an op.
        // Make sure the op is resolved
        if (bean.beanSourceKind == BeanSourceKind.OP) {
            resolveNow(bean.operations.get(0));
        }

        if (!bean.beanClass.isInterface()) {

            // If a we have a (instantiating) class source, we need to find a constructor we can use
            if (bean.beanSourceKind == BeanSourceKind.CLASS && bean.beanKind.hasInstances()) {
                findConstructor();
            }

            // See also java.lang.PublicMethods

            // Introspect all fields on the bean and its super classes
            introspectFields(this, bean.beanClass);

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
                    PackedOperationalMethod.introspectMethodForAnnotations(this, m);
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
                            PackedOperationalMethod.introspectMethodForAnnotations(this, m);
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
                        PackedOperationalMethod.introspectMethodForAnnotations(this, m);
                    }
                }
            }
        }
        // Should be empty... Maybe just an assert
        resolveOperations();

        bean.introspecting = null; // move up down?

        // Call into every BeanIntrospector and tell them it is all over
        for (OperationalExtension e : extensions.values()) {
            e.introspector.afterHooks();
        }
    }

    private void introspectClass() {}

    public void resolveNow(OperationSetup operation) {
        for (int i = 0; i < operation.bindings.length; i++) {
            BindingSetup binding = operation.bindings[i];
            if (binding == null) {
                BeanScannerBindingResolver.resolveBinding(this, operation, i);
            }
        }
    }

    /**
     * 
     */
    void resolveOperations() {
        for (OperationSetup operation = unBoundOperations.pollFirst(); operation != null; operation = unBoundOperations.pollFirst()) {
            resolveNow(operation);
        }
    }

    private static void introspectFields(BeanScanner introspector, Class<?> clazz) {
        // We never process classes in the "java.base" module.
        if (clazz.getModule() != BeanScanner.JAVA_BASE_MODULE) {
            // Recursively call into superclass, before processing own fields
            introspectFields(introspector, clazz.getSuperclass());

            // PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);

            // Iterate over all declared fields
            for (Field field : clazz.getDeclaredFields()) {
                FieldScan.introspectFieldForAnnotations(introspector, field);
            }
        }
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
