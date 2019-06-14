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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Optional;
import java.util.OptionalInt;

import app.packed.app.App;
import app.packed.component.Component;
import app.packed.component.ComponentContext;
import app.packed.util.ConstructorDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;
import packed.internal.inject.InjectionSiteForDependency;
import packed.internal.inject.InjectionSiteForKey;

/**
 * An instance of this class is available for any component method annotated with {@link Provide}.
 * 
 * Whenever a A service requestions has two important parts. What exactly are being requested, is it optional is the
 * service being requested.
 * 
 * An injection site extends the dependency interface with runtime information about which injector requested the
 * injection. And if used within a container which component requested the injection.
 * <p>
 * This class is typically used together with the {@link Provide} annotation to provide custom injection depending on
 * attributes of the requestor. <pre> {@code  @Provides
 *  public static Logger provideLogger(InjectionSite site) {
 *    if (site.component().isPresent()) {
 *      return Logger.getLogger(site.component().get().getPath().toString());
 *    } else {
 *      return Logger.getAnonymousLogger();
 *    }
 *  }}
 * </pre>
 * <p>
 * An injection site can also be used to create a factory that is functional equivalent:
 * 
 * <pre> {@code
 *  new Factory1<ProvisionContext, Logger>(
 *    c -> c.component().isPresent() ? Logger.getLogger(site.component().get().getPath().toString()) : Logger.getAnonymousLogger()) {};
 * }
 * </pre>
 */

// Rename to ProvidesHelper, then we can save Context for other stuff. And say helper is always used for additional

// InjectionSite.. Is a bit misleasing because of Injector.get();
// DependencyRequest

// ProvidesContext, ProvisionContext

// Generic Dependency @SystemProperty, ServiceRequest

// qualifier

// What need injection (Dependency)
// who needs the injection (Which service)

// Should use composition

// Jeg syntes ikke ProvidesHelper

// Jeg er tilboejetil til at lave dependency til en final klasse...
// Og saa lave den optional paa Provides
// Dependency -> Only for Injection. use() <- er ikke en dependency men du kan bruge ProvidesHelper
// Hvad med Factory<@Cool String, Foo> Det ville jeg sige var en dependency.
// Saa Dependency har en ElementType of
// ElementType[] types = new ElementType[] { ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD,
// ElementType.PARAMETER };
// Type
// Hahaha, what about @RequiredService.. har maaske ikke noget med dependencies at goere...

// --------------------------------------------------------------------------------------
// Altsaa vi skal supporte Dependency paa alt hvad vi kan laver til et factory udfra...
// ---
// Hmmmmmm det aendrer tingene lidt, eller maaske....

// DependencyDescriptor -> klasse
// ProvidesHelper Interface. maaske de samme metoder som dependency...

// Primaere grund er at vi kan bruge Optional<Dependency>
// Som bedre angiver brug af use()

// Hvordan virker InjectorTransformer, eller hvad den nu hedder... Den er ikke en service, men en slags transformer.
// Ville ikke give mening at have den som en dependency... Vi processere altid en ting af gangen....
// Provides, for singleton service burde smide Unsupported operation for dependency. Det her object giver faktisk ikke
// mening
// for singleton services. Alle metoder burde smide UnsupportedOperationException...
// Du burde kunne faa ProvidesHelper injected i din klasse ogsaa....
// @Provides(many = true, as = Logger.class) ... ahh does qualifiers...not so pretty
// public class MyLogger {
/// MyLogger(ProvidesHelper p)
// } Saa kan vi klare AOP ogsaa. Hvilket er rigtig svaert

// ProvidesManyContext... A object that helps
// Does not make sense for singletons... med mindre vi har saaden noget som exportAs()
// ServiceContext <- Permanent [exported(), Set<Key<?>> exportedAs(), String[] usesByChildContainers
// ProvisionContext <- One time

// ProvisionContext (used with Provide), InjectionContext (used with Inject)

