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
package app.packed.service;

import java.util.Optional;

import app.packed.base.InvalidDeclarationException;
import app.packed.component.Component;
import app.packed.container.Extension;
import app.packed.introspection.ConstructorDescriptor;
import app.packed.introspection.FieldDescriptor;
import app.packed.introspection.MemberDescriptor;
import app.packed.introspection.MethodDescriptor;
import app.packed.introspection.ParameterDescriptor;
import app.packed.introspection.VariableDescriptor;

/**
 * An instance of this interface can be injected into methods annotated with {@link Provide}.
 * 
 * <p>
 * ProvideContext cannot be used together with the Singleton annotation, as singletons should be independent of the
 * requesting client. Instead an {@link InvalidDeclarationException} is thrown.
 * 
 * Whenever a A service requestions has two important parts. What exactly are being requested, is it optional is the
 * service being requested.
 * 
 * An injection site extends the dependency interface with runtime information about which injector requested the
 * injection. And if used within a container which component requested the injection.
 * <p>
 * This class is typically used together with the {@link Provide} annotation to provide custom injection depending on
 * attributes of the requestor. <pre> {@code  @Provides
 *  public static Logger provideLogger(PrototypeRequest request) {
 *    if (request.component().isPresent()) {
 *      return Logger.getLogger(request.component().get().getPath().toString());
 *    } else {
 *      return Logger.getAnonymousLogger();
 *    }
 *  }}
 * </pre>
 * <p>
 * An injection site can also be used to create a factory that is functional equivalent:
 * 
 * <pre> {@code
 *  new Factory1<PrototypeRequest, Logger>(
 *    c -> c.component().isPresent() ? Logger.getLogger(request.component().get().getPath().toString()) : Logger.getAnonymousLogger()) {};
 * }
 * </pre>
 */

// Taenker den 

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

// ServiceRequest... Somebody is requesting a service...

// Kan not be used with singletons...

// ConfigSite???? <- Use unknown for service locator...

// ComponentPath
// ConfigSite

// Den eneste grund til vi ikke sletter denne, er for request traces....
// Detailed Logging....
// (@PrintResolving SomeService)
//// Kunne vi tage en enum????
//// NO_DEBUG, DEBUG_LOOK_IN_THREADLOCAL

// Interface vs Class..
// Interface because we might want an implementation with meta data internally...
// PackedPrototypeRequest that every entry will read, and log stuff.

// ServiceUsageSite, ProtetypeUsageSite

// ProvideContext <- Used together with Provide.....
// But not if Singleton.....

// Maaske er Component bare en separate injection???
// Men saa skal vi have ComponentContext.component()

// Kan den bruges paa sidecars??? SidecarProvide maaske....
// ProvideLocal? Føler lidt at grunden til vi ikke kan bruge annoteringer
// Som vi vil paa sidecars er pga Provide...
// F.eks. Schedule boer jo fungere praecis som var den paa componenten...

//ProvisionContext (If we have InjectionContext)
//ProvisionPrototypeContext
public interface ProvideContext {

    // Hvad hvis det ikke er en direkte extension der forsporger???
    // Men f.eks. et eller andet inde i en Bundle som er installeret
    // som en extension...
    default Optional<Class<? extends Extension>> extension() {
        return Optional.empty();
    }

    // Vi tager alle annotations med...@SystemProperty(fff) @Foo String xxx
    // Includes any qualifier...
    // AnnotatedElement memberAnnotations();

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    boolean isOptional();

    /**
     * Returns the key of the service that needs to be provided.
     *
     * @return the key of the service that needs to be provided
     */
    // Maaske den bare skal vaere separat.... Hvis vi gerne vil bruge ProvideContext for prime.
    // Saa er det jo noedvendig ikke noget der hedder key...

    // Og vi ved vel aerlig talt hvilken noegle vi er.....
    // Key<?> key();

    /**
     * The class that is requesting
     * 
     * @return stuff
     */
    default Optional<Class<?>> requestingClass() {
        // RequestingClass
        // RequestingMember
        // RequestingVariable

        // Requester, if used for dependency injection....
        // Her er det taenkt som den oprindelig klasse... sans mappers...sans composites
        throw new UnsupportedOperationException();
    }

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
     * @see #originVariable()
     */
    Optional<MemberDescriptor> originMember();

    /**
     * The variable (field or parameter) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * <p>
     * If this dependency was created from a field the returned optional will contain a {@link FieldDescriptor}. If this
     * dependency was created from a parameter the returned optional will contain a {@link ParameterDescriptor}.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     * @see #originMember()
     */
    Optional<VariableDescriptor> originVariable();// Should match Var...-> VarDescriptor-> VariableDescriptor

    /**
     * Return the component that is requesting a service. Or an empty optional otherwise, for example, when used via
     * {@link Injector#use(Class)}.
     * 
     * @return the component that is requesting the component, or an empty optional if not a component.
     */
    // What to do at configuration time.... We probably don't have a component at that point...

    // ComponentPath???, syntes ikke man skal kunne iterere over dens boern...
    // Det er bare f.eks. til at debugge...

    Optional<Component> component();

}

//static ProvideContext of(Dependency dependency) {
//  return new ProvideContextImpl(dependency, null);
//}
//
//static ProvideContext of(Dependency dependency, Component componenent) {
//  return new ProvideContextImpl(dependency, requireNonNull(componenent, "component is null"));
//}

//
///**
//* If this helper class is created as the result of needing dependency injection. This method returns an empty optional
//* if used from methods such as {@link App#use(Class)}.
//* 
//* @return any dependency this class might have
//*/
//default Optional<Dependency> dependency() {
// throw new UnsupportedOperationException();
//}