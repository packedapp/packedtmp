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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;

import app.packed.artifact.ArtifactImage;
import app.packed.component.Component;
import app.packed.container.BaseBundle;
import app.packed.container.ExtensionContext;

/**
 *
 */
public class FooBar extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void compose() {
        provideConstant("HejHEj");
    }

    public static void main(String[] args) throws IllegalAccessException {
        ArtifactImage i = ArtifactImage.of(new FooBar());

        Lookup ll = MethodHandles.privateLookupIn(ServiceExtension.class, MethodHandles.lookup());

        Optional<Component> oc = i.stream().findFirst();
        Optional<ServiceExtension> os = ExtensionContext.privateAccessExtension(ll, ServiceExtension.class, oc.get());

        System.out.println(os);
    }
}
