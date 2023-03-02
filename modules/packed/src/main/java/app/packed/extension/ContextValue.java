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
package app.packed.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.context.Context;
import app.packed.extension.BeanHook.AnnotatedBindingHook;

/**
 * Move to operation template?
 * <p>
 * Variable must be optional or available
 * Context mus
 * {@link app.packed.extension.operation.OperationHandle#contextValues()}
 * <p>
 * {@link app.packed.context.OutOfContextException} is thrown if attempting to use a context that is not available.
 * <p>
 * This annotation can only be used by extensions.
 */
@Target({ ElementType.TYPE_USE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedBindingHook(extension = BaseExtension.class)
// Maybe a more leet name
// Alternative @ConjectInject paa contexten...
public @interface ContextValue {

    /** {@return the context type that providing the value.} */
    Class<? extends Context<?>> value();
}