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
package sandbox.extension.container;

import java.util.function.Supplier;

import app.packed.container.Assembly;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.errorhandling.ErrorHandler;
import app.packed.util.Key;
import internal.app.packed.container.LeafContainerOrApplicationBuilder;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.context.ContextSpanKind;

/**
 * A builder for containers. All containers are either directly or indirectly created via a ContainerBuilder.
 * <p>
 *
 * @see BaseExtensionPoint#addCodeGenerated(BeanConfiguration, Class, Supplier)
 * @see BaseExtensionPoint#beanInstallerForExtension(app.packed.extension.bean.BeanTemplate,
 *      app.packed.extension.ExtensionPoint.UseSite)
 */
// TODO move back to BaseExtensionPoint
public sealed interface ContainerBuilder permits LeafContainerOrApplicationBuilder {

    /**
     * Creates a new container using the specified assembly.
     * <p>
     * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}.
     * Configuration of the new container must be done prior to calling this method.
     *
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the new container
     *
     * @see #build(Wirelet...)
     */
    ContainerHandle build(Assembly assembly, Wirelet... wirelets);

    /**
     * Creates a new configurable container.
     *
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the new container
     *
     * @see #install(Assembly, Wirelet...)
     */
    ContainerHandle build(Wirelet... wirelets);

    /**
     * Creates the new container and adds this extension to the new container.
     * <p>
     * The extension in new the container can be obtained by calling {@link Extension#fromHandle(ContainerHandle)}
     *
     * @return a container handle representing the new container
     *
     * @see app.packed.extension.Extension#fromHandle(ContainerHandle)
     */
    ContainerHandle buildAndUseThisExtension(Wirelet... wirelets);

    // Only Managed-Operation does not require a wrapper
    // For now this method is here. Might move it to the actual CHC at some point


    // Hmm, don't know if need a carrier instance, if we have implicit construction
//    /**
//     * @return
//     * @throws UnsupportedOperationException
//     *             if a carrier type was not defined in the container template
//     */
//    default ContainerCarrierConfiguration<?> carrierInstance() {
//        throw new UnsupportedOperationException();
//    }

    /**
     * Provides constants per Carrier Instance for this particular container builder
     *
     * @param <T>
     * @param key
     * @param arg
     * @return
     *
     * @see ExtensionLink#ofConstant(Class, Object)
     */
    default <T> ContainerBuilder carrierProvideConstant(Class<T> key, T constant) {
        return carrierProvideConstant(Key.of(key), constant);
    }

    /**
     * @see FromLifetimeChannel
     */
    <T> ContainerBuilder carrierProvideConstant(Key<T> key, T constant);

    /**
     *
     * @param holderConfiguration
     * @return
     * @see app.packed.extension.BaseExtensionPoint#installContainerHolder(Class)
     * @throws IllegalArgumentException
     *             if the holder class of the bean does not match the holder type set when creating the container template.
     */
    // LifetimeCarrier?
    default ContainerBuilder carrierUse(ContainerCarrierBeanConfiguration<?> holderConfiguration) {
        // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum
        // supportere det
        // Hvis vi vil dele den...

        // Det betyder ogsaa vi skal lave en wrapper bean alene
        return this;
    }

    /**
     * <p>
     * TODO: How do we handle conflicts? I don't think we should fail
     * <p>
     * TODO This is probably overridable by Wirelet.named()
     * <p>
     * Beans not-capitalized? Containers capitalized
     *
     * @param name
     *            the name of the container
     * @return this builder
     */
    ContainerBuilder named(String name);

    /**
     * Sets the value of the specified container local for the container being built.
     *
     * @param <T>
     *            the type of value the container local holds
     * @param local
     *            the container local to set
     * @param value
     *            the value of the local
     * @return this builder
     */
    // Do we allow non-container scope??? I don't think so
    <T> ContainerBuilder localSet(ContainerLocal<T> containerLocal, T value);

    /**
     * Sets a supplier that creates a special container mirror instead of the generic {@code ContainerMirror} when
     * requested.
     *
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    ContainerBuilder specializeMirror(Supplier<? extends ContainerMirror> supplier);

    // The application will fail to build if the installing extension
    // is not used by. Is only applicable for new(Assembly)
    // Maaske er det fint bare en wirelet der kan tage en custom besked?
    default ContainerBuilder zBuildAndRequiresThisExtension(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // ditch beanBlass, and just make sure there is a bean that can do it
    default ContainerBuilder zContextFromBean(Class<?> beanClass, ContextTemplate template, @SuppressWarnings("exports") ContextSpanKind span) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}
     *
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the linked container
     */
    default ContainerBuilder zErrorHandle(ErrorHandler h) {
        return this;
    }

    default ContainerBuilder zRequireUseOfExtension(String errorMessage) {
        throw new UnsupportedOperationException();
    }
}