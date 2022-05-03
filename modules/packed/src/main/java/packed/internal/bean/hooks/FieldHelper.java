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
package packed.internal.bean.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.Key;
import app.packed.bean.hooks.BeanField;
import packed.internal.inject.DependencyProducer;
import packed.internal.util.MethodHandleUtil;

/**
 *
 */
public class FieldHelper extends DependencyHolder {

    /** The modifiers of the field. */
    private final int modifiers;

    /** A direct method handle to the field. */
    public final VarHandle varHandle;
    
    public FieldHelper(BeanField field, VarHandle mh, boolean provideAsConstant, Key<?> provideAsKey) {
        super(List.of(), provideAsConstant, provideAsKey);
        this.modifiers = requireNonNull(field.getModifiers());
        this.varHandle = requireNonNull(mh);
    }

    @Override
    public DependencyProducer[] createProviders() {
        DependencyProducer[] providers = new DependencyProducer[Modifier.isStatic(modifiers) ? 0 : 1];
        return providers;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandle() {
        return MethodHandleUtil.getFromField(modifiers, varHandle);
    }
}
