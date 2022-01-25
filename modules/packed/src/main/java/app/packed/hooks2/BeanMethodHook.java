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
package app.packed.hooks2;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.extension.Extension;
import app.packed.hooks.BeanMethod;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface BeanMethodHook {

    /**
     * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
     * <p>
     * Methods such as {@link BeanMethod#methodHandle()} and... will fail with {@link UnsupportedOperationException} unless
     * the value of this attribute is {@code true}.
     * 
     * @return whether or not the implementation is allowed to invoke the target method
     * 
     * @see BeanMethod#methodHandle()
     */
    boolean allowInvoke() default false; // allowIntercept...

    @SuppressWarnings("rawtypes")
    Class<? extends Extension> extension() default Extension.class;
    
    Class<? extends Build> buildWith();
    
    abstract class Bootstrap extends Build {
        
        void buildWith(Class<? extends Build> buildWith) {
            
        }
    }
 
    // Taenker metoderne er public saa vi kan smide en instans rundt..
    // Fx til ServiceSupport.provide(BeanMethodHook.Build) <--- reserves usage, creates operation 
    abstract class Build {
        protected abstract void build();
    }
}


