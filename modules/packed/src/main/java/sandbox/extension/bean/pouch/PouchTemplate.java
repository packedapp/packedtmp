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
package sandbox.extension.bean.pouch;

/**
 *
 */

// Kan vaere med i OperationTemplaten?
// og saa laver vi den lazy
public class PouchTemplate {

    // supports foreign threads ect.
    //
}

// A bean that is created per operation.
// Obvious manyton, but should we have own kind?
// I actually think so because, because for now it always requires manyton

// Some questions, do we support @Schedule? Or anything like it?
// I don't think we need to set up the support for it by default. Only if used
// So overhead is not needed

// But I think those annotations that make sense are always "callback" extensions
// From other threads
// Single threaded vs multi-threaded
// If we are single threaded it is obviously always only the request method
// If we are multi threaded we create own little "world"
// I think that is the difference, between the two

// Maybe bean is always single threaded.
// And container is always multi threaded
