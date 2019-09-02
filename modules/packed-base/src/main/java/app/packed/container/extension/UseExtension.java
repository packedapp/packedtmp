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
package app.packed.container.extension;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;

import app.packed.container.extension.UseExtension.Multiple;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Provide;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.lifecycle.OnStart;

/**
 *
 */
@Repeatable(Multiple.class)
@interface UseExtension {

    Class<? extends Annotation>[] annotatedFields() default {};

    Class<?>[] instanceOfs() default {};

    Class<? extends Extension>[] value();

    @interface Multiple {
        @SuppressWarnings("exports")
        UseExtension[] value();
    }
}

@UseExtension(annotatedFields = Provide.class, value = InjectionExtension.class)
@UseExtension(annotatedFields = OnStart.class, instanceOfs = Lifecycle.class, value = LifecycleExtension.class)
class MyBundle {}

interface Lifecycle {
    void onStart();
}

// UseExtension <- calls use()....

// On Annotation, class, ect -> only value can be used
// On Bundle
// On Extension
// On Component @ActivateExtension(InjectionExtension.class)