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

import java.util.Map;

import app.packed.application.ApplicationTemplate;
import app.packed.assembly.Assembly;
import app.packed.bean.BeanConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.FrameworkExtension;
import app.packed.operation.Op;

/**
 * Skal ikke vaere sin egen extension. Vil jeg mene... Fx
 */
public class HostExtension extends FrameworkExtension<HostExtension> {
    HostExtension() {}
    // For nu kan man registrere en Adaptor i templaten.
    // Og maaske override den i installeren, med en konkrete beanconfiguration (Der har vaeret installeret specielt)
    // Kan jo altid filtrer efter dem

    // We lazy register guest adaptors
    // And if you need to do stuff get this map
    // This does mean that we cannot configure have multiple adaptors of the same type
    // That does something different. Can revise the design if need this at some point
    // we have some provideConstant() to adaptor methods now. But don't know their usecase
    // The adaptor class is not part of the ApplicationTemplate
    public Map<Class<?>, BeanConfiguration> adaptors() {
        return Map.of();
    }

    public ComponentHostBeanConfiguration newHost(Class<?> bean) {
        throw new UnsupportedOperationException();
    }
    public ApplicationHostBeanBuilder newHostOfApplications(Class<?> adaptorType) {
        throw new UnsupportedOperationException();
    }

    public HostBeanBuilder newHostOfContainers(Class<?> adaptorType) {
        throw new UnsupportedOperationException();
    }

    public HostBeanBuilder newHostOfBeans(Class<?> adaptorType) {
        throw new UnsupportedOperationException();
    }

    public interface HostBeanBuilder {
    }

    // Det kan vaere vi tager adaptor settings ud af application templaten. Men hmm for container er det jo fint
    public interface ApplicationHostBeanBuilder extends HostBeanBuilder {
        ComponentGuestAdaptorBeanConfiguration<?> addGuestAdaptor(ApplicationTemplate<?> template, Class<?> bean);

        ComponentGuestAdaptorBeanConfiguration<?> addGuestAdaptor(ApplicationTemplate<?> template, Op<?> bean);

        // Deploys static app to host with the specified name
        // All guest adaptors must be assingable to adaptorType

        // Gets a Map<String, GuestLauncher> injected into the host bean
        void deploy(ComponentGuestAdaptorBeanConfiguration<?> b, String name, Assembly assembly, Wirelet... wirelets);

        ComponentHostBeanConfiguration install(Class<?> hostClass);
    }

    public interface ContainerHostBeanBuilder extends HostBeanBuilder {

    }

    public interface BeanHostBeanBuilder extends HostBeanBuilder {

    }
}
