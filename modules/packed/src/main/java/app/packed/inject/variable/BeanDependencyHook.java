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
package app.packed.inject.variable;

import app.packed.extension.Extension;

// On Annotation Type ->
// On Non-annotation Type -> Typed (raw)

// BeanProvideHook

// Not inherited

// InjectionHook
public @interface BeanDependencyHook {
    
    @SuppressWarnings("rawtypes")
    Class<? extends Extension> extension();
    
    // Bootstrap kan vaere optional... Saa skal det vaere en statisk @ScopedProvide paa selve annoteringen...
    Class<? extends BeanDependency> bootstrap() default BeanDependency.class;
}
