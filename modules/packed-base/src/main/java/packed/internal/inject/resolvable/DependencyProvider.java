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
package packed.internal.inject.resolvable;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import packed.internal.component.Region;
import packed.internal.component.SourceAssembly;
import packed.internal.service.buildtime.BuildEntry;

/**
 * Something that
 */
// Typer

// En service
// En Declaring Class
// Specials.. fx dependency...
public abstract class DependencyProvider {

    // Kan lave noget med MethodHandles...

    private DependencyProvider() {}

    // Constants
    @Nullable
    public abstract Injectable resolvable();

    // Used for cycle tracking

    public MethodHandle toMethodHandle() {
        throw new UnsupportedOperationException();
    }

    public static DependencyProvider provideSingleton(SourceAssembly sa) {
        return new SingletonSourceDependencyProvider(sa);
    }

    static class ContextProvider extends DependencyProvider {

        /** {@inheritDoc} */
        @Override
        public Injectable resolvable() {
            throw new UnsupportedOperationException();
        }

    }

    // Something that is being resolved to a service

    static class ServiceDependencyProvider extends DependencyProvider {

        BuildEntry<?> entry;

        /** {@inheritDoc} */
        @Override
        public Injectable resolvable() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A special provider for singleton instance members of singletons.
     * 
     */
    // SourceAssembly kan naesten extende/implemente DependencyProvider
    static class SingletonSourceDependencyProvider extends DependencyProvider {

        /** The source */
        private final SourceAssembly source;

        SingletonSourceDependencyProvider(SourceAssembly source) {
            this.source = requireNonNull(source);
        }

        /** {@inheritDoc} */
        @Override
        public Injectable resolvable() {
            return source.injectable;
        }

        @Override
        public MethodHandle toMethodHandle() {
            if (source.injectable == null) {
                Object instance = source.instance();
                MethodHandle mh = MethodHandles.constant(instance.getClass(), instance);

                // MethodHandle()T -> MethodHandle(Region)T
                return MethodHandles.dropArguments(mh, 0, Region.class);
            } else {
                return Region.readSingletonAs(source.regionIndex, source.component.source.injectable.rawType());
            }
        }
    }
}

// Requirements

// NodeStore, Components

// Er der forskellige MH TYper??
// Needs WebRequest
// Needs Caller

// Resolve -> Finds stuff, makes room in Region

// validate, checks for circles 