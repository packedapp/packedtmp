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
package app.packed.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method that can be applied extension
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExtensionCallback {
    boolean onInstantiation() default false;

    boolean onPreembleDone() default false;
}
// Must be nonstatic and parameter less????

//An extension cannot have more than one method for a given assembling event.
//@AfterExtension(Instantiated)
enum ExtensionState {

    /**
     * The extension has been successfully instantiated. But the extension instance has not yet been returned to the user.
     */
    INSTANTIATED,

    /** All components and extensions have been added and configured. */
    PREEMBLED_DONE,

    /**
     * Any child containers located in the same artifact will be has been defined. Typically using
     * {@link Bundle#link(Bundle, app.packed.container.Wirelet...)}.
     */
    CHILDREN_ADDING_FINISHED,

    /** */
    GUESTS_FINISHED;
}

//Container finished

//Child Linked
//Parent Linked
//NoParent Linked... //NoDirectLink

//Er det her vi linker paa tvaers af artifacts?????

//ExtensionBarrier....

//Kan sagtens tage statiske metoder..

//F.eks. en artifact finished....
