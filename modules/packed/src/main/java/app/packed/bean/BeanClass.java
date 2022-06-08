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
package app.packed.bean;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import app.packed.bean.BeanScanner.BeanElement;
import app.packed.container.Extension;

/**
 *
 * <p>
 * Members from the {@code java.lang.Object} class are never returned.
 */

// Kig maaske i Maurizio Mirror thingy...
public non-sealed interface BeanClass extends BeanElement {

    boolean hasAllAccess();
    
    void forEachConstructor(Consumer<? super BeanConstructor> m);
    
    void forEachMethod(Consumer<? super BeanMethod> m);

    // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
    // Masske har vi BeanClass.Builder() istedet for???

    // Cute men vi gider ikke supportere det
//    static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
//        throw new UnsupportedOperationException();
//    }
    
    
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowAllAccess() default false;

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();
    }
}
