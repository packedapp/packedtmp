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

import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.bundle.BundlingImportOperation;
import app.packed.bundle.BundlingOperation;
import app.packed.container.ContainerBundle;
import app.packed.inject.Injector;
import app.packed.inject.InjectorBundle;
import app.packed.inject.ServiceConfiguration;
import packed.internal.inject.builder.InjectorBuilder;

/** A support class for calling package private methods in the app.packed.inject package. */
public final class BundleSupport {

    public static Helper invoke() {
        return SingletonHolder.SINGLETON;
    }

    /** An abstract class that must be implemented by a class in app.packed.inject. */
    public static abstract class Helper {

        /** An instance of the single implementation of this class. */
        private static Helper SUPPORT;

        @Deprecated
        public abstract void configureInjectorBundle(InjectorBundle bundle, InjectorBuilder builder, boolean freeze);

        public abstract void bundleOperationFinish(BundlingOperation stage);

        public abstract void stageOnService(BundlingOperation stage, ServiceConfiguration<?> sc);

        public abstract MethodHandles.Lookup stageLookup(BundlingOperation stage);

        /**
         * @param stages
         * @param type
         *            either {@link Injector}, {@link InjectorBundle} or {@link ContainerBundle}
         * @return
         */
        public abstract List<BundlingOperation> extractBundlingOperations(BundlingOperation[] stages, Class<?> type);

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
            new BundlingImportOperation() {}; // Initializes TypeLiteral, which in turn will call SupportInject#init
            SINGLETON = requireNonNull(Helper.SUPPORT, "internal error");
        }
    }
}
