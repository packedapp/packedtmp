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
package xxx;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import app.packed.bundle.BundleDescriptor;

@Retention(RUNTIME)
/**
 *
 */

// Ahh, syntes sgu ikke vi skal smide de ting i hoveded paa bruger 99% af dem kommer aldrig til at lave deres egen
// descriptor....
@Inherited
@interface BundleSpec {
    String description() default "";

    String[] tags() default {};

    // Runtime Type
    // Factory Type

    Class<? extends BundleDescriptor> descriptorType() default BundleDescriptor.class;
    // DescriptorType
}
