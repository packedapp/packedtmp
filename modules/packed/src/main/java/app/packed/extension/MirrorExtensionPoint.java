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
package app.packed.extension;

import java.lang.invoke.MethodHandles;

import app.packed.lifetime.ContainerLifetimeChannel;

/**
 *
 */
public class MirrorExtensionPoint extends ExtensionPoint<MirrorExtension> {

    // Hmm Installering af MirrorExtension er lidt hmm hmm

    // Maaske er det bare en dum extension?
    // Og saa paa en anden checke om der er mirrors
    // Alle mirror typerne er jo en del af base's ansvar

    // Altsaa fx naar vi naar hen til application.
    // Vi kan jo ikke installere den i extensionen...
    public static ContainerLifetimeChannel CONTAINER_MIRROR = ContainerLifetimeChannel.builder(MethodHandles.lookup(), MirrorExtension.class, "ContainerMirror")
            .build();

    MirrorExtensionPoint() {}
}
