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
package app.packed.lifecycleold;

import app.packed.container.Wirelet;

/**
 *
 */
// I think all of them support it...... 
// I mean the problem is with Hosts/Guests...
// Guest / GuestInstance [Fordi en artifact maaske er ved at blive initializeret) / Artifact
// I think it just returns G 

//LifecycleWirelets???
public class RestartWirelets {

    public static Wirelet restartable() {
        throw new UnsupportedOperationException();
        // Must be started with execute or a guest
        // This allows stopOption.restart()
        // Fails if trying to link inside an artifact..
        // Will automatically make an image of any bundle being specified if it is not already an image
    }

    @SafeVarargs
    public static Wirelet restartOn(Class<? extends Throwable>... failureTypes) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet restartOnChecked() {
        return restartOn(Exception.class);
    }
}
// I don't think we should allow App.initialize(foo, RestartWirelets.restartOnChecked());
