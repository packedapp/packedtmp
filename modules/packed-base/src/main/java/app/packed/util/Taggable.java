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
package app.packed.util;

import java.util.Set;

/**
 * An interface indicating that an object can be tagged.
 * <p>
 * For example, tags can be added to services:
 *
 * <pre> {@code
 * Injector i = Injector.of(c -> c.bind(123).tags().add("SomeTag"));
 * Set<String> tagsOnService = i.getDescriptor(Integer.class).tags());}</pre>
 * <p>
 * Tags can be any non-null string, even the empty string. Tags are case sensitive.
 */
interface Taggable {

    /**
     * Returns an set of all tags present on the taggable object. The set returned is, unless otherwise specified, mutable
     * at configuration time and immutable at runtime.
     *
     * @return a set of all tags on the taggable object
     */
    Set<String> tags();
}
