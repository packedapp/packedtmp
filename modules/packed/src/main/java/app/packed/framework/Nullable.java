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
package app.packed.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that the annotated element may be null under some circumstances.
 * <p>
 * Packed uses this annotation on every method in this framework that potentially returns {@code null}. It also uses
 * this annotation on every parameter where {@code null} is a valid argument.
 * <p>
 * This annotation is also frequently used in regards to dependency injection.
 * <ul>
 * <li><b>On a parameter,</b> to indicate that injection of the parameter is optional, and {@code null} should be
 * injected in case the dependency can not be resolved.</li>
 * <li><b>On a field,</b> to indicate that field should not be injected if the dependency could not be
 * resolved.<b>NOTE:</b>If the injection of a field is optional and the dependency could not be resolved. The initial
 * value of the field is never changed. This is useful, for example, to declare a default value in case the dependency
 * is missing.</li>
 * </ul>
 */
// * <li><b>On a type parameter,</b> {@link ServiceDependency#fromTypeVariable(Class, Class, int)} and
// * {@link ServiceDependency#fromTypeVariables(Class, Class, int...)} will acknowledges the annotation and mark the
// dependency
// * as optional. The same behavior can be observed by the dependencies on {@link Factory1} and {@link Factory2}.
// https://github.com/uber/NullAway
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE_USE)
@Documented
public @interface Nullable {}
