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
package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.extension.Extension;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
//https://stackoverflow.com/questions/4797465/difference-between-hooks-and-abstract-methods-in-java

//@Deprecated
public @interface OldBeanMethodHook {

    /**
     * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
     * <p>
     * Methods such as {@link OldBeanMethod#methodHandle()} and... will fail with {@link UnsupportedOperationException} unless
     * the value of this attribute is {@code true}.
     * 
     * @return whether or not the implementation is allowed to invoke the target method
     * 
     * @see OldBeanMethod#methodHandle()
     */
    // maybe just invokable = true, idk og saa Field.gettable and settable
    boolean allowInvoke() default false; // allowIntercept...

    /** Bootstrap classes for this hook. */
    Class<? extends OldBeanMethod> bootstrap();
    
    @SuppressWarnings("rawtypes")
    Class<? extends Extension> extension() default Extension.class;
}