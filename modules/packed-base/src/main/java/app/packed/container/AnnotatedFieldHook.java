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
package app.packed.container;

import java.lang.annotation.Annotation;

import app.packed.util.FieldDescriptor;

/**
 * A hook represent an annotated field on a component instance.
 */
public interface AnnotatedFieldHook<T extends Annotation> {

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    /**
     * Returns the hooked component.
     * 
     * @return the hooked component
     */
    Component component();

    /**
     * Returns the annotated field.
     * 
     * @return the annotated field
     */
    FieldDescriptor field();

    /**
     * Returns the current value of the field.
     * 
     * @return the current value of the field
     */
    Object get();

    /**
     * Sets the value of the field
     * 
     * @param value
     *            the value of the field
     * @throws ClassCastException
     *             if the specified value does not match the type of the field
     * @throws UnsupportedOperationException
     *             if the field is final
     */
    void set(Object value);
}
