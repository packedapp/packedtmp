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
package packed.internal.container.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.inject.Injector;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceWirelets;
import app.packed.util.Qualifier;

/**
 *
 */
public class Test2 {

    static final Injector INJ1 = Injector.configure(c -> {
        c.provide("foo123");
    });

    static final Injector INJ2 = Injector.configure(c -> {
        c.provide(123L);
    });

    static Bundle b() {
        return new BaseBundle() {
            @Override
            protected void configure() {
                // injector().manualRequirementsManagement();
                export(provide(NeedsDate.class));
                // provide(new Date());
            }
        };
    }

    public static void main(String[] args) {
        BundleDescriptor bd = BundleDescriptor.of(b());
        System.out.println(bd.extensionsUsed());

        System.out.println(ServiceContract.of(b()));

        Injector i = Injector.of(b(), ServiceWirelets.provide(new Date()));

        System.out.println(i);
        ArtifactImage ai = ArtifactImage.of(b());

        System.out.println(ServiceContract.of(ai));
        // System.out.println(InjectorContract.of(ai.));
    }

    public static class NeedsDate {
        public NeedsDate(Date date) {
            System.out.println(date);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface Doo {}
}
// It is stripped of any implementation details
