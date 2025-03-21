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
package app.packed.bean.sandbox;

/**
 *
 */
// Represents the bean after transformations...
// Tror maaske det er mere debug?? IDK
// Tror ikke det er super let at mappe
interface BeanClassMirror {

    Class<?> beanClass();

    // Has there been any transformations??
    boolean isTransformed();

}

// Transformed ->
// BeanProxy