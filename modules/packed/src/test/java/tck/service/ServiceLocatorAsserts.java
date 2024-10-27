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
package tck.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import app.packed.binding.Key;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class ServiceLocatorAsserts {

    public static void testPresent(ServiceLocator sl, Key<?> key, Predicate<Object> instance) {
        if (!key.isQualified()) {
            testPresent(sl, key.rawType(), instance);
        }
        assertTrue(sl.contains(key));
        assertThat(sl.find(key)).get().matches(instance);
        assertThat(sl.findProvider(key).get().provide()).matches(instance);

        AtomicBoolean hasRun = new AtomicBoolean();
        sl.ifPresent(key, c -> {
            assertTrue(instance.test(c));
            hasRun.set(true);
        });

        assertThat(sl.keys()).contains(key);
        assertThat(sl.toProviderMap().keySet()).contains(key);
        assertThat(sl.use(key)).matches(instance);
    }

    public static void testPresent(ServiceLocator sl, Class<?> key, Predicate<Object> instance) {
        assertTrue(sl.contains(key));
        assertThat(sl.find(key)).get().matches(instance);
        assertThat(sl.findProvider(key).get().provide()).matches(instance);

        AtomicBoolean hasRun = new AtomicBoolean();
        sl.ifPresent(key, c -> {
            assertTrue(instance.test(c));
            hasRun.set(true);
        });

        assertThat(sl.keys()).contains(Key.of(key));
        assertThat(sl.toProviderMap().keySet()).contains(Key.of(key));
        assertThat(sl.use(key)).matches(instance);
    }

    public static void testNotPresent(ServiceLocator sl, Key<?> key) {
        if (!key.isQualified()) {
            testNotPresent(sl, key.rawType());
        }

        assertFalse(sl.contains(key));
        assertThat(sl.find(key)).isEmpty();
        assertThat(sl.findProvider(key)).isEmpty();
        sl.ifPresent(key, c -> assertTrue(false));
        assertThat(sl.keys()).doesNotContain(key);
        assertThat(sl.toProviderMap().keySet()).doesNotContain(key);
        assertThrows(NoSuchElementException.class, () -> sl.use(key));
    }

    public static void testNotPresent(ServiceLocator sl, Class<?> key) {
        assertFalse(sl.contains(key));
        assertThat(sl.find(key)).isEmpty();
        assertThat(sl.findProvider(key)).isEmpty();
        sl.ifPresent(key, c -> assertTrue(false));
        assertThat(sl.keys()).doesNotContain(Key.of(key));
        assertThat(sl.toProviderMap().keySet()).doesNotContain(Key.of(key));
        assertThrows(NoSuchElementException.class, () -> sl.use(key));
    }
}
