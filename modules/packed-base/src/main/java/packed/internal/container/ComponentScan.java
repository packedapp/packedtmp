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
package packed.internal.container;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.app.AppBundle;
import app.packed.component.Install;
import app.packed.container.Wirelet;

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

// 3 defaults
// ScanAll, ScanAllSameModule, ScanAllSamePackage
// ModulepathScan + ClasspathScan
/// Grunden til vi ikke kalder dem f.eks. ComponentScan er vi gerne ville kunne bruge dem til f.eks. jpa.Entitities...
// Eller andet, men hvor der er en context

// Skal vi smide nogle exceptions, hvis den ikke er paa en bundle????

// ScanModulePath
// @ScanModulepath(packages = "*") vs @ScanModulepath(packages = ".")

// @ComponentScan because @EntityScan is probably different....
/// I don't think it is reusable

// If the annotated bundle is on the modulepath -> ModulePath, if Bundle is on the classpath -> ClassPath
public @interface ComponentScan {

    /** Maybe we have special viral debug wirelets.... */
    // What + Where (Components Scan at /wewe/qw/er34/23
    public static final Wirelet DEBUG_INFO = null;

    // Vil maaske gerne ogsaa have @ListenTo paa et tidspunkt...
    // Problemet er hvad hvis

    // All med Type Components????
    // Tager kun Install
    // Kan man vaelge???? annotations= {Entity.class} <- we should have some kind of
    // repositor

    // Severity[FAIL, WARN, INFO?, IGNORE] onIn
    // Basically what happens when you encounter a module that is not open to packed...
    // But explicitly included....
    boolean accessInacessibleModules() default true;
    // Debug.run(new MyBundle());

    /**
     * Returns the annotations that will result in the annotated type being installed as a component. The default
     * implementation returns {@link Install}. Indicating that are all types annotated with {@link Install} will be
     * installed as a component.
     * 
     * @return the annotations that will result in a type being installed as a component.
     */
    Class<?>[] annotations() default { Install.class };

    /**
     * Returns a list of module names that will be included in the component scanning. The default value is "." which
     * indicates that only the module to which the annotated type or member belongs to is scanned. Module names are matched
     * using regular expressions via {@link String#matches(String)} (or similar methods).
     * <p>
     * If the type that this annotation is placed on is on the class path. The value of this annotation is ignored.
     * <p>
     * Returning an empty array indicates that we should scan all modules that are readable .Module names starting with
     * 'java.' or 'jdk.' are always ignored.
     * 
     * @return a list of module names that should be scanned
     */
    // RegExp are okay
    // Module.canOpen???? We skal jo ikke kunne scanne module vi ikke har adgang til. Selvom, Packed maaske har adgang til
    // dem....canRead er vel bare
    String[] modules() default { "." };

    /**
     * Returns a list of package names that will be included in the scanning.
     * 
     * @return a list of package names that will be included in the scanning
     */
    // Special "*" <- All packages, "." <- current package only
    String[] packages() default {};
}

//
/// **
// * Forces the runtime to scan the classpath, even if the annotated bundle is registered with modulepath. The default
// * value is <code>false</code> indicating that only the modulepath should be scanned if the bundle is defined on the
// * modulepath.
// *
// * @return whether or not to scan the classpath
// */
// boolean forceClasspathScan() default false;
//
/// **
// * Returns whether or not to scan the modulepath when looking for classes. The default value is <code>true</code>.
// *
// * @return whether or not to scan the modulepath
// */
// boolean forceModulepathScan() default false;
@ComponentScan(modules = { "*" })
class SomeBundle extends AppBundle {

    @Override
    protected void configure() {}

    public static void main(String[] args) {
        run(new SomeBundle());
    }
}