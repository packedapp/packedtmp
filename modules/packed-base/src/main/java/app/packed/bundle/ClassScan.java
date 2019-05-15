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
package app.packed.bundle;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target(TYPE)
/**
 *
 */
// Can only be placed on a bundle..
// We need to scan every class.....

// I think the default should be only to scan the module you are in....
// And the allow Regexp for modules() so that you can specify "*" for all modules
// Maybe the same for packages....
// So default is all classes in the same package+module as the bundle
// @ClassScan < (

// Think we do a ModuleClassScan +
public @interface ClassScan {

    Class<?>[] annotations() default {}; // Kan man vaelge???? annotations= {Entity.class} <- we should have some kind of repositor

    /**
     * Returns whether or not to scan the classpath when looking for classes. The default value is <code>false</code>.
     * 
     * @return whether or not to scan the classpath
     */
    boolean includeClasspath() default false;

    /**
     * Returns whether or not to scan the modulepath when looking for classes. The default value is <code>true</code>.
     * 
     * @return whether or not to scan the modulepath
     */

    boolean includeModulepath() default false;

    /**
     * Returns a list of module names that will be included in the scanning.
     * 
     * @return a list of module names that will be included in the scanning
     */
    // RegExp are okay
    String[] modules() default {};

    /**
     * Returns a list of package names that will be included in the scanning.
     * 
     * @return a list of package names that will be included in the scanning
     */
    String[] packages() default {};

    // Severity[FAIL, WARN, INFO?, IGNORE] onIn
    // Basically what happens when you encounter a module that is not open to packed...3
    boolean accessInacessibleModules() default true;
}

@ClassScan
class SomeBundle extends Bundle {

    public static void main(String[] args) {
        run(new SomeBundle());
    }
}