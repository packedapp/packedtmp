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
package packed.internal.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.Bundle;
import app.packed.container.Extension;

/**
 * Uses an extension
 * 
 * 
 * @see Bundle
 * @see Extension
 * 
 */
// Skriver en warning, hvis man har en extension, hvor alle hooks bruger @UseExtension...
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented

// Extensions that have non-activating hooks must be installed before any components.
// Or use @UseExtensionLazily(SomeExtension.class) on the bundle
// taenker det kun er rigtige komponenter... Man kan f.eks. godt link andre containere...

// UseExtensionLazily /
public @interface LazyExtensionUsage {

    String[] optional() default {};

    Class<? extends Extension>[] value();
}
