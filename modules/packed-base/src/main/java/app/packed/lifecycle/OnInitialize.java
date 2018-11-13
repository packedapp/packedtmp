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
import app.packed.inject.Injector;

/**
 * A method annotation that can be used to indicate that a method must be executed <b>after</b> the method's declaring
 * entity has transitioned to the {@link LifecycleState#INITIALIZING} state. But before it transitions to the
 * {@link LifecycleState#INITIALIZED} state.
 * <p>
 * This annotation can, for example, be used on method on a component instance, such as:
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
 *
 * You should never use the {@link Inject} annotation together with the {@link OnInitialize}, as this would mean the
 * method would be invoked twice, once in the entity's <b>injection</b> phase and once in the entity's
 * <b>initialization</b> phase.
 * <p>
 * To find out exactly what kind of services that can be injected into an annotated method use an {@link Injector}.
 *
 * <pre>{@code  @OnInitialize
 * public void onInit(Injector injector) {
 *   System.out.println("The following services can be injected into this method");
 *   injector.printServices();
 * }}
 * </pre>
 * <p>
 * If a method annotated with {@code @OnInitialize} throws an exception. The initialization of the entity will normally
 * fail, and the state of the entity change from {@link LifecycleState#INITIALIZING} to {@link LifecycleState#STOPPING}.
 *
 * @see OnStart
 * @see OnStop
 */
// TODO update example with injector.printServices()
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnInitialize {}
