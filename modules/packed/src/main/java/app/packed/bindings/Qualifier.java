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
package app.packed.bindings;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares an annotation type to be a qualifier. Qualifier annotations allow unambiguously identify a suitable mapping
 * method in case several methods qualify to map a bean property,
 * 
 * Qualifiers are used to distinguish different objects of the same type.
 * <p>
 * This framework does not provide any facilities to dynamically apply qualifiers to annotations. For example, in order
 * to use annotations that cannot directly depend on this framework as qualifier annotations. Any annotation that needs
 * to act as a qualifier must be annotated with this annotation.
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface Qualifier {}

// Consumer<> validator

// dependency resolver, qualifier resolver,
// Default is Qualifier which indicates that no special resolver is resolver is used
// Only support static @Provides methods.... Then we avoid needing to think about how many instances we create...
// Class<?> resolver() default Injector.class;

// or provider
// QualifiedProvider

// Allow ignoring attributes? String[] ignoreAttributes() default {};