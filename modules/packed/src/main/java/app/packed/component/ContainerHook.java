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
package app.packed.component;

import app.packed.container.Assembly;

/**
 *
 */
public interface ContainerHook {

    @interface ContainerIs {

        String ifContainerInPath() default "*"; // Regexp on container path
        // Would it be nice to be able to do it with beans as well??
        // For example, if I cannot modify the bean. So like a " full path
        // Nah, I think just specifying the class would be fine

        String[] ifContainerTaggedWith() default {};


        boolean rootInApplicationOnly() default false;
        boolean rootInAssemblyOnly() default false;

        // Hmm Maybe we need an assembly hook... IDK
        Class<? extends Assembly>[] definedInAssemblyOfType() default {}; // Alternative have Class<Assembly> as default value
    }
}
