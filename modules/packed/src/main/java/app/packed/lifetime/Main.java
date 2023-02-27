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
package app.packed.lifetime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.AnnotatedMethodHook;

/**
 * Trying to build an application with more than a single method annotated with this annotation will fail with
 * {@link BuildException}.
 * <p>
 * Methods annotated with {@code @Main} must have a void return type.
 * <p>
 * If the application fails either at initialization time or startup time the annotated will not be invoked.
 * <p>
 * When the annotated method returns the container will automatically be stopped. If the annotated method fails with an
 * unhandled exception the container will automatically be shutdown with the exception being the cause.
 * <p>
 * Annotated methods will never be invoked more than once??? Well if we have some retry mechanism
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedMethodHook(allowInvoke = true, extension = BaseExtension.class)
public @interface Main {}
//A single method. Will be executed.
//and then shutdown container down again
//Panic if it fails???? or do we not wrap exception??? I think we wrap...
//We always wrap in container panic exception
