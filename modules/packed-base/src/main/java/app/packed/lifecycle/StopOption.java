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
package app.packed.lifecycle;

import java.util.concurrent.TimeUnit;

/**
 *
 */

// ContainerStopOption????
// Eller er det generisk..? Kan den bruges paa en actor??? et Actor Trae...
public interface StopOption {

    static StopOption erroneous(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    static StopOption now() {
        // Now == shutdownNow();
        throw new UnsupportedOperationException();
    }

    static StopOption now(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    static StopOption forced() {
        throw new UnsupportedOperationException();
    }

    static StopOption graceTime(long timeout, TimeUnit unit) {
        // before forced???
        throw new UnsupportedOperationException();
    }

    // restart.. (Artifact must have been started with RestartWirelets.restartable();

}
