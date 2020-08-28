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
package packed.internal.sidecar.old;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OldMethodSidecar {

    /**
     * Returns any runtime sidecar representations.
     * 
     * @return any runtime sidecar representations
     */
    Class<?>[] runtime() default {};

    /**
     * An sidecar lifecycle event that the sidecar has been successfully instantiated by the runtime. But the instance has
     * not yet been returned to the user.
     */
    String INSTANTIATION = "Instantiation";

    /**
     * Applies to the target entity not the sidecar. Cannot be used together with an invoker
     * 
     * @return a state expression
     */
    // onSidecarTarget
    // Sidecar must have a lifecycle otherwise the annot will fail...
    String invokeOnSidecarTarget() default ""; // ExceptionHandler??? @HandleException on the sidecar??? @AfterInvoke...

    // Will only be available in the sidecar, not any of its runtime representations (why not)

    /**
     * Whether or not a direct method handle should be available for injection. If set to <code>true</code> the method
     * handle will be available to both injection into the sidecar and for sidecar bootstrap methods.
     * 
     * @return make a direct method handle to the underlying available
     */
    boolean injectDirectMethodHandle() default false;
}

// Alternative en bootstrap @Const statisk metode...
// Class<?>[] methodConst() default {}; // MD og evt. DirectMH

//// Conditional installs a... Nah hvad med en annotation paa classen???
//// Kan lave noget a.la.  
// Class<Predicate<? extends MethodDescriptor>>

// @BootStrapMethod(cancelIfNull = true)
// Object o return null if !!! 

// Problemet er lidt DirectMethodHandle... hvis vi ikke skal bruge den...

// Vi vil gerne kombinere const og predicated..
// F.eks. vi gider ikke extract ting i baade et predicate og saa igen i en const
