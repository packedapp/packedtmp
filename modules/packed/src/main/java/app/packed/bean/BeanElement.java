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

import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import app.packed.bean.BeanElement.BeanClass;
import app.packed.bean.BeanElement.BeanConstructor;
import app.packed.bean.BeanElement.BeanField;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bindings.BeanVariable;
import app.packed.bindings.Key;
import app.packed.bindings.Variable;
import app.packed.framework.AnnotationList;
import app.packed.lifetime.BeanLifetimeTemplate;
import app.packed.operation.BeanOperationTemplate;
import app.packed.operation.DelegatingOperationHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.bean.PackedBeanConstructor;
import internal.app.packed.bean.PackedBeanField;
import internal.app.packed.bean.PackedBeanMethod;

/**
 *
 */
public sealed interface BeanElement permits BeanClass, BeanField, BeanConstructor, BeanMethod, BeanVariable {

    /** {@return an annotation reader for the element.} */
    AnnotationList annotations();

    /**
     * @param postFix
     *            the message to include in the final message
     *
     * @throws BeanInstallationException
     *             always thrown
     */
    default void failWith(String message) {
        throw new BeanInstallationException(message);
    }

    /**
     * Returns the modifiers of the element.
     *
     * {@return the modifiers of the constructor}
     *
     * @see Class#getModifiers()
     * @see Field#getModifiers()
     * @see Constructor#getModifiers()
     * @see Method#getModifiers()
     * @see Parameter#getModifiers()
     */
    int modifiers(); // What about type parameters? return 0

    /**
     * Attempts to convert field to a {@link Key} or fails by throwing {@link KeyExceptio} if the field does not represent a
     * proper key.
     * <p>
     * This method will use the exact type of the field. And not attempt to peel away injection wrapper types such as
     * {@link Optional} before constructing the key. As a binding hook is typically used in cases where this would be
     * needed.
     *
     * @return a key representing the field
     *
     * @throws KeyException
     *             if the field does not represent a valid key
     */
    Key<?> toKey();

    // CheckRealmIsApplication
    // CheckRealmIsExtension
    /**
     *
     * <p>
     * Members from the {@code java.lang.Object} class are never returned.
     */
    public non-sealed interface BeanClass extends BeanElement {

        void forEachConstructor(Consumer<? super BeanConstructor> action);

        void forEachField(Consumer<? super BeanField> action);

        void forEachMethod(Consumer<? super BeanMethod> action);

        boolean hasFullAccess();

        // Fields first, include subclasses, ... blabla
        // Maybe on top of full access have boolean custom processing on ClassHook
        void setProcessingStrategy(Object strategy);

        // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
        // Masske har vi BeanClass.Builder() istedet for???

        // Cute men vi gider ikke supportere det
//       static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
//           throw new UnsupportedOperationException();
//       }
    }

    /**
     * This class represents a {@link Constructor} on a bean.
     * <p>
     * Unlike field and methods hooks. There are no way to define hooks on constructors. Instead they must be defined on a
     * bean driver or a bean class. Which determines how constructors are processed.
     */
    // Do we need a BeanExecutable??? Not sure we have a use case
    // Or maybe we just have BeanMethod (Problem with constructor() though)
    public sealed interface BeanConstructor extends BeanElement permits PackedBeanConstructor {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();

        // LifetimeTemplate??? Also available for on Method????
        OperationHandle newOperation(BeanOperationTemplate template);

        /** {@return a factory type for this method.} */
        OperationType operationType();
    }

    /**
     * This class represents a {@link Field} on a bean.
     *
     * @see BaseExtensionPoint.AnnotatedFieldHook
     * @see BeanIntrospector#hookOnAnnotatedField(BeanProcessor$BeanField)
     *
     * @apiNote There are currently no support for obtaining a {@link VarHandle} for a field.
     */
    public sealed interface BeanField extends BeanElement permits PackedBeanField {

        /** {@return the underlying field.} */
        Field field();

        /**
         * Creates a new operation that can read the underlying field.
         * <p>
         * If an {@link OperationMirror} is created for the operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         * @see Lookup#unreflectGetter(Field)
         */
        OperationHandle newGetOperation(BeanOperationTemplate template);

        // Unwrapped instaed???
        default BeanVariable newInjectOperation() {
            throw new UnsupportedOperationException();
        }

        /**
         * Creates a new operation that can read or/and write a field as specified by the provided access mode.
         * <p>
         * If an {@link OperationMirror} is created for this operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @param accessMode
         *            the access mode of the operation
         *
         * @return an operation handle
         *
         * @see VarHandle#toMethodHandle(java.lang.invoke.VarHandle.AccessMode)
         *
         * @apiNote There are currently no way to create more than one MethodHandle per operation (for example one for reading
         *          and one for writing a field). You must create an operation per access mode instead. It is also currently not
         *          possible to get a VarHandle for the field
         */
        OperationHandle newOperation(BeanOperationTemplate template, VarHandle.AccessMode accessMode);

        /**
         * Creates a new operation that can write to a field.
         * <p>
         * If an {@link OperationMirror} is created for this operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         *
         * @see Lookup#unreflectSetter(Field)
         */
        OperationHandle newSetOperation(BeanOperationTemplate template);

        /**
         * {@return the underlying field represented as a {@code Variable}.}
         *
         * @see Variable#ofField(Field)
         */
        Variable variable();
    }

    /**
     * This class represents a {@link Method} from which an {@link OperationHandle operation} can be created.
     */
    public sealed interface BeanMethod extends BeanElement permits PackedBeanMethod {

        /**
         * @return
         * @see AnnotatedMethodHook#allowInvoke()
         */
        boolean hasInvokeAccess();

        /** {@return the underlying method.} */
        Method method();

        /**
         * Creates a new delegating operation handle that allows an extension to delegate the invocation of the method to
         * another extension.
         *
         * @return the delegating operation handle
         */
        DelegatingOperationHandle newDelegatingOperation();

        // IDK if we need a separate one. Or this can be encoded in the BOT
        default OperationHandle newLifetimeOperation(BeanLifetimeTemplate template) {
            throw new UnsupportedOperationException();
        }

        /**
         * Creates a new operation that can invoke the underlying method.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         *
         * @throws InaccessibleBeanMemberException
         *             if the framework does not have access to invoke the method
         * @throws InternalExtensionException
         *             if the extension does not have access to invoke the method
         *
         * @see OperationTarget.OfMethodHandle
         * @see Lookup#unreflect(Method)
         * @see BeanMethodHook#allowInvoke()
         * @see BeanClassHook#allowFullPrivilegeAccess()
         */
        OperationHandle newOperation(BeanOperationTemplate template);

        /** {@return the default type of operation that will be created.} */
        OperationType operationType();
    }
}
