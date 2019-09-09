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
package app.packed.errorhandling;

/**
 *
 */
// Build exception, creation exception, ...
// Vi kan ogsaa have en Initialization exception...
class CompositionException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    CompositionException(String message) {
        super(message);
    }

    public CompositionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompositionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
