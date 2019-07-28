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
package app.packed.hook2;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.InstanceOfHook;

/**
 * Hooks are used for callbacks. Methods annotated with this method must have exactly one parameter which is an instance
 * of either {@link AnnotatedFieldHook}, {@link AnnotatedMethodHook}, {@link AnnotatedTypeHook} or
 * {@link InstanceOfHook}.
 */
// Should also be able to take a Stream/List/Collection/Iterable
// Hvad hvis hvis vi bare tager en definition....
// Should we Allow Hook? matching every hook? or AnnontatedFieldHook<?> matching all field annotations

// Could allow mailbox'es for actors. Where we automatically transforms method invocations into
// We would need to have some way to indicate that some method invocation can be done without requring the result
// Maybe return Void to indicate sync and void as async?
// @Extension.ActivatorAnnotation(HooksExtension.class)
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface OnHook2 {
    @SuppressWarnings("rawtypes")
    Class<? extends HookCacheBuilder> value() default HookCacheBuilder.class;
}
