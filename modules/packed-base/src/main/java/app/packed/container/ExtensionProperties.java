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
package app.packed.container;

/**
 *
 */

// UseExtension -> mandatory....
// Problemet er at UseExtension betyder noget andet paa extension ends

// Why do need to declare extensions..
// Because we can reference extension from onConfigured();
// And we have no idea what other extensions they use until we run
// onConfigured on them,

// Should not be documented...
public @interface ExtensionProperties {

    Class<? extends Extension>[] extensions() default {};

    String[] extensionsOptional() default {};

    boolean viral() default false;
}
