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
package packed.internal.service.util.nextapi;

/**
 *
 */

// For components yes
public enum InstanceCardinality {
    ZERO, ONE, MANY;
}

// But for services vi only have isSingleton

// Prototype scope = A new object is created each time it is injected/looked up. It will use new SomeClass() each time.
//
// Singleton scope = (Default) The same object is returned each time it is injected/looked up. Here it will instantiate
// one instance of SomeClass and then return it each time.

// Maybe lazy <- But thats the component, not the service.....
// Or is it???