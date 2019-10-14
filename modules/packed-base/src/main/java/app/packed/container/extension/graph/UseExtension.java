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
package app.packed.container.extension.graph;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.extension.Extension;
import app.packed.container.extension.graph.UseExtension.Multiple;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.lifecycle.OnStart;
import app.packed.service.Provide;
import app.packed.service.ServiceExtension;

/**
 *
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Multiple.class)

// ComponentActivateExtension...
@interface UseExtension {

    Class<? extends Annotation>[] annotatedFields() default {};

    Class<? extends Annotation>[] annotatedMethods() default {};

    Class<? extends Annotation>[] annotatedTypes() default {};

    Class<?>[] instanceOfs() default {};

    Class<? extends Extension>[] extension();

    // String[] extensionOptional(); <- kan ikke lige finde ud af om vi skal bruge den

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Multiple {
        UseExtension[] value();
    }
}
// Prop dem paa useExtension...
// Taeller imod.. Det giver kun mening paa bundle
// Taeller for.. Det er okay, Paa hooks og extensions er det alligevel kun powerbrugere....
//

@UseExtension(annotatedFields = Provide.class, extension = ServiceExtension.class)
@UseExtension(annotatedFields = OnStart.class, instanceOfs = Lifecycle.class, extension = LifecycleExtension.class)
class MyBundle {}

interface Lifecycle {
    void onStart();
}

@UseExtension(annotatedFields = Provide.class, extension = ServiceExtension.class)
@UseExtension(annotatedFields = OnStart.class, instanceOfs = Lifecycle.class, extension = LifecycleExtension.class)
@interface Foo {

}

// UseExtension <- calls use()....

// On Annotation, class, ect -> only value can be used
// On Bundle
// On Extension
// On Component @ActivateExtension(InjectionExtension.class)