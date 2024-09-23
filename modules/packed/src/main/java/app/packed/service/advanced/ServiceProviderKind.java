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

/**
 * A service lookup scope is the place where services are looked up
 *
 * @see ServiceLookupScope
 */
// was ServiceLookupScope
// ServiceProviderKind
public enum ServiceProviderKind {

    /**
     * A service that is provided by the operation itself.
     * <p>
     * The extension (operator) that operates the operation can also choose to specify
     * @see app.packed.operation.OperationConfiguration#bindServiceInstance(Class, Object)
     * @see app.packed.operation.OperationConfiguration#bindServiceInstance(app.packed.binding.Key, Object)
     */
    // Kan selv specificere dem.
    // Extensions der laver operationer kan ogsaa specificere dem
    // Fx guests bean
    // En ting er uklart om vi skal supportere overrides?
    // Maaske kan en extension
    OPERATION,

    /**
     * Lookup services that have been specified on the bean level.
     *
     * @see app.packed.bean.BeanConfiguration#bindCodeGenerator(Class, java.util.function.Supplier)
     * @see app.packed.bean.BeanConfiguration#bindCodeGenerator(app.packed.binding.Key, java.util.function.Supplier)
     * @see app.packed.bean.BeanConfiguration#bindServiceInstance(Class, Object)
     * @see app.packed.bean.BeanConfiguration#bindServiceInstance(app.packed.binding.Key, Object)
     **/
    // Men har vi disse??? Er det ikke kun i factories???
    // Altsaa hvis der er konstanter saa.
    // Supportere vi Ops saa virker det naturligvis ikke
    BEAN,

    /**
     * Lookup services that are provided by a context.
     *
     * @see app.packed.context.ContextualServiceProvider
     * @see app.packed.context.InheritableContextualServiceProvider
     **/
    CONTEXT,

    /**
     * Lookup services that are provided by a service namespace.
     *
     * @see app.packed.extension.BaseExtension#services()
     * @see app.packed.extension.BaseExtension#services(String)
     * @see app.packed.service.ServiceNamespaceConfiguration
     **/
    NAMESPACE;
}
