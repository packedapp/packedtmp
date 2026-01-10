/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.container;

import app.packed.assembly.Assembly;
import app.packed.binding.Key;
import internal.app.packed.container.PackedContainerInstaller;

/**
 * A container installer.
 *
 * @see BaseExtensionPoint#addCodeGenerated(BeanConfiguration, Class, Supplier)
 * @see BaseExtensionPoint#beanInstallerForExtension(app.packed.extension.bean.BeanTemplate,
 *      app.packed.extension.ExtensionPoint.UseSite)
 */
public sealed interface ContainerInstaller<H extends ContainerHandle<?>> permits PackedContainerInstaller {

    ContainerInstaller<H> componentTag(String... tags);

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
    H install(Assembly assembly, Wirelet... wirelets);

    /**
     * Creates a new configurable container.
     *
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the new container
     *
     * @see #install(Assembly, Wirelet...)
     */
    H install(Wirelet... wirelets);

    /**
     *
     * @param holderConfiguration
     * @return
     * @see app.packed.extension.BaseExtensionPoint#installContainerHolder(Class)
     * @throws IllegalArgumentException
     *             if the holder class of the bean does not match the holder type set when creating the container template.
     */
    // LifetimeCarrier?
//        default Installer carrierUse(ComponentGuestAdaptorBeanConfiguration<?> holderConfiguration) {
//            // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum
//            // supportere det
//            // Hvis vi vil dele den...
//
//            // Det betyder ogsaa vi skal lave en wrapper bean alene
//            return this;
//        }

    // Only Managed-Operation does not require a wrapper
    // For now this method is here. Might move it to the actual CHC at some point

    // Hmm, don't know if need a carrier instance, if we have implicit construction
    // /**
    // * @return
    // * @throws UnsupportedOperationException
    // * if a carrier type was not defined in the container template
    // */
    // default ContainerCarrierConfiguration<?> carrierInstance() {
    // throw new UnsupportedOperationException();
    // }

    /**
     * Creates the new container and adds this extension to the new container.
     * <p>
     * The extension in new the container can be obtained by calling {@link Extension#fromHandle(ContainerHandle)}
     *
     * @return a container handle representing the new container
     *
     * @see app.packed.extension.Extension#fromHandle(ContainerHandle)
     */
    H installAndUseThisExtension(Wirelet... wirelets);

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
    ContainerInstaller<H> named(String name);

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
    default <T> ContainerInstaller<H> provideGuestConstant(Class<T> key, T constant) {
        return provideGuestConstant(Key.of(key), constant);
    }

    /**
     * @see FromLifetimeChannel
     */
    <T> ContainerInstaller<H> provideGuestConstant(Key<T> key, T constant);

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
    // initializeLocalWith??
    <T> ContainerInstaller<H> setLocal(ContainerBuildLocal<T> containerLocal, T value);

}