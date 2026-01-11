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

import java.util.function.Function;

import app.packed.bean.Bean;
import app.packed.operation.Op;
import internal.app.packed.application.PackedApplicationTemplate;

/**
 * A template for creating new applications.
 * <p>
 * Application templates are typically only used by extensions, and normal users will rarely have any use for them.
 *
 * @param <H>
 *            the type of application handles the template creates
 */
public sealed interface ApplicationTemplate<H extends ApplicationHandle<?, ?>> permits PackedApplicationTemplate {

    /** {@return the handle class that was specified when creating the template} */
    Class<? super H> handleClass();

    /** {@return whether this template represents a managed or unmanaged application} */
    boolean isManaged();

    static <T> Builder<T> builder(Bean<T> bean) {
        return new PackedApplicationTemplate.Builder<>(bean);
    }

    @Deprecated // use builder(Bean)
    static <I> Builder<I> builder(Class<I> hostClass) {
        return new PackedApplicationTemplate.Builder<>(Bean.of(hostClass));
    }

    @Deprecated // use builder(Bean)
    static <I> Builder<I> builder(Op<I> hostOp) {
        return new PackedApplicationTemplate.Builder<>(Bean.of(hostOp));
    }

    interface Builder<I> {

       Builder<I> unmanaged();

       ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> build();

       <H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> build(Class<? super H> handleClass,
               Function<? super ApplicationInstaller<H>, ? extends H> handleFactory);
    }
}
