/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.bean.scanning;

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
import java.util.IdentityHashMap;

import app.packed.bean.BeanSourceKind;
import app.packed.bean.InaccessibleBeanMemberException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.StringFormatter;

/**
 * This class represents a single bean being introspected.
 */
public final class BeanScanner {

    /** The app.packed.base module, we will never scan classes in this module. */
    private static final Module APP_PACKED_BASE_MODULE = BeanScanner.class.getModule();

    /** We {@code java.base} module, which we never process classes from. */
    static final Module JAVA_BASE_MODULE = Object.class.getModule();

    private final OpenClass accessor;

    /** The bean that is being reflected upon. */
    public final BeanSetup bean;

    /** The bean class. */
    public final Class<?> beanClass;

    final Lookup customLookup;

    /** The various bean introspectors that have been encountered. */
    // We sort it in the end
    private final IdentityHashMap<BeanIntrospectorClassModel, BeanIntrospectorSetup> introspectors = new IdentityHashMap<>();

    boolean isConfigurable;

    /** The trigger model for the bean. */
    final BeanTriggerModel triggerModel;

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

    public final ArrayDeque<OperationSetup> unBoundOperations = new ArrayDeque<>();

    public BeanScanner(BeanSetup bean) {
        this.bean = bean;
        this.beanClass = bean.bean.beanClass;
        this.customLookup = bean.container.assembly.customLookup;
        this.triggerModel = new BeanTriggerModelDefaults();
        this.accessor = new OpenClass(MethodHandles.lookup());
        bean.scanner = this;
    }

    /** Introspect the bean. */
    public void introspect() {
        introspectClass();

        // If the bean is created using an Op, we need to resolve it as the first thing
        if (bean.bean.beanSourceKind == BeanSourceKind.OP) {
            resolveBindings(bean.operations.first());
        }

        // What if the op creates an interface?
        if (!beanClass.isInterface()) {
            // Find the constructor if needed
            BeanScannerOnConstructors.findConstructor(this, beanClass);

            // Introspect all fields on the bean and its super classes
            BeanScannerOnFields.fiendIntrospect(this, beanClass);

            // Introspect all methods on the bean and its super classes
            BeanScannerOnMethods.introspect(this, beanClass);

        }

        resolveOperations();

        // Call into every BeanIntrospector and tell them it is all over
        for (BeanIntrospectorSetup introspector : introspectors.values()) {
            introspector.introspector.onStop();
        }
    }

    private void introspectClass() {}

    /**
     * Returns a introspector for the specified introspector model.
     * @param model
     *            the introspector model
     * @return the introspector
     */
    BeanIntrospectorSetup introspector(BeanIntrospectorClassModel model) {
        return introspectors.computeIfAbsent(model, im -> BeanIntrospectorSetup.create(this, im));
    }

    void resolveBindings(OperationSetup operation) {
        for (int i = 0; i < operation.bindings.length; i++) {
            BindingSetup binding = operation.bindings[i];
            if (binding == null) {
                BeanScannerOnVariable.resolveVariable(this, operation, operation.type.parameter(i), i);
            }
        }
    }

    /**
     *
     */
    private void resolveOperations() {
        for (OperationSetup operation = unBoundOperations.pollFirst(); operation != null; operation = unBoundOperations.pollFirst()) {
            resolveBindings(operation);
        }
    }

    MethodHandle unreflectConstructor(Constructor<?> constructor) {
        Lookup lookup = accessor.lookup(constructor);
        try {
            return lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
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
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }
    }

    MethodHandle unreflectMethod(Method method) {
        Lookup lookup = accessor.lookup(method);
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }
    }

    MethodHandle unreflectSetter(Field field) {
        Lookup lookup = accessor.lookup(field);
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }
    }

    VarHandle unreflectVarHandle(Field field) {
        Lookup lookup = accessor.lookup(field);
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
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

        private OpenClass(MethodHandles.Lookup lookup) {
            this.lookup = requireNonNull(lookup);
        }

        private Lookup lookup(Member member) {
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
                throw new InaccessibleBeanMemberException("In order to access '" + StringFormatter.format(beanClass) + "', the module '" + otherModule
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
                throw new InaccessibleBeanMemberException("Could not create private lookup [type=" + beanClass + ", Member = " + member + "]", e);
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
