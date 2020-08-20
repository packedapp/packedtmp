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
package app.packed.artifact;

import app.packed.component.Wirelet;
import app.packed.container.BaseBundle;

/**
 *
 * @param <A>
 *            the type of artifact the image create
 */
public interface UnmanagedImage<A> {

    // Nej... syntes ikke alle skal kunne noedvendig bruge den driver...
    ArtifactDriver<A> driver();

    /**
     * Creates a new artifact using this image.
     * 
     * @param wirelets
     *            wirelets used to create the artifact
     * @return the new artifact
     */
    A create(Wirelet... wirelets);

    static class Doo extends BaseBundle {
        public static void main(String[] args) {
            try (App a = App.start(new Doo())) {
                System.out.println(a.path());
            }

            TakeImage<App> img = App.newImage(new Doo());
            try (App a = img.start()) {
                System.out.println(a.path());
            }

        }

        /** {@inheritDoc} */
        @Override
        protected void configure() {}
    }
}
