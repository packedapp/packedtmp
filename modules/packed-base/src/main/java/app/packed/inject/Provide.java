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
import java.util.Optional;

import app.packed.container.ExtensionAnnotation;
import app.packed.util.Key;

/**
 * An annotation indicating that the annotated type, method or field provides a service of some kind. A field
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
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
// @Provides(description = "Current time) final Provider<LocalDate> p = ....
// @Provides final Factory<Long> p = ....

// @Component(name, description, children, ....)
// Taenker @Provides @Install(asChild=true)
// Taenker vi maaske venter med at implementere paa Type
// Men ville saa vaere rart at kunne sige @Provides(exportAs = MyInterface.class)

// Okay shutdown/cleanup ikke supportered paa many som er eksporteret som services...
// Maaske hvis man eksplicit, siger its managed....
@ExtensionAnnotation(InjectorExtension.class)
public @interface Provide {

    boolean prototype() default false;

    /**
     * So ServiceConfiguration.as overrides or adds.... hmmm
     * 
     * @return stuff
     */
    // Use case en klasse, der implementere 2 interfaces....
    // provide(BigClass).as(AManager.class).as(BManager.class)

    // install(X)
    // Ingen annoterering -> intet
    // Annoterering -> default som X + Qualifier. ServiceConfiguration.as() override alle
    // Problemer f.eks. Description som vi jo gerne vil have forskellige....
    // Her har vi brug for alias... <-
    // alias(doo).as
    // eller en private @Provide X foo() {return this;} paa klassen
    // Vi har ikke brug for alias!
    Class<?> as() default Optional.class; // Defaults.class

    /**
     * Returns a description of the service provided by the providing method, field or type.
     * <p>
     * The default value is the empty string, indicating that there is no description for the provided service
     * 
     * @return a description of the service provided by the providing method, field or type
     * @see ProvidedComponentConfiguration#setDescription(String)
     * @see ServiceDescriptor#description()
     */
    // how does this relate to description on component??? description on component???
    // Same on both ->
    // Service 1, Component another -> each have different
    String description() default "";

    /**
     * Indicates that the service should be exported from any bundle that it is registered in. This method can be used
     * to.... to avoid having to export it from the bundle.
     * 
     * <p>
     * The service is always exported out under the same key as it is registered under internally in the bundle. If you wish
     * to export it out under another you key you can use {@link #exportAs()}. If you need to export the service out under a
     * key that uses a qualifier or a generic type. There is no way out of having to it manually
     * 
     * to use another key, the service must be explicitly exported, for example, using Bundle#exportService().
     * <p>
     * The default value is {@code false}, indicating that the provided service is not exported.
     * 
     * @return whether or not the provided service should be exported
     */
    boolean export() default false;

    // Maybe we do not want this...
    // Class<?> exportAs(); Only way to specify generic type or Qualifier is manually... Do we remove any qualifier?????
    // exportAs overrides Qualifiers og generic information. identical to calling as(MyInterface.class)
    // Essential a export(this).as(SomeInterface.class)
    Class<?> exportAs() default Object.class;

    /**
     * The instantiation mode of the providing method or field, the default is {@link InstantiationMode#SINGLETON}.
     * 
     * @return the binding mode
     */
    InstantiationMode instantionMode() default InstantiationMode.SINGLETON;

    /**
     * Returns the tags of the service.
     * 
     * @return the tags of the service
     */
    String[] tags() default {};

    // exportedKey = Class<? Supplier<Key>>??? ///
}

// Basically ServiceConfiguration
// Will override any annotations.
// Will only be
// Add debug???
//// notifier(Consumer<String> )

// InjectionExtension.setDefaultAnnotationProcessor..

// Ideen er @Provides(configurator = MyProvideAnnotationOption.class)
abstract class ProvideAnnotationOption {

    // final MethodMirror annotatedField();
    // final MethodMirror annotatedMethod();
    // final Class<?> annotatedClass();

    // Or just take a ServiceConfiguration:)
    abstract void configure();

    // Ahh er problemet her, generiske typer???
    // kan ikke koere cc.as(SomeKey) paa ServiceConfiguration<?>
    // abstract void configure(ServiceConfiguration<?> cc);

    final void export(Key<?> key) {}
}

// Set default options on a module
// @Require