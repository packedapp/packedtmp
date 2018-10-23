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

import java.lang.reflect.Type;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.TypeLiteral;
import packed.inject.factory.InternalFactory;
import packed.util.descriptor.AbstractVariableDescriptor;

/** A support class for calling package private methods in the app.packed.inject package. */
public final class InjectAPI {

    /**
     * Converts the specified variable descriptor to a dependency.
     * 
     * @param variable
     *            the variable to convert
     * @return a new dependency
     */
    public static Dependency toDependency(AbstractVariableDescriptor variable) {
        return SingletonHolder.SINGLETON.toDependency(variable);
    }

    /**
     * Converts the specified factory to an internal factory.
     * 
     * @param factory
     *            the factory to convert
     * @return the internal factory
     */
    public static TypeLiteral<?> toTypeLiteral(Type type) {
        return SingletonHolder.SINGLETON.toTypeLiteral(type);
    }
    
    /**
     * Converts the specified factory to an internal factory.
     * 
     * @param factory
     *            the factory to convert
     * @return the internal factory
     */
    public static <T> InternalFactory<T> toInternalFactory(Factory<T> factory) {
        return SingletonHolder.SINGLETON.toInternalFactory(factory);
    }

    /** Holder of the singleton. */
    static class SingletonHolder {

        /** The singleton instance. */
        static final SupportInject SINGLETON;

        static {
            Factory.find(Object.class); // Initializes Factory, which in turn will call SupportInject#init
            SINGLETON = requireNonNull(SupportInject.SUPPORT, "internal error");
        }
    }

    /** An abstract class that must be implemented by a class in app.packed.inject. */
    public static abstract class SupportInject {

        /** Used for invoking package private InjectorBuilder constructor. */
        private static SupportInject SUPPORT;

        /**
         * Converts the specified variable descriptor to a dependency.
         * 
         * @param variable
         *            the variable to convert
         * @return a new dependency
         */
        protected abstract Dependency toDependency(AbstractVariableDescriptor variable);

        /**
         * Converts the specified factory to an internal factory.
         * 
         * @param factory
         *            the factory to convert
         * @return the internal factory
         */
        protected abstract <T> InternalFactory<T> toInternalFactory(Factory<T> factory);
        
        /**
         * Converts the type to a type literal.
         * 
         * @param type
         *            the type to convert
         * @return the type literal
         */
        protected abstract TypeLiteral<?> toTypeLiteral(Type type);
        
        /**
         * Initializes this class.
         * 
         * @param support
         *            an implementation of this class
         */
        public static void init(SupportInject support) {
            if (SUPPORT != null) {
                throw new Error("Already initialized");
            }
            SUPPORT = requireNonNull(support);
        }
    }
}
