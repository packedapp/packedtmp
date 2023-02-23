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
package app.packed.extension.beanpouch;

/**
 *
 */
public class BeanProps {

    // Actor.serialize
    // Wrapper alle operationer


    // Controlled.. Vi behoever ikke have en shutdown lock.

    // Controlled, er vel uafhandig af single multi_threading?
    enum Threading {
        SINGLE_THREAD, CONTROLLED, MULTI_THREADED;
    }
}
