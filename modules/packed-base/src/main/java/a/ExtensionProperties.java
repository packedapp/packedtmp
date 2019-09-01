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
package a;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.packed.component.ComponentExtension;
import app.packed.container.extension.Extension;
import app.packed.inject.InjectionExtension;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionProperties {

    // Det er primaert for at kunne dokumentere det.... Vi kan jo laese det direkte af Extension + Sidecars
    Class<? extends Annotation>[] hookAnnotatedFields() default {};

    // Checks for
    /**
     * 
     * Packed keeps track of usage of extensions from other extensions. This is needed in order to precisely control the
     * order of callback methods to each extension. For example, an extension might call another extension from its
     * Extension#onConfigured() method. However, if we maintain no order, that extension might already believe it has been
     * fully configured.
     * 
     * call
     * 
     * @return any extensions this extensions requires
     */

    // Strictly we only need to define not packed extensions...
    // Because we can order the extensions ourself...
    Class<? extends Extension>[] dependencies() default {};

    /**
     * Either dependencies that might only be available on the classpath optionally. Or dependencies that
     * 
     * @return stuff
     */
    String[] optionalDependencies() default {};

    /**
     * Only sidecars listed here
     * 
     * @return stuff
     */
    // Why list them??? Only reason is to avoid situations.

    Class<?>[] sidecars() default {};

    // Alternativ til newPipeline paa Wirelet
    // Hvordan styrer vi de forskellige.
    Class<?>[] pipelines() default {};

    // Must be enabled if people want the full monty
    // Instance Hooks...
    boolean requireComponentInstances() default false;
}

@ExtensionProperties(dependencies = { InjectionExtension.class, ComponentExtension.class })
class MyExtension extends Extension {

}

// lookupSidecar <- will lookup

/////////// Big decisions
// V1
// Allow Artifact sidecars

// V2
// Do not allow artifact sidecars
