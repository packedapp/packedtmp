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
package packed.internal.service.buildtime;

/**
 * The instantiation mode of a service.
 */
// lazy, prototype, no_cache
// Rename Prototype to Many???? The thing is it might be cached
// ADHOC, VARIABLE, ANY, MANY, PER_REQUEST

// Maybe Service.Mode?

// None -> must be from component

// We keep this around for a bit.

// ServiceMode

// Maybe we should more think of it as whether or not it is cachable....
// This also more or less makes eager singleton + lazy singleton identical

// isConstant???

public enum ServiceMode {

    /**
     * A single instance of the service is created when the injector or container where the entity is registered is created.
     * This is the default mode used throughout Packed.
     */
    CONSTANT,

    // /**
    // * A single instance of the service is created the first time it is requested. Concurrent calls by other threads while
    // * constructing the value will block. Guaranteeing that only a single instance will ever be created.
    // */
    // @Deprecated
    // LAZY,

    /** A new instance of the service is created every time the service is requested. */
    PROTOTYPE;

}
