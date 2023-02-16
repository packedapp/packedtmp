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
package app.packed.extension.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.AnnotatedBindingHook;

/**
 *
 * <p>
 * This annotation can only be used by {@link OperationHandle#operator()} of the underlying operation.
 */
@Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedBindingHook(extension = BaseExtension.class)
// Har jo ikke noget med Context at goere. Men er jo rart at vi kun er et argument til operationen.
// Og ikke skal gennem nogen steder

// Den er maaske brugbar alligevel. Det er svaert at sige der her
public @interface FromInvocationArgument {

    /**
     * If there are more than 1 invocation argument of the annotated target type. The exact index of the argument must be
     * specified.
     * <p>
     *
     * @return
     */
    int exactIndex() default -1;
}