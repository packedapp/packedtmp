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
package app.packed.build.hook;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used on assemblies or bean classes
 */
// What about extensions???
@Target( ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// Inheritable?????
public @interface ApplyBuildHook {

    // I think we have hooks (instead of value), because we hope to have functions in annotations some day
    Class<? extends BuildHook>[] hooks();
}

// Alternativ we have two annotations
/// @BeanDefinition
/// @AssemblyDefinition

// @BeanDefinition(name="asdasd")

// Grunden til jeg ikke taenker er den rigtige løsning er at jeg tænker vi kommer til at lave meta (hook) annoteringer...