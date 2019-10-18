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

import java.util.Set;

import app.packed.service.ServiceDescriptor;

/**
 *
 */
// configuration time (Descriptor)
// run time
// run time via ComponentContext... for example add
public abstract class FeatureHolder<A, B> {

    FeatureHolder(AFeature<A, B> feature) {

    }

    protected FeatureHolder<A, B> instantiate() {
        return this;// Ideen er lidt at vi kan optimere en runtime version

        // Hvis man returnere null kan man ogsaa fjerne den?
        // F.eks. kunne vi have et descriptor set der var tilgaengelig paa runtime...
        // F.eks. alle dependencies fra en Component, det er ikke rigtig relevant naar den er blevet bygget.

        // En feature holder skal ogsaa kunne skrive nogle fede ting. Saa man kan sige
        // component.printInteresting stuff()..

        // Skal vi beholder Attachment map??? som en slags. anything goes

        // Hvordan passer det her med descriptors???. De er jo per container....
    }

    protected abstract A getA();

    protected abstract B getB();
}

class MyServices extends FeatureHolder<Set<ServiceDescriptor>, Set<ServiceDescriptor>> {

    MyServices() {
        super(Inj.SERVICES);// Burde checke at den passer med featuren....
        // Saa alle ikke bare kan tilfoeje det en holder for en given feature...
    }

    /** {@inheritDoc} */
    @Override
    protected Set<ServiceDescriptor> getA() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    protected Set<ServiceDescriptor> getB() {
        return getA();
    }

}
