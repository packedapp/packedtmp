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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.lang.Nullable;
import app.packed.service.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.inject.util.InjectConfigSiteOperations;

/**
 * An extension that provides basic functionality for installing components in a container.
 */
public final class ComponentExtension extends Extension {

    /** The configuration of the container, should only be accessed via {@link #pcc()}. */
    @Nullable
    private PackedContainerConfiguration pcc;

    /** Should never be initialized by users. */
    ComponentExtension() {}

    void addRule(ComponentRule rule) {

    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * <p>
     * This method uses the {@link ServiceExtension} to instantiate the an instance of the component. (only if there are
     * dependencies???)
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    // Den eneste grund for at de her metoder ikke er paa ComponentConfiguration er actors
    // Eller i andre situation hvor man ikke vil have at man installere alm componenter..
    // Men okay. Maaske skal man wrappe det saa. Det er jo let nok at simulere med useParent
    public <T> ComponentConfiguration<T> install(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return pcc().install(Factory.findInjectable(implementation), captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * This method uses the {@link ServiceExtension} to instantiate an component instance from the factory.
     * 
     * @param <T>
     *            the type of the component
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BaseBundle#install(Factory)
     */
    public <T> ComponentConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return pcc().install(factory, captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see BaseBundle#installInstance(Object)
     */
    public <T> ComponentConfiguration<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        return pcc().installInstance(instance, captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /**
     * Installs a component that does not have any instance representing it.
     * <p>
     * This method uses the {@link ServiceExtension}.
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public <T> ComponentConfiguration<T> installStatic(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return pcc().installStatic(implementation, captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /**
     * Returns the container configuration that this extension wraps.
     * 
     * @return the container configuration that this extension wraps
     * @throws IllegalStateException
     *             if thrown by {@link #context()}
     */
    private PackedContainerConfiguration pcc() {
        PackedContainerConfiguration p = pcc;
        if (p == null) {
            p = pcc = ((PackedExtensionContext) context()).container();
        }
        return p;
    }

    // Scans this package...
    // Se nederst i filen paa ComponentScan, som vi har droppet fordi der er rigeligt annoteringer....
    void scan() {}

    void scan(String... packages) {}

    // Alternative to ComponentScan
    void scanForInstall(Class<?>... classesInPackages) {}

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
