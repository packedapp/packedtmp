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
package app.packed.request;

import java.util.Optional;

import app.packed.base.AttributedElement;

/**
 *
 */
// Structured concurrency all the way.
public interface Request extends AttributedElement {

    // Ideen er vi er recursive...
    Optional<Request> parent();

    // Checks that CurrentThread = Request.Thread
    // spawn()

    /**
     * Returns the thread this request is a part of.
     * 
     * @return the thread this request is a part of
     */
    Thread thread();
}
