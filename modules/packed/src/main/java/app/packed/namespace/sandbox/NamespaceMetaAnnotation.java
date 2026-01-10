/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.namespace.sandbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates than a particular annotation operates within a namespace.
 * <p>
 * This annotation is solely used for informational purposes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
// Maybe a nested annotation on Namespace

// Ideen er vist lidt at markere annoteringer der arbejder inde for et namespace...
// Og maaske implicit vil skabe et.. Hvad med inject? Laver vel ikke noedvendig et service namespace
// Det er vel bare provide

// Eller ogsaa er det til at aendre namespacet on usage
public @interface NamespaceMetaAnnotation {
    String DEFAULT_NAMESPACE = "main";
}
