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
package app.packed.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.inject.Inject;
import app.packed.inject.InjectionContext;
import app.packed.state.sandbox.InstanceState;
import app.packed.state.sandbox.OnStop;

/**
 * An annotation used to indicate that a particular method should be invoked whenever the declaring entity reaches the
 * {@link InstanceState#INITIALIZING} state.
 * <p>
 * This annotation can, for example, be used on a method on a component instance:
 *
 * <pre>{@code  @OnInitialize
 * public void onInit() {
 *   System.out.println("This component is being initialized");
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
 * To find out exactly what kind of services that can be injected into an annotated method, an instance of an
 * {@link InjectionContext} can be used:
 *
 * <pre>{@code  @OnInitialize
 * public void onInit(InjectContext injector) {
 *   System.out.println("The following services can be injected into this method");
 *   injector.services().forEach(e -> System.out.println(e.getKey()));
 * }}
 * </pre>
 * <p>
 * If a method annotated with {@code @OnInitialize} throws an exception. The initialization of the entity will normally
 * fail, and the state of the entity change from {@link InstanceState#INITIALIZING} to {@link InstanceState#TERMINATED}.
 * <p>
 * The {@link Inject} annotation should never be used together with the {@link OnInitialize}, as this would mean the
 * method would be invoked twice, once in the entity's <b>injection</b> phase and once in the entity's
 * <b>initialization</b> phase.
 * 
 * @see OnStart
 * @see OnStop
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
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
    boolean preOrder() default true;

    // Maaske har vi en ENUM PRE_DEPENDENCIES, ASYNC, POST_DEPENDENCIES
}
