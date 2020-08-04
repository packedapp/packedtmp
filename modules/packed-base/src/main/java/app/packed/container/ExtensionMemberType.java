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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a type is a member of an extension in some way. This annotation can be applied to. subclasses of
 * {@link Wirelet}. In which case the pipeline implementation can have an instance of the extension injected its
 * constructor.
 * <p>
 * Types that make use of this annotation must be located in the same module as the extension itself.
 */
@Target(ElementType.TYPE)
// @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented // Maybe just ExtensionMember
public @interface ExtensionMemberType {

    /**
     * Returns the extension the annotated type is a member of.
     * 
     * @return the extension the annotated type is a member of
     */
    Class<? extends Extension> value();
}
// Was @Inherited
// But if we have abstract classes as packlets, users subclass would have automatically become part of the extension

//Was UseExtension and MemberOfExtension
//try look here https://www.thesaurus.com/browse/member
// Wirelet (non-pipelined) Will be available for injection into any extension runtime component
// Pipelines -> The given extension must have been installed in order to use any wirelets that belong to the pipeline...
// Packlet -> Will install the given extension if not already installed
