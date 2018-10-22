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
package support.assertj;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.inject.Factory;
import packed.inject.factory.InternalFactory;
import support.assertj.app.packed.inject.FactoryAssert;
import support.assertj.packed.inject.factory.InternalFactoryAssert;

/**
 *
 */
public class Assertions {
    public static void npe(Consumer<?> c, String name) {
        assertThatNullPointerException().isThrownBy(() -> c.accept(null)).withMessage(name + " is null").withNoCause();
    }

    public static <T, U> void npe(BiConsumer<? super T, ? super U> c, T t, U u, String nameT, String nameU) {
        assertThatNullPointerException().isThrownBy(() -> c.accept(null, u)).withMessage(nameT + " is null").withNoCause();
        assertThatNullPointerException().isThrownBy(() -> c.accept(t, null)).withMessage(nameU + " is null").withNoCause();
    }

    public static <T> FactoryAssert<T> assertThatFactory(Factory<T> factory) {
        return new FactoryAssert<>(factory);
    }

    public static <T> InternalFactoryAssert<T> assertThatInternalFactory(InternalFactory<T> factory) {
        return new InternalFactoryAssert<>(factory);
    }
}
