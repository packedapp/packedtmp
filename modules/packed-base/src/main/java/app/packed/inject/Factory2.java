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
package app.packed.inject;

import java.util.function.BiFunction;

import app.packed.base.InvalidDeclarationException;
import packed.internal.inject.factory.BaseFactory;

/**
 * A {@link Factory} type that takes two dependencies and uses a {@link BiFunction} to create new instances. The input
 * to the bi-function being the two dependencies.
 * 
 * @see Factory0
 * @see Factory1
 */
public abstract class Factory2<T, U, R> extends BaseFactory<R> {

    /**
     * Creates a new factory.
     *
     * @param function
     *            the function to use for creating new instances
     * @throws InvalidDeclarationException
     *             if the type variable T, U or R could not be determined. Or if T or U does not represent a proper
     *             dependency, or R does not represent a proper key
     */
    protected Factory2(BiFunction<? super T, ? super U, ? extends R> function) {
        super(function);
    }
}
