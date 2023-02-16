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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.AnnotatedMethodHook;
import app.packed.lifetime.LifetimeOrder;

/**
 * Indicates that the annotated method should be executed as part of the target bean's initialization.
 * <p>
 * The method can any kind of dependencies available for the bean.
 * <p>
 * If the entity support dependency injection, the arguments of the method can automatically be dependency injected. For
 * example, a method on a component instance can have the container in which it is registered injected:
 *
 * <pre>{@code  @OnInitialize
 * public void onInit() {
 *   System.out.println("This bean is being initialized");
 * }}
 * </pre>
 * <p>
 * If a method annotated with {@code @OnInitialize} throws an exception the bean will fail to be put into service.
 * Depending on the type of bean this might result in the bean's container not being initializable.
 * <p>
 * Methods (or fields) annotated with {@link Inject} are always executed before methods annotated with
 * {@code OnInitialize} (on the same bean).
 * <p>
 * If this annotation is used on bean that does not have a lifecycle {@link UnavailableLifecycleException} being thrown
 * when installing the bean.
 *
 * @see Inject
 * @see OnStart
 * @see OnStop
 *
 * @see BeanLifecycleOperationMirror
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedMethodHook(allowInvoke = true, extension = BaseExtension.class)
public @interface OnInitialize {

    /**
     * @return whether or not the annotated method should be run before or after dependencies in the same lifetime are
     *         initialized.
     */
    LifetimeOrder order() default LifetimeOrder.BEFORE_DEPENDENCIES;
}

// int priorityOnBean() default 0;