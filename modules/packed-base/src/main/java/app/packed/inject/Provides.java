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
package app.packed.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation indicating that the method or field provides a service.
 * <p>
 * The annotation can be used on both static and non-static fields and methods. However, if you use a non-static field
 * or method you implicitly introduces a dependency to an instance of the type on which the field or method is located.
 * This is normally not a problem, however in some situations it can lead to circles in the dependency graph.
 * <p>
 * A field
 * 
 * 
 * Or a final field.
 * 
 * Using this annotation on non-final fields are not supported. Eller.... hvad er forskellen paa at expose en metode der
 * return non-final-field;.... Maaske skriv noget med volatile og multiple threads
 * 
 * <p>
 * Both fields and methods can make used of qualifiers to specify the exact key they are made available under. For
 * example, given to two qualifier annotations: {@code @Left} and {@code @Right}<pre>
 *  &#64;Left
 *  &#64;Provides
 *  String name = "left";
 *   
 *  &#64;Right
 *  &#64;Provides
 *  String provide() {
 *      return "right";
 *  }
 *  </pre>
 * 
 * The field will is made available with the key {@code Key<@Left String>} while the method will be made available under
 * the key {@code Key<@Right String>}.
 * <p>
 * The objects provided by fields and methods are <b>never</b> injected. This must be done manually if needed, for
 * example, via <pre> 
 *   &#64;Provides
 *   public SomeObject provide(String name, Injector i) {
 *       SomeObject o = new SomeObject(name);
 *       i.injectMembers(o, MethodHandles.lookup());
 *       return o;
 *   }
 * </pre>
 * <p>
 * Placing You cannot register a service that have anything but singleton scope
 * 
 * place this annotation on objects that do not have
 * 
 * <p>
 * // Null is only a valid result, if the dependency is optional. Otherwise it is a failure
 * 
 * Or do we????
 * 
 */

// Hvordan fungere den med asNone() metoden????? Der er jo ingen grund til at instantiere objekter...hvid vi ikke
// behoever
// saa registerer den som singleton bliver den lavet ligegyldigt hvad.
// Lazy bliver den lavet hvis der er behov den...
// Okay, lazy can godt have @Provides alligevel.
// Hvis felterne er statisk paa en lazy, bliver lazy aldrig lavet?!!?!?!
// Test med en constructor der smider en AssertionError

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Provides {

    /**
     * The binding mode of the providing method or field, the default is {@link BindingMode#SINGLETON}.
     * 
     * @return the binding mode
     */
    BindingMode bindingMode() default BindingMode.SINGLETON;

    /**
     * Returns a description of the service provided by the annotated method or field.
     *
     * @return a description of the service provided by the annotated method or field
     */
    String description() default "";

    /**
     * Returns the tags of the service.
     * 
     * @return the tags of the service
     * @see ServiceDescriptor#tags()
     */
    String[] tags() default {};
}
