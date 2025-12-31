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
package app.packed.bean.sandbox;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import app.packed.bean.BeanInstallationException;
import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.AnnotationList;

/**
 *
 */

// Ja.... Hmm
// Lad os lege lidt med den.
//

// Altsaa det gode ville jo vaere hvis vi har en descriptiv beskrivelse af hvordan annoteringerne virker...

public interface NewIntrospector<E extends Extension<E>> {

    // Ved ikke om den er bedre. Problemet er at annoteringen ikke har class initializers

    // Alternativt kan man angive en HandleClass
    // ala @Inject(handleClass = xxx);
    // handleClass static {registerAnnotatedMethod(Lookup.lookup, Inject.class).install(x, c). }


    static void testMethodAnnotation(NewIntrospector<BaseExtension> ni) {
//        ni.onAnnotatedMethod(Initialize.class).install(null, i -> {
//            BeanInitializeOperationHandle h = new BeanInitializeOperationHandle(i, null);
//            // hack.bean.operations.addHandle(h);
//            return h;
//        });

    }

    <T extends Annotation> OnAnnotatedField<E, T> onAnnotatedField(Class<T> annotationType);

    <T extends Annotation> OnAnnotatedMethod<E, T> onAnnotatedMethod(Class<T> annotationType);

    // Must be a repeatable annotation
    <T extends Annotation> OnAnnotatedMethod<E, List<T>> onAnnotatedMethodArray(Class<T> annotationType);

    // Ideen er lidt fx at kunne saette Path
    <T extends Annotation> OnAnnotatedMethod<E, T> onAnnotatedMethodMarker(Class<T> annotationType);

    // Primary and secondaries
    OnAnnotatedMethod<E, AnnotationList> onAnnotatedMethodAny(Class<?>... annotationTypes);

    interface OnAnnotatedField<E extends Extension<E>, T extends Annotation> {
        T annotation();

        OnAnnotatedField<E, T> allowStaticField();

        // Men saa fikser vi jo templaten, ved ikke om vi kan have forskellige templates
        //void install(OperationTemplate template, Consumer<? super OnAnnotatedField<E, T>> action);
    }

    // Smart med at extende OperationInstaller fungere ikke rigtig med fields
    // Da den har 3 metoder
    interface OnAnnotatedMethod<E extends Extension<E>, T> extends OperationInstaller {

        T value();


        /**
         * {@return a list of annotations on the method}
         *
         * @see Method#getAnnotations()
         **/
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
         * {@return whether or not this bean method has invoke access to the underlying method}
         *
         * @see AnnotatedMethodHook#allowInvoke()
         */
        boolean hasInvokeAccess();

        /** {@return the underlying method (if the method is not synthetic).} */
        // Prob not in version 1. What if we just removed an annotation????
        // Probably need these in OperationTarget as well:(
        Optional<Method> method();

        /**
         * {@return the modifiers of the method}
         *
         * @see Method#getModifiers()
         */
        int modifiers();

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
        OperationInstaller newOperation();

        /** {@return the default type of operation that will be created.} */
        OperationType operationType();

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

        OnAnnotatedMethod<E, T> allowStaticField();

        <H extends OperationHandle<?>> H install(OperationTemplate template, Function<? super OnAnnotatedMethod<E, T>, H> factory);
    }

}
