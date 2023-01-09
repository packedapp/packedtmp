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
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.bean.BeanHook.AnnotatedVariableHook;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;

/**
 *
 */
public interface ContextTemplate {

    /** {@return the context this template is a part of.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension this template is a part of.} */
    Class<? extends Extension<?>> extensionClass();

    /** {@return the type of arguments that must be provided.} */
    List<Class<?>> invocationArguments();

    /**
     * Creates a new context template, adding the specified argument type to the list of invocation arguments.
     * 
     * @param argument
     *            the argument type to add
     * @return the new context template
     */
    ContextTemplate withArgument(Class<?> argument);

    static ContextTemplate of(MethodHandles.Lookup lookup, Class<? extends Context<?>> contextClass, Class<?>... invocationArguments) {
        throw new UnsupportedOperationException();
    }

    /**
     * Move to operation template?
     */
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @AnnotatedVariableHook(extension = BaseExtension.class)
    public @interface InvocationContextArgument {
        Class<? extends Context<?>> context();

        int index() default 0;
    }
}
