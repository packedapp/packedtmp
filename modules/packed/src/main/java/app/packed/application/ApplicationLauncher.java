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
package app.packed.application;

import static java.util.Objects.requireNonNull;

import app.packed.binding.Key;

/**
 * Launches an application.
 */
public interface ApplicationLauncher {

    default ApplicationLauncher args(String... args) {
        throw new UnsupportedOperationException();
    }

    // Kan vi vel smide på imaged
    default ApplicationLauncher named(String name) {
        throw new UnsupportedOperationException();
    }

    default <T> ApplicationLauncher provide(Class<? super T> key, T instance) {
        return provide(Key.of(key), instance);
    }

    <T> ApplicationLauncher provide(Key<? super T> key, T instance);

    default ApplicationLauncher provide(Object instance) {
        requireNonNull(instance, "object cannot be null");
        return provideCaptured(this, instance.getClass(), instance);
    }

    private static <T> ApplicationLauncher provideCaptured(
            ApplicationLauncher self, Class<T> type, Object instance) {
        return self.provide(type, type.cast(instance));
    }
}
