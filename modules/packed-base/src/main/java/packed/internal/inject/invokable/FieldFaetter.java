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
package packed.internal.inject.invokable;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.VarHandle;

import app.packed.util.FieldDescriptor;

/**
 *
 */
public abstract class FieldFaetter {

    /** The field to read. */
    public final FieldDescriptor field;

    /** The var handle used for reading the field. */
    private final VarHandle varHandle;

    public FieldFaetter(FieldDescriptor field, VarHandle varHandle) {
        this.field = requireNonNull(field);
        this.varHandle = varHandle;
    }

    /**
     * Returns the value of this field for the given instance.
     * 
     * @param instance
     *            the instance for which to return the value
     * @return the value of this field for the specified instance
     * @see VarHandle#get(Object...)
     */
    public abstract Object getField();

    public abstract Object getField(Object instance);

    public class FieldFaetterStatic extends FieldFaetter {

        /**
         * @param field
         * @param varHandle
         */
        public FieldFaetterStatic(FieldDescriptor field, VarHandle varHandle) {
            super(field, varHandle);
        }

        /** {@inheritDoc} */
        @Override
        public Object getField() {
            return varHandle.get();
        }

        /** {@inheritDoc} */
        @Override
        public Object getField(Object instance) {
            throw new UnsupportedOperationException();
        }
    }

    public class FieldFaetterInstanceMissing extends FieldFaetter {

        /**
         * @param field
         * @param varHandle
         */
        public FieldFaetterInstanceMissing(FieldDescriptor field, VarHandle varHandle) {
            super(field, varHandle);
        }

        /** {@inheritDoc} */
        @Override
        public Object getField() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Object getField(Object instance) {
            return varHandle.get(instance);
        }
    }

    public class FieldFaetterInstance extends FieldFaetter {

        Object instance;

        /**
         * @param field
         * @param varHandle
         */
        public FieldFaetterInstance(FieldDescriptor field, VarHandle varHandle) {
            super(field, varHandle);
        }

        /** {@inheritDoc} */
        @Override
        public Object getField() {
            return varHandle.get(instance);
        }

        /** {@inheritDoc} */
        @Override
        public Object getField(Object instance) {
            throw new UnsupportedOperationException();
        }
    }
}