// provide(String.class).via(h->"fooo");
// Hmm provide("Goo").via() giver ingen mening..

// provide(MyService).from(MyServiceImpl.class)
// provide(MyServiceImpl).as(MyService.class)

public interface ProvideHelper {
    // Vi tager alle annotations med...@SystemProperty(fff) @Foo String xxx
    // Includes any qualifier...
    // AnnotatedElement annotations();

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    boolean isOptional();

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    Key<?> key();

    /**
     * The member (field, method or constructor) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a member.
     * <p>
     * If this dependency was created from a member this method will an optional containing either a {@link FieldDescriptor}
     * in case of field injection, A {@link MethodDescriptor} in case of method injection or a {@link ConstructorDescriptor}
     * in case of constructor injection.
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #variable()
     */
    Optional<Member> member();

    /**
     * If this dependency represents a parameter to a constructor or method. This method will return an optional holding the
     * index of the parameter. Otherwise, this method returns an empty optional.
     * 
     * @return the optional parameter index of the dependency
     */
    OptionalInt parameterIndex();

    /**
     * The variable (field or parameter) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * <p>
     * If this dependency was created from a field this method will return a {@link FieldDescriptor}. If this dependency was
     * created from a parameter this method will return a {@link ParameterDescriptor}.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     * @see #member()
     */
    Optional<VariableDescriptor> variable();

    /**
     * If this helper class is created as the result of needing dependency injection. This method returns an empty optional
     * if used from methods such as {@link App#use(Key)}.
     * 
     * @return any dependency this class might have
     */
    default Optional<Dependency> dependency() {
        throw new UnsupportedOperationException();
    }

    default <T extends Annotation> T qualifier(Class<T> qualifierType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the component that is requesting a service. Or an empty optional otherwise, for example, when used via
     * {@link Injector#use(Class)}.
     * 
     * @return the component that is requesting the component, or an empty optional if not a component.
     */
    Optional<Component> component();

    // default Container container() {
    // throw new UnsupportedOperationException();
    // }

    /**
     * Returns the injector through which injection was requested. If the injection was requested via a container, the
     * returned injector can be cast to Container.
     *
     * @return the injector through which injection was requested
     */
    Injector injector();// HMMMMM,

    static ProvideHelper of(Injector injector, Dependency dependency) {
        return new InjectionSiteForDependency(injector, dependency, null);
    }

    static ProvideHelper of(Injector injector, Dependency dependency, Component componenent) {
        return new InjectionSiteForDependency(injector, dependency, requireNonNull(componenent, "component is null"));
    }

    /**
     * Returns a new injection site for the specified injector and key.
     * <p>
     * This method is used to create injection site for methods such as {@link Injector#use(Key)}.
     * 
     * @param injector
     *            the injector from where injection is requested
     * @param key
     *            the for which injection is requested
     * @return an injection site for the specified injector and key.
     */
    static ProvideHelper of(Injector injector, Key<?> key) {
        return new InjectionSiteForKey(injector, key, null);
    }

    /**
     * Returns a new injection site for the specified injector, key and component.
     * <p>
     * This method is used to create injection site for methods such as {@link Injector#use(Key)} on
     * {@link ComponentContext#injector() component injectors}.
     * 
     * @param injector
     *            the injector from where injection is requested
     * @param key
     *            the for which injection is requested
     * @param component
     *            the component to which the injector belongs
     * @return an injection site for the specified injector and key and component.
     * @see #of(Injector, Dependency)
     */
    static ProvideHelper of(Injector injector, Key<?> key, Component component) {
        return new InjectionSiteForKey(injector, key, requireNonNull(component, "component is null"));
    }

    // withTags();// A way to provide info to @Provides....ahh bare mere boebl
    // static {AopReady r = AOPSupport.compile(FooClass.class)}, at runtime r.newInstance(r))// Arghh grimt
}