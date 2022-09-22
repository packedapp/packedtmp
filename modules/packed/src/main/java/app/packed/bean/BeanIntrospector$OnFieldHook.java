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
package app.packed.bean;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.bean.BeanIntrospector.BeanElement;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.Variable;
import internal.app.packed.bean.introspection.PackedBeanField;

/**
 * This class represents a {@link Field} on a bean.
 * 
 * @see BeanExtensionPoint.FieldHook
 * @see BeanIntrospector#onField(BeanProcessor$BeanField)
 * 
 * @apiNote There are currently no support for obtaining a {@link VarHandle} for a field.
 */
public sealed interface BeanIntrospector$OnFieldHook extends BeanElement permits PackedBeanField {

    /** {@return an annotation reader for the field.} */
    BeanIntrospector$AnnotationReader annotations();

    /** {@return the underlying field.} */
    Field field();

    /**
     * Attempts to convert field to a {@link Key} or fails by throwing {@link BeanDefinitionException} if the field does not
     * represent a proper key.
     * <p>
     * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
     * key. As {@link BeanIntrospector$OnBindingHook} is typically used in cases where this would be needed.
     * 
     * @return a key representing the field
     * 
     * @throws BeanDefinitionException
     *             if the field does not represent a proper key
     */
    default Key<?> fieldToKey() {
        return Key.convertField(field());
    }

    /**
     * @return
     */
    default Set<Class<?>> hooks() {
        return Set.of();
    }

    /**
     * {@return the modifiers of the field.}
     * 
     * @see Field#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Field#getModifiers()}
     */
    int modifiers();

    /**
     * Creates a new operation that reads the field as specified by {@link Lookup#unreflectGetter(Field)}.
     * 
     * @param operator
     *            the bean that will invoke the operation. The operator must be defined in the same container (or in a
     *            parent container) as the bean that declares the field
     * @return an operation handle
     * @throws IllegalArgumentException
     *             if the specified operator is not a direct ancestor of the bean that declares the field
     */
    OperationHandle newGetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType);

    /**
     * @param operator
     * @param accessMode
     *            the access mode determines the operation type as exposed via the mirror api. However, extensions are free
     *            to use any access mode they want
     * @return
     * 
     * @see VarHandle#toMethodHandle(java.lang.invoke.VarHandle.AccessMode)
     * 
     * @apiNote there are currently no way to create more than 1 MethodHandle or VarHandle per operation. If this is needed
     *          at some point. We could take a varargs of access modes and then allow repeat calls to methodHandleNow. No
     *          matter what we must declare the invocation types when we create the operation, so we can check access before
     *          creating the actual operation
     */
    OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, VarHandle.AccessMode accessMode, InvocationType invocationType);

    /**
     * Creates a new operation that writes a field as specified by {@link Lookup#unreflectSetter(Field)}.
     * 
     * @return an operation configuration object
     */
    OperationHandle newSetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType);

    /**
     * {@return the underlying field represented as a {@code Variable}.}
     * 
     * @see Variable#ofField(Field)
     */
    Variable variable(); // mayby toVariable (kun hvis den fejler taenker jeg)
}

interface Zandbox {

    default int beanFieldId() {
        // IDeen er lidt at fields (og methods) har et unikt id...
        // Som man saa kan sammenligne med
        // Problemet er metoder med baade @ScheduleAtFixedRate og @ScheduleAtVariableRate
        // Maaske skal vi droppe Class<? extends Annotation> som parameter
        return 1;
    }

    // BeanInfo

    // Can only read stuff...
    // Then we can just passe it off to anyone
    // IDK know about usecases
    // BeanProcessor$BeanField unmodifiable();

    // Or maybe just rawVarHandle() on IOH
    //// Ideen var lidt at man kunne kalde den her metode for at faa extra
    // Varhandles hvis man havde angivet mere end en access mode
    default VarHandle varHandleOf(OperationHandle handle) {
        throw new UnsupportedOperationException();
    }
}