/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.web;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import internal.app.packed.web.WebGetOperationHandle;

/**
 * A mirror for a web operation.
 */
public class WebOperationMirror extends OperationMirror {

    final WebGetOperationHandle handle;

   public WebOperationMirror(OperationHandle<?> handle) {
        super(handle);
        this.handle = (WebGetOperationHandle) handle;
    }

    /** {@return the URL pattern this operation responds to} */
    public String urlPattern() {
        return handle.urlPattern;
    }
}
