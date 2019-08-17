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
package app.packed.hook;

/**
 * Represent an annotated field on a component instance.
 */
public interface InstanceOfHook<T> {

    /**
     * Returns the
     * 
     * @return the type we are hooked on
     */
    Class<T> hookType();

    /**
     * Returns the instance.
     *
     * @return the instance
     */
    T instance();

    /**
     * Returns the actual type (assignable to T).
     * 
     * @return the actual type
     */
    // Syntes det er rimeligt at klassen er til raadighed,
    // Den er det jo paa runtime
    Class<? extends T> type();
}
