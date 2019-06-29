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
package app.packed.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation that can be placed on annotations...
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
// FeatureAnnotation

// Unfprtunantely, you cannot register random annotations for use. As this would break encapsulation.
// ActivateExtension

// Stort problem. Nu har vi lige pludselig Runtime komponenter der bliver registeret naar en container allerede koerer.
/// F.eks. Actors faar lige pludselig en lifecycle annoterering...
///// Jup... Actors er rimelig dynamiske...

// Vi har vel i virkeligheden 3 interesante ting...
// Extension
// Online-Component
// Hook
// Online-Hook
public @interface ContainerExtensionActivator {

    /**
     * Returns the extension hook group that knows how do handle the types, fields or methods that are annotated...
     * 
     * @return the extension hook group that knows how do handle
     */
    Class<? extends ContainerExtensionHookGroup<?, ?>> value();
}