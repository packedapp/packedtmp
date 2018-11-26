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
package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.TypeLiteral;
import packed.internal.inject.buildnodes.InternalInjectorConfiguration;

/** A support class for calling package private methods in the app.packed.inject package. */
public final class BundleSupport {

    public static final void configure(InjectorBundle bundle, InternalInjectorConfiguration delegate, boolean freeze) {
        SingletonHolder.SINGLETON.configureInjectorBundle(bundle, delegate, freeze);
    }

    /** Holder of the singleton. */
    static class SingletonHolder {

        /** The singleton instance. */
        static final Helper SINGLETON;

        static {
            TypeLiteral.of(Object.class); // Initializes TypeLiteral, which in turn will call SupportInject#init
            SINGLETON = requireNonNull(Helper.SUPPORT, "internal error");
        }
    }

    /** An abstract class that must be implemented by a class in app.packed.inject. */
    public static abstract class Helper {

        /** An instance of the single implementation of this class. */
        private static Helper SUPPORT;

        protected abstract void configureInjectorBundle(InjectorBundle bundle, InternalInjectorConfiguration delegate, boolean freeze);

        protected abstract void importExportStageOnFinish(ImportExportStage stage);

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
}
