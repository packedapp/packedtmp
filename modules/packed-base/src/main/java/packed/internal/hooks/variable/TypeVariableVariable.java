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
package packed.internal.hooks.variable;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.TypeVariable;
import java.util.Optional;

import app.packed.base.TypeToken;

/**
 *
 */
public final class TypeVariableVariable extends AbstractVariable {

    final TypeVariable<?> p;

    /**
     * @param e
     */
    public TypeVariableVariable(TypeVariable<?> e) {
        super(e);
        this.p = requireNonNull(e);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getType() {
        return p.getGenericDeclaration().getClass();
    }
    
    public Optional<String> name() {
        return Optional.of(p.getName());
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<?> typeToken() {
        throw new UnsupportedOperationException();
    }
}
