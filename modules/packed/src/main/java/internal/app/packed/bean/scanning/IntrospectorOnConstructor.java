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
package internal.app.packed.bean.scanning;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;

/** Implements {@link app.packed.bean.scanning.BeanIntrospector.OnConstructor}. */
public final class IntrospectorOnConstructor extends IntrospectorOnExecutable<Constructor<?>> implements BeanIntrospector.OnConstructor {

    /**
     * @param ce
     * @param member
     * @param annotations
     */
    IntrospectorOnConstructor(BeanIntrospectorSetup ce, Constructor<?> member, Annotation[] annotations) {
        super(ce, member, annotations);
    }

    /** {@inheritDoc} */
    @Override
    public Constructor<?> constructor() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller newOperation(OperationTemplate template) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        throw new UnsupportedOperationException();
    }
}
