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
package app.packed.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanHook.AnnotatedFieldHook;
import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.extension.BaseExtension;

/**
 * Ideen er at provide context fra en bean. Typisk container brug.
 *
 * Ved ikke hvordan vi ellers skal provide context info. Fra en non-lifetime
 * root container. Hvor vi ikke tager argument med
 * <p>
 * Tror ikke man baade kan specificere context arguments og bruge denne annotering.
 * <p>
 * Also
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedMethodHook(extension = BaseExtension.class, allowInvoke = true)
@AnnotatedFieldHook(extension = BaseExtension.class, allowGet = true)
public @interface ContextProvider {
    Class<? extends Context<?>> context(); // context.extension must be identical to owner

    int argumentIndex();
}
