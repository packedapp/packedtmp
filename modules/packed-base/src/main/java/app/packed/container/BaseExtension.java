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
 * This is actually not 
 */
// 2 implementations:
//
// 1: A fake extension. Fails if attempting to use it.
// Will only support it for reporting core services where we need to specify an extension
//
// 2: Contaings actually functionality. But you cannot depend on it. And it will never show up under used extensions...

// Nah vi returnere bare Extension hvis det er 
final class BaseExtension extends Extension {

    /** Not today Satan, not today. */
    private BaseExtension() {}
}
