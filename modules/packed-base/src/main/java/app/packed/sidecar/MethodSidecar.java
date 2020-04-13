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
package app.packed.sidecar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodSidecar {

    /**
     * An sidecar lifecycle event that the sidecar has been successfully instantiated by the runtime. But the instance has
     * not yet been returned to the user.
     */
    String INSTANTIATION = "Instantiation";

    // Will only be available in the sidecar, not any of its runtime representations
    boolean injectDirectMethodHandle() default false; // Can also be used for consts....

    Class<?>[] methodConst(); // MD og evt. DirectMH

    //// Conditional installs a... Nah hvad med en annotation paa classen???
    // Class<Predicate<? extends MethodDescriptor>>

    // Problemet er lidt DirectMethodHandle... hvis vi ikke skal bruge den...

    // Vi vil gerne kombinere const og predicated..
    // F.eks. vi gider ikke extract ting i baade et predicate og saa igen i en const
}
