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

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.container.BundleDescriptor;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionPipeline;
import app.packed.container.extension.ExtensionWirelet;
import packed.internal.container.extension.PackedExtensionContext;

/** A support class for calling package private methods in the app.packed.extension package. */
public final class AppPackedExtensionAccess {

    /** Never instantiate. */
    private AppPackedExtensionAccess() {}

    public static Helper invoke() {
        return SingletonHolder.SINGLETON;
    }

    /** An abstract class that must be implemented by a class in app.packed.container. */
    public static abstract class Helper {

        /** An instance of the single implementation of this class. */
        private static Helper SUPPORT;

        public abstract void buildBundle(Extension extension, BundleDescriptor.Builder builder);

        /**
         * Initializes the extension.
         * 
         * @param context
         *            the extension context containing the extension
         */
        public abstract void initializeExtension(PackedExtensionContext context);

        public abstract void onConfigured(Extension extension);

        public abstract void onPrepareContainerInstantiation(Extension extension, ArtifactInstantiationContext context);

        public abstract <E extends Extension, T extends ExtensionPipeline<T>> T wireletNewPipeline(E extension, ExtensionWirelet<E, T> wirelet);

        public abstract <E extends Extension, T extends ExtensionPipeline<T>> void wireletProcess(T pipeline, ExtensionWirelet<E, T> wirelet);

        /**
         * Initializes this class.
         * 
         * @param support
         *            an implementation of this class
         */
        public static void init(Helper support) {
            if (SUPPORT != null) {
                throw new Error("Can only be initialized once");
            }
            SUPPORT = requireNonNull(support);
        }
    }

    /** Holder of the singleton. */
    private static class SingletonHolder {

        /** The singleton instance. */
        private static final Helper SINGLETON;

        static {
            new Extension() {};
            SINGLETON = requireNonNull(Helper.SUPPORT, "internal error");
        }
    }
}
