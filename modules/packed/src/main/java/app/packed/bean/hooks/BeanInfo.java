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

import app.packed.application.Realm;
import app.packed.bean.BeanKind;
import app.packed.container.Extension;

/**
 *
 */
// int beanApplicationId();
// int beanContainerId();

// BeanDescriptor??? Vi har ogsaa ApplicationInfo (var ApplicationDescriptor) og ExtensionDescriptor

// Tror vi skal have defineret hvor vi skal bruge den fra...
// Er ikke sikker paa vi kan noejes med en


public interface BeanInfo {

    Class<?> beanClass();

    BeanKind beanKind();

    Class<? extends Extension<?>> operator();

    Realm owner();
}
