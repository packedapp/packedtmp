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
package app.packed.block;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be used on {@link Extension} subclasses. However, if the extension uses any other extensions
 * this annotation must be used to indicate which extensions it may use.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
// ExtensionDependencies er vi vel tilbage i....
// transitive... Altsaa kan vi forstille os at extensions of extension skal bruge dem...
// Was ExtensionSettings
//SubtensionUse

// Altsaa kommer vi til at have mere her???
public @interface ExtensionSetup {

    /**
     * Other extensions that the annotation extension may make use of. This need not include transitive dependencies
     * (dependencies of dependencies). Only extensions that are directly used, for example, via
     * {@link ExtensionConfiguration#use(Class)}.
     * 
     * @return extensions that the extension may use
     */
    // Should we use Sub instead??? Giver god mening.. Da man ikke kan depend paa extensions der ikke har en sub
    Class<? extends Extension>[] dependencies() default {};

    /**
     * Other extensions that an extension may use if they are present on the classpath or modulepath.
     * <p>
     * The extension types will only be used if they can be resolved at runtime using
     * {@link Class#forName(String, boolean, ClassLoader)} or a similar mechanism.
     * <p>
     * Checking whether or not an optional dependency is available is done exactly once when the extension is first used.
     * Caching the result for future usage.
     * 
     * @return extensions that the extension may use if they are present on the classpath or modulepath
     */
    String[] optionalDependencies() default {};
}
