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
package app.packed.component.guest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.scanning.BeanTrigger.OnAnnotatedVariable;
import internal.app.packed.extension.BaseExtensionBeanintrospector;

/**
 * Can be used to annotated injectable parameters into a guest bean.
 *
 * @see ComponentHostContext
 * @see OnComponentGuestLifecycle
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedVariable(introspector = BaseExtensionBeanintrospector.class, requiresContext = ComponentHostContext.class)
// Den bliver ikke resolvet som context service. Saa der er ingen problemer med fx ApplicationMirror
public @interface FromGuest {}
