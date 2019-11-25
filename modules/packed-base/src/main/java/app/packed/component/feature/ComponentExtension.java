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
package app.packed.component.feature;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;

import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.service.InjectionException;

/**
 * An extension that provides basic functionality for installing components in a container.
 */
// Nahhhh, maaske et interface og saa direkte paa ContainerConfiguration...

// Kill this... den fungere ikke

final class ComponentExtension extends Extension {

    void addRule(ComponentRule rule) {

    }

    // Scans this package...
    // Se nederst i filen paa ComponentScan, som vi har droppet fordi der er rigeligt annoteringer....
    void scan() {}

    void scan(String... packages) {}

    // Alternative to ComponentScan
    void scanForInstall(Class<?>... classesInPackages) {}

    // De fungere ikke paa en type sikker maade
    // Forstaaet saadan at enhver nu kan vaeree parent for alle andre typer.
    // F.eks. kan vi ikke sige at actors kun kan have actor boern
    public void useParent() {

    }

    public <T> ComponentConfiguration<T> useParent(ComponentConfiguration<T> cc) {
        // Ideen er at alle nye componenter bruger den som parent, incl linked containers...

        // may useAsParent is better

        // useAsParent(install("Fucker"))
        // include extensions???
        return cc;
    }

    public ComponentConfiguration<Void> useParent(String folder) {
        // Folk vil tro det er en component med navnet man skal bruge...

        // addFolder()??????

        // LazyAdd Folder... if extensions do not install anything we shouldnt give a fck
        // useAsParent("Extensions")

        // FOlder -> component type = Void.class

        throw new UnsupportedOperationException();
    }
}

// install
// noget med Main, Entry points....
// Man kan f.eks. disable et Main.... EntryPointExtension....

// @Main skal jo pege et paa en eller anden extension...

// Selvfoelelig er det hele komponenter... Ogsaa scoped
// Vi skal ikke til at have flere scans...

// AllowRuntimeInstallationOfComponents();

// @Scoped
// @Install()

// Why export, Need to export
class ComponentRule {

    // What to disable
    // Where (which components)to Disable it?
    // What todo... warn. fail, ...

    protected final void disableMemberInjection(Class<? extends Member> memberType) {
        //// Det burde vaere noget paa component....
        // Ahh vi har instance of and types...
        // architecture().disable(Inject.class)
        // architecture().disable(Inject.class, Class<? extends Member> fieldOrMethod);
        // // Field, Method, Member.class

        // Kunne ogsaa lave en @Rules() man kunne smide paa bundles...
    }

    static ComponentRule disableAnnotatedMethodHook(Class<? extends Annotation> type, Class<?>... scannable) {
        // Fungere daarlig med hook groups....
        // Vi bliver noedt til at lave en ny...
        // Og det bliver nok ikke superlet at cache den...

        // disableAnnotatedMethodHook(Main.class, String
        throw new UnsupportedOperationException();
    }

    static ComponentRule disableAnnotatedMethodHook(Class<? extends Annotation> type, Object componentFilter) {
        // ComponentFilter
        // path
        // name
        // module
        // Bundle
        // ScannableType
        throw new UnsupportedOperationException();
    }
}

/**
 * Typically used together with component scanning to indicate that a class should be installed.
 * 
 * Unlike many other popular dependency injection frameworks. There are usually no requirements in Cake to use
 * <code>@Inject</code> annotations on the constructor or method that must have dependencies injected. However, in some
 * situations an annotation can be used for providing greater control over how dependencies are being injected.
 * <p>
 * One such example is if a dependency should only be injected if it is available. Injecting {@code null} instead of
 * throwing an {@link InjectionException}.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)

// Giver kun mening sammen ComponentScan....
@interface Install {

    Class<?> as() default Component.class; // @install(as = Actor.class)

    /**
     * The description of the component. The default value is the empty string, which means that no description will be set.
     *
     * @return the description of the component
     * @see ComponentConfiguration#setDescription(String)
     * @see Component#description()
     */
    String description() default "";

    /**
     * The name of the component. The default value is the empty string, which means that the container will automatically
     * generate a unique name for the component.
     *
     * @return the name of the component
     * @see ComponentConfiguration#setName(String)
     * @see Component#name()
     */
    String name() default "";

    boolean instantiable() default true;

}
/// **
// * Returns any children that should be installed for the component.
// *
// * @return any children that should be installed for the component
// */
// Class<?>[] children() default {};

// Would also solve our problems with mixin cycles.
// Cannot come up with any situations where you would reference
// from a component with a mixin to another component with a mixin
// Class<?>[] mixins() default {};
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
@Deprecated(since = "We don't want this as an annotation, but a method on ComponentExtension")
// Saa kan man ogsaa bruge lookup objecter. og alt mulgit
@interface ComponentScan {

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
    boolean accessInacessibleModules()

    default true;
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
