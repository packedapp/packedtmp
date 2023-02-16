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
import java.lang.invoke.VarHandle;
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

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InaccessibleMemberException;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.operation.OperationTemplate;
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

    /** The app.packed.base module. */
    private static final Module APP_PACKED_BASE_MODULE = OpenClass.class.getModule();

    /** We {@code java.base} module, which we never process classes from. */
    private static final Module JAVA_BASE_MODULE = Object.class.getModule();

    /** A handle for invoking the protected method {@link BeanIntrospector#initialize()}. */
    private static final MethodHandle MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), BeanIntrospector.class,
            "initialize", void.class, BeanScannerExtension.class);

    private final OpenClass accessor;

    /** The bean that is being reflected upon. */
    public final BeanSetup bean;

    /** The bean class. */
    public final Class<?> beanClass;

    final Lookup customLookup;

    /** The various extensions that are part of the reflection process. */
    // We sort it in the end
    private final IdentityHashMap<Class<? extends Extension<?>>, BeanScannerExtension> extensions = new IdentityHashMap<>();

    /** The hook model for the bean. */
    final BeanHookModel hookModel;

    public final ArrayDeque<OperationSetup> unBoundOperations = new ArrayDeque<>();

    // Vi har lidt det her besvaer med at lukke ting in order her ogsaa.
    // Hvis man f.x. kalder OH.manuallyBindingable.bind(new Op0<ExoticExtension>(){})
    // Saa har vi jo lige pludseligt tilfoejet endnu en extension der skal koeres.
    // Og lukkes ned

    // Er det i virkeligheden user beans der er problemet og ikke extensionen selv?
    // Hvis extension installere sine egne beans er det jo kun dependencies.
    // Der skal koere efter extensionen...
    // Og den kan jo ogsaa kun bruge

    // Lad os sige man godt vil tilfoeje en fake WebRequest op
    // Jamen saa depender man jo paa WebRequest...
    // Saa fake operationer bliver resolved som om man selv ejer dem
    // Jaa, eller ogsaa skal de ikke resolves...
    // IDK, hvis jeg siger access til get some Field.
    // Skal man saa have rettigheder til at lave tilfoeje et WebRequest???

    // WebEP.addFakeOp(BeanIntrospector bi);
    // WebEP.addFakeOp(BeanHandle bi)

    boolean isConfigurable;

    BeanScanner(BeanSetup bean) {
        this.bean = bean;
        this.beanClass = bean.beanClass;
        this.customLookup = bean.container.assembly.customLookup;
        this.hookModel = bean.container.assembly.model.hookModel;
        this.accessor = new OpenClass(MethodHandles.lookup());
        // We need to make a copy of attachments, as the the map may be updated in the BeanInstaller
    }

    /**
     * @param extensionType
     * @param fullAccess
     * @return the contributor
     */
    BeanScannerExtension computeContributor(Class<? extends Extension<?>> extensionType) {
        return extensions.computeIfAbsent(extensionType, c -> {
            // Get the extension (installing it if necessary)
            ExtensionSetup extension = bean.container.useExtension(extensionType, null);

            // Create a new introspector
            BeanIntrospector introspector = extension.newBeanIntrospector();

            BeanScannerExtension bse = new BeanScannerExtension(this, extension, introspector);

            // Call BeanIntrospector#initialize(BeanScannerExtension)
            try {
                MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, bse);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }

            // Notify the bean introspector that it is being used
            introspector.beforeHooks();
            return bse;
        });
    }

    /** Find a constructor on the bean and create an operation for it. */
    private void findConstructor() {
        BeanScannerConstructor constructor = BeanScannerConstructor.CACHE.get(beanClass);

        Constructor<?> con = constructor.constructor();

        MethodHandle mh = unreflectConstructor(con);

        OperationTemplate ot;
        if (bean.lifetime.lifetimes().isEmpty()) {
            ot = OperationTemplate.defaults();
        } else {
            ot = bean.lifetime.lifetimes().get(0).template;
        }
        ot = ot.withReturnType(beanClass);

        OperationSetup os = new MemberOperationSetup(bean.installedBy, bean, constructor.operationType(), ot,
                new OperationConstructorTarget(constructor.constructor()), mh);
        bean.operations.add(os);
        resolveNow(os);
    }

    /** Introspect the bean. */
    void introspect() {
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

        if (!beanClass.isInterface()) {

            // If a we have a (instantiating) class source, we need to find a constructor we can use
            if (bean.beanSourceKind == BeanSourceKind.CLASS && bean.beanKind != BeanKind.STATIC) {
                findConstructor();
            }

            // See also java.lang.PublicMethods

            // Introspect all fields on the bean and its super classes
            introspectFields(this, beanClass);

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
                    PackedBeanMethod.introspectMethodForAnnotations(this, m);
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
                            PackedBeanMethod.introspectMethodForAnnotations(this, m);
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
                        PackedBeanMethod.introspectMethodForAnnotations(this, m);
                    }
                }
            }
        }
        // Should be empty... Maybe just an assert
        resolveOperations();

        // Call into every BeanIntrospector and tell them it is all over
        for (BeanScannerExtension e : extensions.values()) {
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

    MethodHandle unreflectConstructor(Constructor<?> constructor) {
        Lookup lookup = accessor.lookup(constructor);
        try {
            return lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }
    }

    /**
     * <p>
     *
     * @param field
     *            the field to unreflect
     * @see Lookup#unreflectGetter(Field)
     */
    MethodHandle unreflectGetter(Field field) {
        Lookup lookup = accessor.lookup(field);
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }
    }

    MethodHandle unreflectMethod(Method method) {
        Lookup lookup = accessor.lookup(method);
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }
    }

    MethodHandle unreflectSetter(Field field) {
        Lookup lookup = accessor.lookup(field);
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }
    }

    VarHandle unreflectVarHandle(Field field) {
        Lookup lookup = accessor.lookup(field);
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }
    }

    private static void introspectFields(BeanScanner introspector, Class<?> clazz) {
        // We never process classes in the "java.base" module.
        if (clazz.getModule() != BeanScanner.JAVA_BASE_MODULE) {
            // Recursively call into superclass, before processing own fields
            introspectFields(introspector, clazz.getSuperclass());

            // We are never going to sort here. It only makes sense
            // to sort fields that have hooks

            // Iterate over all declared fields
            for (Field field : clazz.getDeclaredFields()) {
                FieldScan.introspectField(introspector, field);
            }

            // Maybe store things directly in BeanScannerExtension
        }
    }

    /**
     * An open class is a thin wrapper for a single class and a {@link Lookup} object.
     */
    private final class OpenClass {

        /** A lookup object that can be used to access {@link #type}. */
        @SuppressWarnings("unused")
        private final MethodHandles.Lookup lookup;

        /** A lookup that can be used on non-public members. */
        private MethodHandles.Lookup privateLookup;

        OpenClass(MethodHandles.Lookup lookup) {
            this.lookup = requireNonNull(lookup);
        }

        Lookup lookup(Member member) {
            // If we already have a private lookup for the bean return it
            MethodHandles.Lookup lookup = privateLookup;
            if (lookup != null) {
                return lookup;
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

            String pckName = beanClass.getPackageName();
            Module beanModule = beanClass.getModule();

            // See if the bean's package is open to app.packed.base
            if (!beanModule.isOpen(pckName, APP_PACKED_BASE_MODULE)) {
                String otherModule = beanModule.getName();
                String thisModule = APP_PACKED_BASE_MODULE.getName();
                throw new InaccessibleMemberException("In order to access '" + StringFormatter.format(beanClass) + "', the module '" + otherModule
                        + "' must be open to '" + thisModule + "'. This can be done, for example, by adding 'opens " + pckName + " to " + thisModule
                        + ";' to the module-info.java file for " + otherModule);
            }

            // Should we use lookup.getdeclaringClass???
            APP_PACKED_BASE_MODULE.addReads(beanModule);

            // Create and cache a private lookup.
            try {
                // Fjernede lookup... Skal vitterligt have samlet det i en klasse
                return privateLookup = MethodHandles.privateLookupIn(beanClass, MethodHandles.lookup() /* lookup */);
            } catch (IllegalAccessException e) {
                throw new InaccessibleMemberException("Could not create private lookup [type=" + beanClass + ", Member = " + member + "]", e);
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
