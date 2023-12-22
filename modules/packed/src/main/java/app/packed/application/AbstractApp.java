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
package app.packed.application;

import app.packed.container.Wirelet;

/**
 * Ideen var lidt at man bare extended nogle faa metoder. Og saa havde man en working implementation.
 */
// IDK
class AbstractApp implements App {

    /** {@inheritDoc} */
    @Override
    public void close() {

    }

    /**
     * Creates a new app image by wrapping the specified bootstrap image.
     *
     * @param image
     *            the bootstrap app image to wrap
     * @return an app image
     */
    protected final App.Image newImage(BootstrapApp.Image<?> image) {
        return new AppImage(image);
    }

    /** Implementation of {@link app.packed.application.App.Image}. */
    record AppImage(BootstrapApp.Image<?> image) implements App.Image {

        /** {@inheritDoc} */
        @Override
        public void run() {
            image.launch();
        }

        /** {@inheritDoc} */
        @Override
        public void run(Wirelet... wirelets) {
            image.launch(wirelets);
        }

        /** {@inheritDoc} */
        @Override
        public App start() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public App start(Wirelet... wirelets) {
            return null;
        }
    }
}
