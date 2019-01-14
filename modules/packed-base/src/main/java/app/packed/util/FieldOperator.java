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
package app.packed.util;

/**
 * A field operator
 */
public interface FieldOperator {

    /**
     * Returns the fields descriptor.
     * 
     * @return the fields descriptor
     */
    FieldDescriptor descriptor();

    /**
     * Returns the value of the field.
     * 
     * @return the value of the field
     */
    Object get();

    /**
     * Returns whether or not the field can be written. For example, if the field is final it cannot be written
     * 
     * @return
     */
    boolean isSettable();

    /**
     * @param value
     * @throws UnsupportedOperationException
     *             if the field cannot be written, for example, if it is final
     * @see #isSettable()
     */
    void set(Object value);

    void setAndGet(Object value);
}
// VarHandle varHandle(); // <-....Nope...., bliver sindsygt svaert at styre native generering, med mindre vi har en
// support metode

// onFieldHook(Class<? extends Annotation).doTransformation