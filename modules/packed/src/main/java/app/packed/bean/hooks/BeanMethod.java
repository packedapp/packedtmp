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
package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.FactoryType;
import app.packed.operation.InjectableOperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTargetMirror;
import packed.internal.bean.hooks.PackedBeanMethod;

/**
 * Represents a method on a bean.
 * 
 * @see Extension#hookOnBeanMethod(BeanMethod)
 */
public sealed interface BeanMethod extends BeanElement permits PackedBeanMethod {

    /** {@return a factory type for this method.} */
    FactoryType factoryType();

    /**
     * {@return the modifiers of then underlying method.}F
     *
     * @see Method#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    int getModifiers();

    /**
     * @return
     */
    boolean hasInvokeAccess(); // isOperational?

    /** {@return the underlying method.} */
    Method method();

    /**
     * 
     * <p>
     * Any mirror created from the operation will have a {@link OperationTargetMirror.OfMethodInvoke} as the
     * {@link OperationMirror#target()}.
     * 
     * @param operator
     *            the extension bean that will invoke the operation
     * @return
     * @throws IllegalArgumentException
     *             if the specified extension bean is not located in the same container as the method's bean.
     */
    // YODO I think we allow descendents as well. But only in the same lifetime I think...
    // Altsaa fx for session. Skal vi jo have en instansp[ per container (session)
    // Saa en descent kan jo ikke vide bvad der er vhad
    InjectableOperationHandle newOperation(ExtensionBeanConfiguration<?> operator);

    /**
     * Returns a direct method handle to the {@link #method()} (without any intervening argument bindings or transformations
     * that may have been configured elsewhere).
     * 
     * @return a direct method handle to the underlying method
     * @see Lookup#unreflect(Method)
     * @see BeanMethodHook#allowInvoke()
     * @see BeanClassHook#allowAllAccess()
     * 
     * @throws UnsupportedOperationException
     *             if invocation access has not been granted via {@link BeanMethodHook#allowInvoke()} or
     *             BeanClassHook#allowAllAccess()
     */
//    RawOperationHandle<MethodHandle> newRawOperation();

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    // CaptureAnnotated
    public @interface AnnotatedWithHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanMethod#newOperation(ExtensionBeanConfiguration)} and... will fail with
         * {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanMethod#newOperation(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();

        // Altsaa vi har jo ikke lukket for at vi senere kan goere nogle andre ting...
        // Class<Supplier<? extends BeanMethod>> bootstrap();
    }
}
