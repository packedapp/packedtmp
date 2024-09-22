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

import app.packed.bean.BeanTrigger.AnnotatedMethodBeanTrigger;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationDependencyOrder;

/**
 * Indicates that the annotated method should be invoked as part of the targeted bean's initialization.
 * <p>
 * Like most other operations annotated methods Methods that use this annotation will operate within a
 * {@link InitializationContext} which can be injected as per usual.
 * <p>
 * Any values returned by a annotated method are always ignored by the runtime.
 * <p>
 * If an annotated methods throws an exception during initialization, the bean will fail to be put into action.
 * Depending on the lifetime of the bean, this may result in the application being unable to be initialized.
 * <p>
 * Methods (or fields) annotated with {@link Inject} on the same bean are always executed before methods annotated with
 * {@code OnInitialize}.
 * <p>
 * Attempting install a bean (that uses this annotation) that do not have a lifecycle, for example, a static bean. Will
 * fail with {@link UnavailableLifecycleException}.
 * <p>
 * Any method using this annotated will be represented by a {@link LifecycleOperationMirror} with
 * {@link RunState#INITIALIZING} in the {@link app.packed.bean.BeanMirror#operations() bean's mirror API}.
 * <p>
 * If multiple methods uses this annotation on the same bean and have the same value for the {@link #order()} attribute.
 * The framework may choose to invoke them in any sequence.
 *
 * @see Inject
 * @see OnStart
 * @see OnStop
 * @see LifecycleOperationMirror
 */
// I don't think we have a Context??? What can it specifically do???
// runAfterLifetimeInitialization, runAfterApplicationInitialize();

// How do we say I want to run this after the application has finished installing
// Could have both application and bean as lifetime()
// I don't we support this.. We shouldn't start loose threads here...
// Maybe we will have an extension escape hatch to implement this

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedMethodBeanTrigger(allowInvoke = true, extension = BaseExtension.class)
public @interface OnInitialize {

    /**
     * Controls the order in which beans in the same lifetime that depend on each other are initialized.
     * <p>
     * The default order is to invoke the annotated method <strong>before</strong> any other beans that depends on the
     * targeted bean are initialized.
     * <p>
     * This attribute has no effect if no other beans depend on the targeted bean.
     *
     * @return whether or not the annotated method should be run before or after dependent beans in the same lifetime are
     *         initialized.
     */
    OperationDependencyOrder order() default OperationDependencyOrder.BEFORE_DEPENDENCIES;
}
