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
package app.packed.state;

/**
 * Thrown by a stateful system when an error could not be handled.
 */
// Or just panic exception???
// If this is in error handling why isn't BuildException or InitializationException
public class PanicException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;
}

//FailedToRuntException, ExecutionExceptionFailed
//ExecutionExeception
/// RunFailedException <- See execute fungere daaerlig here
/// StartupFailedException();
