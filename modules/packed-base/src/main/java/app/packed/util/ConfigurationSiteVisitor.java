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
package app.packed.util;

import java.lang.annotation.Annotation;

/**
 *
 */
interface ConfigurationSiteVisitor {

    default void visitAnnotatedMethod(ConfigurationSite configurationSite, MethodDescriptor method, Class<? extends Annotation> annotation) {}

    default void visitAnnotatedField(ConfigurationSite configurationSite, FieldDescriptor method, Class<? extends Annotation> annotation) {}

    default void visitStackTrace() {} // Always only the top one?????

    //// Ahhh vi gemmer ikke noedvendig informationen, skal lige have fundet ud af hvordan det fungere
    // default void visitConfiguration(ConfigurationNode....)visitConfiguration
    default void visitUnknown() {}

    // Look at ConfigurationSource in some closed project. Has a number of options as well
}
