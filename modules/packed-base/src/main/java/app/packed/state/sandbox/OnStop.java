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
package app.packed.state.sandbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.ManagedInstance;
import app.packed.lifecycle.OnInitialize;
import app.packed.lifecycle.OnStart;

/**
 * An annotation used to indicate that a particular method should be invoked whenever the declaring entity reaches the
 * {@link InstanceState#STOPPING} state.
 * <p>
 * Static methods annotated with OnStop are ignore.
 *
 * @see OnInitialize
 * @see OnStart
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnStop {

    boolean async() default false;

    ManagedInstance.Mode[] mode() default {};

    ManagedInstance.Mode[] notMode() default {};

    boolean preOrder() default false;
}

// String[] before() default {};

// The only guarantee we make is that if an entity has transitioned to the starting state.
// OnStop will run...