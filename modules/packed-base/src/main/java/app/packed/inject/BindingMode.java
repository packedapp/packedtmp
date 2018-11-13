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

/**
 * The binding mode of a service.
 */
public enum BindingMode {

    /**
     * A service instance is created together with the injector that holds the service. This is the default mode used
     * throughout the framework.
     */
    EAGER_SINGLETON,

    /**
     * A single service instance is created on demand. Concurrent calls by other threads while constructing the value will
     * block.
     */
    LAZY_SINGLETON,

    /** A new service instance is created every time the service is requested. */
    PROTOTYPE;

    /**
     * Returns whether or not mode is {@link #EAGER_SINGLETON} or {@link #LAZY_SINGLETON}.
     * 
     * @return whether or not mode is {@link #EAGER_SINGLETON} or {@link #LAZY_SINGLETON}
     */
    public boolean isSingleton() {
        return this != PROTOTYPE;
    }
}
