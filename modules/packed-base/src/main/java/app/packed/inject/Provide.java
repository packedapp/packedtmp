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
import java.time.LocalDateTime;

import app.packed.base.OpenMode;
import app.packed.base.Opens;
import app.packed.container.ExtensionMember;
import app.packed.container.Packlet;
import app.packed.service.ServiceExtension;

/**
 * An annotation indicating that an annotated type, method or field provides a object of some kind. A field
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
 *  &#64;Provide
 *  String name = "left";
 *   
 *  &#64;Right
 *  &#64;Provide
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

// @Provides final Factory<Long> p = .... <-------------- COOL, havde helt glemt...

// @Component(name, description, children, ....)
// Taenker @Provides @Install(asChild=true)
// Taenker vi maaske venter med at implementere paa Type
// Men ville saa vaere rart at kunne sige @Provides(exportAs = MyInterface.class)

// Okay shutdown/cleanup ikke supportered paa many som er eksporteret som services...
// Maaske hvis man eksplicit, siger its managed....
// Maaske @Provide og @ProvidePrototype... @ProvideTemplate
// Det ville ogsaa hjaelpe paa sidecars maaske?? Eller de ignore den vel bare...

// Er ret sikker paa vi ikke kan smide det paa en type..???? IDK

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Packlet(extension = ServiceExtension.class)
@ExtensionMember(ServiceExtension.class)
@Opens(to = { OpenMode.METHOD_INVOKE, OpenMode.FIELD_SET })
public @interface Provide {

    /**
     * Returns a description of the service provided by the providing method, field or type.
     * <p>
     * The default value is the empty string, indicating that there is no description for the provided service
     * 
     * @return a description of the service provided by the providing method, field or type
     */
    // how does this relate to description on component??? description on component???
    // Same on both ->
    // Service 1, Component another -> each have different
    String description() default "";

    /**
     * Indicates whether or not the provided value is a constant.
     * <p>
     * Constant values are always eagerly constructed.
     * 
     * Constants may be cached by the runtime... I think we need to make some strong guarantees.
     * 
     * The provided does not have to be an constant in the way t. But from the perspectiv of the runtime. The instance
     * 
     * Constants may be cached
     * 
     * <p>
     * The default value is <code>false</code>
     * 
     * @return whether or not the provided value is a constant
     */
    boolean constant() default false;
}

class Us {

    // It is a bit of a wart with FactoryX. But very managable. We don't expect to many of them
    // Its an extract X<> + {}
    @Provide
    static final Factory<LocalDateTime> now = new Factory0<>(LocalDateTime::now) {};
}
