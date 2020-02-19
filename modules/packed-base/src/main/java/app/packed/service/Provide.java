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
package app.packed.service;

import static app.packed.base.invoke.OpenMode.GET_FIELD;
import static app.packed.base.invoke.OpenMode.INVOKE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.base.invoke.OpensFor;
import app.packed.container.UseExtension;

/**
 * An annotation indicating that an annotated type, method or field provides a service of some kind. A field
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
 * Injection is never performed on any objects provided by a annotated field or method is <b>never</b> injected. This
 * must be done manually if needed, for example, via <pre> 
 *   &#64;Provides
 *   public SomeObject provide(String name, Injector i) {
 *       SomeObject o = new SomeObject(name);
 *       i.injectMembers(o, MethodHandles.lookup());
 *       return o;
 *   }
 * </pre>
 * <p>
 * The annotation can be used on both static and non-static fields and methods. However, if you use a non-static field
 * or method you implicitly introduces a dependency to the instance of the type on which the field or method is located.
 * This is normally not a problem, however in some situations it can lead to circles in the dependency graph.
 * <p>
 * 
 * If using
 * <p>
 * Proving a null value, for example, via a null field or by returning null from a method is illegal unless the
 * dependency is optional.
 */
// @Provides(description = "Current time) final Provider<LocalDate> p = ....
// @Provides final Factory<Long> p = ....

// @Component(name, description, children, ....)
// Taenker @Provides @Install(asChild=true)
// Taenker vi maaske venter med at implementere paa Type
// Men ville saa vaere rart at kunne sige @Provides(exportAs = MyInterface.class)

// Okay shutdown/cleanup ikke supportered paa many som er eksporteret som services...
// Maaske hvis man eksplicit, siger its managed....
// Maaske @Provide og @ProvidePrototype... @ProvideTemplate
// Det ville ogsaa hjaelpe paa sidecars maaske?? Eller de ignore den vel bare...

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@UseExtension(ServiceExtension.class)
@OpensFor({ INVOKE, GET_FIELD })
public @interface Provide {

    /**
     * Returns a description of the service provided by the providing method, field or type.
     * <p>
     * The default value is the empty string, indicating that there is no description for the provided service
     * 
     * @return a description of the service provided by the providing method, field or type
     * @see ServiceComponentConfiguration#setDescription(String)
     * @see ServiceDescriptor#description()
     */
    // how does this relate to description on component??? description on component???
    // Same on both ->
    // Service 1, Component another -> each have different
    String description() default "";

    /**
     * The instantiation mode of the providing method or field, the default is {@link ServiceMode#SINGLETON}.
     * 
     * @return the binding mode
     */
    ServiceMode instantionMode() default ServiceMode.SINGLETON;
}
