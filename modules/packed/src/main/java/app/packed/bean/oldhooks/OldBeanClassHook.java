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
package app.packed.bean.oldhooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A class hook allows for runtime
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface OldBeanClassHook {

    // Maybe we allow injection of a Lookup object.
    // Eller ogsaa har vi metoderne direkte paa Bootstrap. Jaa
    // Tror ikke bootstrap supportere injection af noget som helst...
    // Alt er jo allerede bestemt

    // Tror ikke vi kan bruge lookup. Vi har noget @OpenForAll annoterings vaerk.
    // Og den vil lookup aldrig kunne forstaa.

    boolean allowAllAccess() default false;

    // Hvordan passer den med ConstructorHook???
    // boolean allowInstantiate() default false; <-- allows custom instantiation

    /** The hook's {@link OldBeanClass} class. */
    Class<? extends OldBeanClass> bootstrap();

    /**
     * Returns annotations
     * 
     * @return
     */
    Class<? extends Annotation>[] matchesAnnotation() default {};

    Class<?>[] matchesAssignableTo() default {};

    // Tror det er noget med vi kan filtere fields/constructor/method/...
    public interface MemberOption {

        public static OldBeanClassHook.MemberOption declaredOnly() {
            throw new UnsupportedOperationException();
        }
    }
}
// matchesAssignableTo was Inherited, Extending... men 