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
package app.packed.container.extension;

import app.packed.container.Wirelet;

/**
 * Extensions that make use of wirelets must use extends
 * 
 * A wirelet attached to
 */
public abstract class ExtensionWirelet<E extends Extension, T extends ExtensionPipeline<T>> extends Wirelet {

    /**
     * <p>
     * 
     * @param extension
     *            the extension to create a new pipeline from
     * @return the new pipeline
     */
    protected abstract T newPipeline(E extension);

    /**
     * @param context
     */
    protected abstract void process(T context);
}
