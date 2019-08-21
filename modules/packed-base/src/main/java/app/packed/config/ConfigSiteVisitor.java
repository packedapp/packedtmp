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

import app.packed.inject.Provide;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;

/**
 * While the {@link ConfigSite} class contains all common information about a configuration site, this visitor can be
 * used to get details that are specific to a specific type of a configuration site.
 */
// When we get Configuration-> rename to getLazy() or just lazyRead, lazy
public interface ConfigSiteVisitor {

    /**
     * Visits an annotated field that was the origin of a configuration action. For example, a service provided via a field
     * annotated with {@link Provide}.
     * 
     * @param configSite
     *            the configuration site
     * @param field
     *            the annotated field
     * @param annotation
     *            the annotation that resulted in the configuration
     */
    default void visitAnnotatedField(ConfigSite configSite, FieldDescriptor field, Annotation annotation) {}

    /**
     * Visits an annotated method that was the origin of a configuration action. For example, a service provided via a
     * method annotated with {@link Provide}.
     * 
     * @param configSite
     *            the configuration site
     * @param method
     *            the annotated method
     * @param annotation
     *            the annotation that resulted in the configuration
     */
    default void visitAnnotatedMethod(ConfigSite configSite, MethodDescriptor method, Annotation annotation) {}

    // for example, @ComponentScan -> @Install
    default void visitAnnotatedType(ConfigSite configSite, Class<?> type, Annotation annotation) {}

    /**
     * This method is visited whenever.
     * 
     * @param configSite
     *            the configuration site
     */
    default void visitCapturedStackFrame(ConfigSite configSite) {} // Always only the top one, we can always add a method a visitAllStackFrames

    //// Ahhh vi gemmer ikke noedvendig informationen, skal lige have fundet ud af hvordan det fungere
    // default void visitConfiguration(ConfigurationNode....)visitConfiguration
    //
    /**
     * Visits a unknown configuration site, for example, if the capturing of configuration sites has been disabled.
     * 
     * @param configSite
     *            the configuration site
     */
    default void visitUnknown(ConfigSite configSite) {}
}
// Look at ConfigurationSource in some closed project. Has a number of options as well
//// Maybe linenumber + column number both start and stop, possible for -1 indicating unknown..