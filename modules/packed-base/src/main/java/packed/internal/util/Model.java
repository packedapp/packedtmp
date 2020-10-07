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
package packed.internal.util;

import static java.util.Objects.requireNonNull;

/**
 * An abstract class for the various models we have in Packed. A model is typically build upon a class provided by the
 * user. Where we need to analyse constructors/methods/fields.
 * 
 * @apiNote This class is actually pretty useless. It is just nice to be able to find all of the models we have. By
 *          finding all classes that extends from this class.
 */

public abstract class Model {

    /** The type this is a model for. */
    protected final Class<?> type;

    protected Model(Class<?> type) {
        this.type = requireNonNull(type);
    }

    /**
     * Returns the type this is a model for
     * 
     * @return the type this is a model for
     */
    public final Class<?> modelType() {
        return type;
    }
}
