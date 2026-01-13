/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.binding;

import static java.util.Objects.requireNonNull;

import app.packed.bean.BeanTrigger.AutoService;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;

/**
 * A provider of instances.
 *
 * @param <T>
 *            the type of instances that are provided
 */
@FunctionalInterface
@AutoService(introspector = ProviderBeanIntrospector.class)
public interface Provider<T> {

    /**
     * Provides an instance of type {@code T}.
     *
     * @return the provided instance
     */
    T provide();

    /**
     * Returns a provider that will be provide the specified instance for every invocation of {@link #provide()}.
     *
     * @param <T>
     *            the type of the provider
     * @param instance
     *            the instance
     * @return a new provider that provides the specified instance
     */
    static <T> Provider<T> ofConstant(T constant) {
        record InstanceProvider<T>(T provide) implements Provider<T> {}
        return new InstanceProvider<>(requireNonNull(constant, "instance is null"));
    }
}

final class ProviderBeanIntrospector extends BaseExtensionBeanIntrospector {

}

