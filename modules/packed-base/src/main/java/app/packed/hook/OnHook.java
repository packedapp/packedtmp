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
package app.packed.hook;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation indicating that the method should be invoked when a hook is xxxx as detailed by the first parameter of
 * the method..
 * 
 * The first parameter of any method annotated with {@code @OnHook} must a subtype of Hook.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
// Attributes
// DisableNativeImage -> Don't write stuff to native image if graal... Don't know if need to decide this dynamically?
// Or dont know if there are any usecases

// ResolveMetaAnnotations -> Look in annotatations of annotations. (Fails, if a metaannotations has two annotations each
// using the same attribute, for example, runWith(X) and runWith(Y)

// ExactAssignableType() -> Instance of must be exact type boolean
//// Den passer ikke specielt godt ind. Den skal jo naeste have sine egen katagori saa.
// Fordi der er forskel hvis man kalder ExtensionDesciptor.assignableTo or ExtensionDesciptor.exactTypes

// ResolveMetaAnnotations is the only usefull one
public @interface OnHook {}
