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

import java.lang.reflect.Member;
import java.util.Optional;

import app.packed.base.AnnotatedVariable;
import app.packed.base.Key;
import app.packed.bundle.Extension;
import app.packed.component.Component;

/**
 * An instance of this interface can be injected into methods that are annotated with {@link Provide}.
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
 * 
 * @apiNote In the future, if the Java language permits, {@link ProvisionContext} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ProvisionContext {

    /**
     * Returns weather or not the provided instance will be treated as a constant.
     * <p>
     * The runtime may choose to create
     * 
     * @return whether or not the provided instance will be treated as a constant
     * 
     * @see Provide#constant()
     */
    boolean isConstant();

    /**
     * Returns whether or the instance is being injected into, for example, a field, method, constructor.
     * <p>
     * This method always returns the opposite of {@link #isLookup()}
     * 
     * @return whether or the instance is being injected
     */
    boolean isInjection();

    /**
     * Returns whether or not the instance was requested via a {@link ServiceLocator} or one of its subclasses.
     *
     * @return whether the instance is being provided via a lookup
     * @see ServiceLocator#findInstance(Class)
     * @see ServiceLocator#findInstance(Key)
     * @see ServiceLocator#use(Class)
     * @see ServiceLocator#use(Key)
     */
    boolean isLookup();

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    // Tillader vi det??? Hvorfor ikke
    // Hvis man vil vaere optional skal man bruge ProvisionContext
    // Og man skal lade sig styre af isOptional
    boolean isOptional();

    /**
     * If the provisioning is part of an injection and not a {@link #isConstant() constant}. Returns the type of class that
     * the provided instance is being injected into. If the provisioning is part of a lookup returns
     * {@link Optional#empty()}.
     * 
     * @return any class that is being provisioned
     */
    Optional<Class<?>> targetClass();

    /**
     * Return the component that is requesting a service. Or an empty optional otherwise, for example, when used via
     * {@link ServiceLocator#use(Class)}.
     * 
     * @return the component that is requesting the component, or an empty optional if not a component.
     */
    Optional<Component> targetComponent(); // ComponentPath???, syntes ikke man skal kunne iterere over dens boern...

    /**
     * If the requester party is part of an {@link Extension} and not a constant. Returns the type of extension that is
     * requesting an instance. Otherwise false.
     * <p>
     * 
     * @return who wants this shit
     * 
     * @apiNote This method is only relevant for extension developers.
     */
    Optional<Class<? extends Extension>> targetExtension();

    /**
     * The member (field, method or constructor) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a member.
     * <p>
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #targetVariable()
     */
    // Altsaa taenker man laver en special annotation.
    Optional<Member> targetMember();

    /**
     * Returns the variable, typically a field or parameter, if it can be determined. The variable (field or parameter) for
     * which this dependency was created. Or an empty {@link Optional} if this dependency was not created from a variable.
     * <p>
     * Returns empty is provisioning is done via service locator. Or if the provided instance is shared
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     */
    Optional<AnnotatedVariable> targetVariable();
}
