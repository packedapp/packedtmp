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

import app.packed.base.Key;
import app.packed.bean.BeanIntrospector.BeanElement;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.OperationCustomizer;
import app.packed.operation.Variable;
import internal.app.packed.bean.hooks.PackedBeanField;

/**
 * This class represents a {@link Field} on a bean.
 * 
 * @see BeanExtensionPoint.FieldHook
 * @see BeanIntrospector#onField(BeanProcessor$BeanField)
 */
public sealed interface BeanIntrospector$BeanField extends BeanElement permits PackedBeanField {

    /** {@return the underlying field.} */
    Field field();

    /**
     * Attempts to convert field to a {@link Key} or fails by throwing {@link BeanDefinitionException} if the field does not
     * represent a proper key.
     * <p>
     * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
     * key. As {@link BeanIntrospector$BeanDependency} is typically used in cases where this would be needed.
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
     * {@return the modifiers of the field.}
     * 
     * @see Field#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Field#getModifiers()}
     */
    int getModifiers();

    /**
     * Creates a new operation that reads a field as specified by {@link Lookup#unreflectGetter(Field)}.
     * 
     * @param operator
     *            the bean that will invoke the operation. The operator must be defined in the same container (or in a
     *            parent container) as the bean that declares the field
     * @return an operation configuration object
     * @throws IllegalArgumentException
     *             if the specified operator is not a direct ancestor of the bean that declares the field
     */
    OperationCustomizer newGetOperation(ExtensionBeanConfiguration<?> operator);

    /**
     * @param operator
     * @param accessMode
     * @return
     * 
     * @see VarHandle#toMethodHandle(java.lang.invoke.VarHandle.AccessMode)
     * 
     * @apiNote there are currently no way to create more than 1 MethodHandle or VarHandle per operation. If this is needed
     *          at some point. We could take a varargs of access modes and then allow repeat calls to methodHandleNow. No
     *          matter what we must declare the invocation types when we create the operation, so we can check access before
     *          creating the actual operation
     */
    // Tror man maa lave 2 operationer hvis man behov for det. JUC bruger aldrig mere end 1 VarHandle. Men det er jo ogsaa
    // fordi
    // de altid bare laesaer den volatile vaerdi. Saa de har aldrig nogle gettere
    // Har stadig ikke en usecase for 2 VarHandle. Men get plus set, er dog ikke sikker paa det er noget vi vil supportere
    OperationCustomizer newOperation(ExtensionBeanConfiguration<?> operator, VarHandle.AccessMode accessMode);

    /**
     * Creates a new operation that writes a field as specified by {@link Lookup#unreflectSetter(Field)}.
     * 
     * @return an operation configuration object
     */
    OperationCustomizer newSetOperation(ExtensionBeanConfiguration<?> operator);

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
    //BeanProcessor$BeanField unmodifiable();

    // Or maybe just rawVarHandle() on IOH
    //// Ideen var lidt at man kunne kalde den her metode for at faa extra
    // Varhandles hvis man havde angivet mere end en access mode
    default VarHandle varHandleOf(OperationCustomizer handle) {
        throw new UnsupportedOperationException();
    }
}