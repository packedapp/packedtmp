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
package pckd.internals.inject.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public final class FieldHolder {

    private final List<AnnotatedFieldInject> fieldsAnnotatedWithInject;

    private final List<AnnotatedFieldListenTo> fieldsAnnotatedWithListener;

    private FieldHolder(List<AnnotatedFieldInject> fieldsAnnotatedWithInject, List<AnnotatedFieldListenTo> fieldsAnnotatedWithListener) {
        this.fieldsAnnotatedWithInject = requireNonNull(fieldsAnnotatedWithInject);
        this.fieldsAnnotatedWithListener = requireNonNull(fieldsAnnotatedWithListener);
    }

    /**
     * Return a list of all fields annotated with inject.
     *
     * @return a list of all fields annotated with inject
     */
    public List<AnnotatedFieldInject> fieldsAnnotatedWithInject() {
        return fieldsAnnotatedWithInject;
    }

    /**
     * Returns a list of all fields annotated with ListenTo.
     *
     * @return a list of all fields annotated with listen to
     */
    public List<AnnotatedFieldListenTo> fieldsAnnotatedWithListener() {
        return fieldsAnnotatedWithListener;
    }

    static FieldHolder get(Class<?> clazz, MethodHandle h) {
        // ArrayList<AnnotatedFieldInject> injectFields = new ArrayList<>();
        // ArrayList<AnnotatedFieldListenTo> listenToFields = new ArrayList<>();
        //
        // for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
        // for (Field f : c.getDeclaredFields()) {
        //
        // }
        // }
        return null;
    }

    /**
     * Grunden til at have den her, er saa vi kan undgaa at iterere 2 gange
     */
    static class ComponentClassDescriptorBuilder {
        Collection<AnnotatedFieldInject> injectableFields;
        Collection<AnnotatedFieldListenTo> listenerFields;
    }

    public void scanFields(ComponentClassDescriptorBuilder builder) {
        // for each Fields
        // if has @Inject field add to injectable Fields
    }
}
