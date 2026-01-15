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
package sandbox.app.packed.build.action;

/**
 * <p>
 * Instances of this interface should never be exposed to the user that was responsible for initiating the action.
 */
// Could have 2 version, one closeable and with spawn.
// Issue with closeable is that it is a minimum of 3 lines...
public interface BuildAction {

    // Who is calling... BuildTransformer, Assembly, ect

    // Logging???

    // Used the default configured log level for the template
    BuildAction log(String message, Object... args);

    BuildAction logTrace(String message, Object... args);
}
