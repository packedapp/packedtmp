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
package app.packed.bean.lifecycle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.scanning.BeanTrigger.AnnotatedMethodBeanTrigger;
import app.packed.extension.BaseExtension;

/**
 * An annotation used to indicate that a particular method should be invoked whenever the declaring entity reaches the
 * {@link RunState#STOPPING} state.
 * <p>
 * Static methods annotated with OnStop are ignore.
 *
 * @see OnInitialize
 * @see OnStart
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedMethodBeanTrigger(allowInvoke = true, extension = BaseExtension.class)

// Some examples:
// https://stackoverflow.com/questions/26547532/how-to-shutdown-a-spring-boot-application-in-a-correct-way
//https://www.smilecdr.com/our-blog/the-pros-and-cons-of-spring-smartlifecycle

// Channels -> Notification: Notifiers friends and families about the pending shutdown
// Do the actual shutdown
// Notifaction again: Shit has been shutdown
public @interface Stop {

//    // What is the usecase?
//    boolean onlyOnApplicationStop() default false;

    boolean fork() default false;

    /**
     * <p>
     * Notice that the default ordering is the opposite of the ordering from {@link OnInitialize} and {@link OnStart}. By
     * default {@code OnStop} operations will be executed after on stop operations on dependencies.
     *
     * @return
     */
    LifecycleDependantOrder order() default LifecycleDependantOrder.AFTER_DEPENDANTS;

    // Timeout?
    public enum ForkPolicy {
        FORK, FORK_AWAIT_AFTER_DEPENDENCIES, NO_FORK;
    }

}
