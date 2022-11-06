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
// Lifetime Bridge? ExtensionLifetimeBridge

// 3 basic modes

// Constant - Laves og kan shares som et static field

// Configurable - Er semi lavet faerdigt og kan bootes med en let metode...

public class ExtensionBridge<E> {

    InnerStuff innerStuff;

    // Maybe run before Extension#onNew
    protected void onStuff(E extension) {}

    public static class Builder {

    }

    private class InnerStuff {

    }
}

// Send info til extensionen...
// Send info til en ExtensionBean (Via operationens MethodHandle)
// Provide lokale service(s) i container wrapper beanen tilbage naar containeren er initializeret vil jeg mene?.
// Disse vil som regel vaere et extract fra extension beanen