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
package packed.internal.moduleaccess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import packed.internal.hook.MemberUnreflector;

/** An access class for accessing package private members in app.packed.hook. */
public interface AppPackedHookAccess {

    /**
     * Creates a new instance of {@link AnnotatedFieldHook}.
     * 
     * @param <T>
     *            the type of annotation
     * @param controller
     *            the component model builder
     * @param field
     *            the annotated field
     * @param annotation
     *            the annotation value
     * @return the new annotated field hook
     */
    <T extends Annotation> AnnotatedFieldHook<T> newAnnotatedFieldHook(MemberUnreflector controller, Field field, T annotation);

    /**
     * Creates a new instance of {@link AnnotatedMethodHook}.
     * 
     * @param <T>
     *            the type of annotation
     * @param controller
     *            the component model builder
     * @param method
     *            the annotated method
     * @param annotation
     *            the annotation value
     * @return the new annotated field hook
     */
    <T extends Annotation> AnnotatedMethodHook<T> newAnnotatedMethodHook(MemberUnreflector controller, Method method, T annotation);

    /**
     * Creates a new instance of {@link AnnotatedTypeHook}.
     * 
     * @param <T>
     *            the type of annotation
     * @param controller
     *            the component model builder
     * @param type
     *            the annotated type
     * @param annotation
     *            the annotation value
     * @return the new annotated type hook
     */
    <T extends Annotation> AnnotatedTypeHook<T> newAnnotatedTypeHook(MemberUnreflector controller, Class<?> type, T annotation);

    <T> AssignableToHook<T> newAssignableToHook(MemberUnreflector processor, Class<T> type);
}
