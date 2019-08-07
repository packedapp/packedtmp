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
package packed.internal.invoke;

import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;

/**
 * An internal factory
 */
public abstract class InvokableMember<T> extends FunctionHandle<T> {

    @Nullable
    final Object instance;

    public InvokableMember(TypeLiteral<T> typeLiteralOrKey, Object instance) {
        super(typeLiteralOrKey);
        this.instance = instance;
    }

    public abstract InvokableMember<T> withInstance(Object instance);

    public abstract boolean isMissingInstance();
}
