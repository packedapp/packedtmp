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
package sandbox.app.packed.application.registry;

import java.util.function.Function;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.bean.Bean;
import app.packed.lifecycle.LifecycleKind;
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

    static <T, H extends ApplicationHandle<T, ?>> ApplicationTemplate<H> of(Bean<T> bean, Class<? super H> handleClass,
            Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        return new PackedApplicationTemplate<>(LifecycleKind.MANAGED, bean, handleClass, handleFactory);
    }
}
