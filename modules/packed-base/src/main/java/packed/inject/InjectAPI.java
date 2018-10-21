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
package packed.inject;

import static java.util.Objects.requireNonNull;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import packed.inject.factory.InternalFactory;
import packed.util.descriptor.AbstractVariableDescriptor;

/**
 *
 */
public class InjectAPI {

    public static <T> InternalFactory<T> fromFactory(Factory<T> factory) {
        return InjectHolder.INJECT.fromFactory(factory);
    }

    public static Dependency injectNewDependency(AbstractVariableDescriptor variable) {
        return InjectHolder.INJECT.newDependency(variable);
    }

    static class InjectHolder {
        static final SupportInject INJECT;

        static {
            Factory.find(Object.class);
            INJECT = requireNonNull(SupportInject.SUPPORT, "internal error");
        }
    }

    public static abstract class SupportInject {

        /** Used for invoking package private InjectorBuilder constructor. */
        private static SupportInject SUPPORT;

        protected abstract <T> InternalFactory<T> fromFactory(Factory<T> factory);

        protected abstract Dependency newDependency(AbstractVariableDescriptor variable);

        // protected abstract InjectorBuilder newBuilderSupport(InternalInjectorBuilder builder);

        public static void init(SupportInject ss) {
            if (SUPPORT != null) {
                throw new Error("Already initialized");
            }
            SUPPORT = requireNonNull(ss);
        }
    }
}
