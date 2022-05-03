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
package packed.internal.bean.operation.configsite;

import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * A visitor for the various {@link ConfigSite config sites} that are supported. This visitor can be used to get details
 * about a specific type of a configuration site.
 */
public interface ConfigSiteVisitor {

    /**
     * Visits a config site created from an annotated field.
     * 
     * @param configSite
     *            the configuration site
     * @param field
     *            the annotated field
     * @param annotation
     *            the annotation value
     */
    default void visitAnnotatedField(ConfigSite configSite, Field field, Annotation annotation) {}

    /**
     * Visits a config site created from an annotated method.
     * 
     * @param configSite
     *            the configuration site
     * @param method
     *            the annotated method
     * @param annotation
     *            the annotation value
     */
    default void visitAnnotatedMethod(ConfigSite configSite, Method method, Annotation annotation) {}

    /**
     * Visits a config site created from an annotated type.
     * 
     * @param configSite
     *            the configuration site
     * @param type
     *            the annotated type
     * @param annotation
     *            the annotation value
     */
    default void visitAnnotatedType(ConfigSite configSite, Class<?> type, Annotation annotation) {}

    /**
     * Visits a config site created by capturing the top stack frame.
     * 
     * @param configSite
     *            the configuration site
     * @param stackFrame
     *            the top stack frame
     */
    default void visitStackFrame(ConfigSite configSite, StackFrame stackFrame) {}

    /**
     * Visits an unknown configuration site, for example, if stack frame capturing has been disabled.
     * 
     * @param configSite
     *            the configuration site
     */
    default void visitUnknown(ConfigSite configSite) {}

    // Tror maaske vi har en actual class istedet for column/row
    default void visitFile(Path path, int column, int row) {
        // visitURI(path.toUri());
    }

    // default void visitURI(Path path, );
}
// Tilfoeje nogle default formatting config sites

// When we get Configuration-> rename to getLazy() or just lazyRead, lazy

//// Ahhh vi gemmer ikke noedvendig informationen, skal lige have fundet ud af hvordan det fungere
// default void visitConfiguration(ConfigurationNode....)visitConfiguration
//
// Look at ConfigurationSource in some closed project. Has a number of options as well
//// Maybe linenumber + column number both start and stop, possible for -1 indicating unknown..