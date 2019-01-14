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
package app.packed.config;

import java.lang.annotation.Annotation;

import app.packed.inject.Provides;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;

/**
 * While the {@link ConfigurationSite} class contains all common information about a configuration site, this visitor
 * can be used to get details that are specific to a specific type of a configuration site.
 * <p>
 * Unlike the configuration site class this
 */
public interface ConfigurationSiteVisitor {

    default void visitAnnotatedField(ConfigurationSite configurationSite, FieldDescriptor field, Annotation annotation) {}

    /**
     * Visits an annotated method that was the origin of a configuration action. For example, a service provided via a
     * method annotated with {@link Provides}.
     * 
     * @param configurationSite
     *            the configuration site
     * @param method
     *            the annotated method
     * @param annotation
     *            the annotation that resulted in the configuration
     */
    default void visitAnnotatedMethod(ConfigurationSite configurationSite, MethodDescriptor method, Annotation annotation) {}

    /**
     * This method is visited whenever.
     * 
     */
    default void visitTopStackFrame(ConfigurationSite configurationSite) {} // Always only the top one, we can always add a method a visitAllStackFrames

    //// Ahhh vi gemmer ikke noedvendig informationen, skal lige have fundet ud af hvordan det fungere
    // default void visitConfiguration(ConfigurationNode....)visitConfiguration
    default void visitUnknown() {}

    // Look at ConfigurationSource in some closed project. Has a number of options as well
}
