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
package app.packed.lifetime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.AnnotatedMethodHook;

/**
 * Indicates that the annotated method should be invoked as part of the annotated bean's initialization.
 * <p>
 * Methods that use this annotation will operate within a {@link InitializationContext} which can be injected as per
 * usual.
 * <p>
 * If a bean throws an exception during initialization the bean will fail to be put into action. Depending on the type
 * of bean this might result in the bean's container not being initializable.
 * <p>
 * Methods (or fields) annotated with {@link Inject} on the same bean are always executed before methods annotated with
 * {@code OnInitialize}.
 * <p>
 * This annotation may only be used on beans that have an actual lifecycle. Using it on, for example, static beans will
 * fail with {@link UnavailableLifecycleException} when installing the bean.
 * <p>
 * Operations created using annotation are represented by a {@link LifecycleOperationMirror} in the mirror API.
 * <p>
 * If there are multiple methods using this annotation on a single bean with the same value of the {@link #order()}
 * attribute. The framework provides no guarantees about the invocation sequence of these methods.
 *
 * @see InitializationContext
 * @see Inject
 * @see OnStart
 * @see OnStop
 * @see LifecycleOperationMirror
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedMethodHook(allowInvoke = true, extension = BaseExtension.class)
public @interface OnInitialize {

    /**
     * This attribute can be used to control the order in which dependent beans are initialized in the same container
     * lifetime.
     * <p>
     * The default value is to invoke the method <strong>before</before> any dependent beans are initialized.
     * <p>
     * This attribute has no effect if the annotated bean is in its own bean lifetime.
     *
     * @return whether or not the annotated method should be run before or after dependent beans in the same lifetime are
     *         initialized.
     */
    LifecycleOrder order() default LifecycleOrder.BEFORE_DEPENDENCIES;
}
