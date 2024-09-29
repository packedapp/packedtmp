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
package app.packed.service.advanced;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.service.PackedServiceResolution;

/**
 * Can be used to annotated a variable or field with a strategy for how a service is resolved.
 */
// Put them on the method? Or bean? Or even Assembly? [Parameter, Field, Method, Bean, Assembly] (I don't think we support extension, just because of enum)
// was ServiceLookupStrategy
// ServiceResolutionStrategy
// I think it is a failure to use with field or variable annotation hooks.
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceResolver {

    /** The default service resolution used unless anything else is specified. */
    static final ServiceResolver DEFAULT = new PackedServiceResolution();

    /**
     * {@return the context that will be searched, if empty all available contexts will be searched}
     * <p>
     * Context.class can be used to specify context-less service providers
     */
    Class<? extends Context<?>>[] contexts() default {};

    /**
     * {@return the namespaces that will be searched. }
     * <p>
     * If multiple namespaces are specified, and more than one namespace can resolve a given service, a build time exception
     * will be thrown.
     */
    String[] namespaces() default { "main" };

    /**
     * {@return the order in which the service scopes will be searched}
     * <p>
     * If a service is not found in one scope the framework will look in the next scope. If there are no more scopes to
     * evaluate the resolution will fail for the given {@link app.packed.binding.Key}. The various enum values in
     * {@link ServiceLookupScope} describes in more detailed how each scope is searched.
     * <p>
     * The default Strategy is Operation->Bean->Context->ServiceNamespace
     * <p>
     * If a matching service is found at the operation level it is used
     *
     * @see ServiceLookupScope#OPERATION
     * @see ServiceLookupScope#BEAN
     * @see ServiceLookupScope#CONTEXT
     * @see ServiceLookupScope#SERVICE_NAMESPACE
     */
    ServiceProviderKind[] order() default { ServiceProviderKind.OPERATION, ServiceProviderKind.BEAN, ServiceProviderKind.CONTEXT,
            ServiceProviderKind.NAMESPACE };

    /**
    *
    */
    // IDeen er at vi kan bruge til til at prioritere context. Fx ved guest
    // ApplicationMirror er

    // Man specificere da bare Context.class
    /**
     * A special marker interface that can be used with {@link ServiceResolver#contexts()} to indicate that only services
     * with no context should be resolved.
     */
    public interface NoContext extends Context<BaseExtension> {}

}
// Default
// Is there are service for operation
// Is there are service for the bean
// Is there are service for the context (Normally this is CSP). If we support dynamic services here. And two contexts both provide a service we fail
// Must use contexts() to control it
// I think if we specify multiple names and they both have the service we must fail just like contexts
// Finally we look in the namespace as defined in namespaces()

// You can change the order by using the order attribute, or remove any scopes you don't want