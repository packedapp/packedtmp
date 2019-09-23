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
package packed.internal.access;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.AnnotatedTypeHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletPipeline;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.container.model.ComponentModel;

/** A support class for calling package private methods in the app.packed.extension package. */
public interface AppPackedExtensionAccess extends SecretAccess {

    /**
     * Initializes the extension.
     * 
     * @param context
     *            the extension context containing the extension
     */
    ExtensionNode<?> initializeExtension(PackedExtensionContext context);

    <T extends Annotation> AnnotatedFieldHook<T> newAnnotatedFieldHook(ComponentModel.Builder builder, Field field, T annotation);

    <T extends Annotation> AnnotatedMethodHook<T> newAnnotatedMethodHook(ComponentModel.Builder builder, Method method, T annotation);

    /**
     * Creates a new instance of {@link AnnotatedTypeHook}.
     * 
     * @param <T>
     *            the type of annotation
     * @param builder
     *            the component model builder
     * @param type
     *            the annotated type
     * @param annotation
     *            the annotation value
     * @return the new annotated type hook
     */
    <T extends Annotation> AnnotatedTypeHook<T> newAnnotatedTypeHook(ComponentModel.Builder builder, Class<?> type, T annotation);

    void onConfigured(Extension extension);

    void onPrepareContainerInstantiation(Extension extension, ArtifactInstantiationContext context);

    <T extends ExtensionWireletPipeline<?>> void wireletProcess(T pipeline, ExtensionWirelet<T> wirelet);
}
