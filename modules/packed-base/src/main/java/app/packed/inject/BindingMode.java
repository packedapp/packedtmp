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
     * A single instance of the service is created when the injector where the service is registered is created. This is the
     * default mode used throughout the framework.
     */
    SINGLETON,

    /**
     * A single service instance is created the f. Concurrent calls by other threads while constructing the value will
     * block.
     */
    LAZY,

    /** A new instance is created every time the service is requested. An injector will never attempt to cache it. */
    PROTOTYPE;

    /**
     * Returns true if the binding mode is either {@link #SINGLETON} or {@link #LAZY}, otherwise false.
     * 
     * @return true if the binding mode is either {@link #SINGLETON} or {@link #LAZY}, otherwise false
     */
    public boolean isSingleton() {
        return this != PROTOTYPE;
    }
}
