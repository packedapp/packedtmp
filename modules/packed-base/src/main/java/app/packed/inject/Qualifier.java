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
package app.packed.inject;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation basically works identical to the Qualifier annotation in javax.inject. And both of them can be used
 * interchangeable.
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface Qualifier {}
// String value default "";
// If value starts with "/....." -> ComponentPath
// otherwise ordinary
// # topics, / <- component path + .. ./
// Why not also on Inject("xxxx") = Qualifier("xxxx");
// @Inject("/fff") = @Inject @Qualifier("/fff") = @Inject @Named("/fff")
// All qualifiers that does not start with an alphanumeric character are reserved.
// ascii characters that are not in the range of 0-9a-zA-Z are reserved for internal usage.
// And components cannot define qualifiers with it.
