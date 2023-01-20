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

import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.extension.BaseExtension;
import app.packed.lifetime.RunState;

/**
 * This annotation indicates that the annotated method should be invoked as part of the bean's initialization.
 * <p>
 * This annotation can, for example, be used like this:
 *
 * <pre>{@code  @OnInitialize
 * public void onInit() {
 *   System.out.println("This bean is being initialized");
 * }}
 * </pre>
 * <p>
 * If the entity support dependency injection, the arguments of the method can automatically be dependency injected. For
 * example, a method on a component instance can have the container in which it is registered injected:
 *
 * <pre>{@code  @OnInitialize
 * public void onInit(Container container) {
 *   System.out.println("This component is registered with " + container.getName());
 * }}
 * </pre>
 * <p>
 * If a method annotated with {@code @OnInitialize} throws an exception. The initialization of the bean will normally
 * fail, and the state of the entity change from {@link RunState#INITIALIZING} to {@link RunState#TERMINATED}.
 * <p>
 * The {@link Inject} annotation should never be used together with the {@link OnInitialize}, as this would mean the
 * method would be invoked twice, once in the entity's <b>injection</b> phase and once in the entity's
 * <b>initialization</b> phase.
 * 
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
     * If the lifecycle enabled entities are ordered in some kind of hierarchy this attribute can be used to indicate
     * whether or not a particular method should be executed before any of its children and initialized or after all of its
     * children has been successfully initialized.
     * <p>
     * If the lifecycle enabled entities on which this annotation is placed does not make use of a hierarchy, the value of
     * this attribute is ignored
     * 
     * @return whether or not to run before children
     */
    // Meaning it will be visited before all dependencies...
    // Maaske har vi 3 -> PreOrder AnyOrder PostOrder...
    // Or PRE_DEPENDENCIES, ANY_TIME, POST_DEPENDENCIES;
    // Og saa scheduler vi automatisk til Pre_Dependencies
    // Ved ikke hvordan Async fungere fx med PRE_DEPENDENCIS

    // Vi skal have en Lifetime annotering istedet
    //// @ReverseOrderLifecycle
    // or just lifetimeOrderReversed default false();
    boolean preOrder() default true;

    // We supportere lokal order for bean.. Priority?
    // Bliver ikke super let at implementere
    int priorityOnBean() default 0;
}
