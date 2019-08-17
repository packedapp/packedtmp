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
package packed.internal.support;

import static java.util.Objects.requireNonNull;

import app.packed.extension.Extension;
import packed.internal.container.PackedContainerConfiguration;

/** A support class for calling package private methods in the app.packed.extension package. */
public final class AppPackedExtensionSupport {

    public static Helper invoke() {
        return SingletonHolder.SINGLETON;
    }

    /** An abstract class that must be implemented by a class in app.packed.container. */
    public static abstract class Helper {

        /** An instance of the single implementation of this class. */
        private static Helper SUPPORT;

        /**
         * Initializes the extension with the configuration of the container in which it is registered.
         * 
         * @param extension
         *            the extension to configure
         * @param configuration
         *            the configuration of the container in which the extension is registered
         */
        public abstract void initializeExtension(Extension extension, PackedContainerConfiguration configuration);

        public abstract void onConfigured(Extension extension);

        /**
         * Initializes this class.
         * 
         * @param support
         *            an implementation of this class
         */
        public static void init(Helper support) {
            if (SUPPORT != null) {
                throw new Error("Can only be initialized ince");
            }
            SUPPORT = requireNonNull(support);
        }
    }

    /** Holder of the singleton. */
    static class SingletonHolder {

        /** The singleton instance. */
        static final Helper SINGLETON;

        static {
            new Extension() {};
            SINGLETON = requireNonNull(Helper.SUPPORT, "internal error");
        }
    }
}
