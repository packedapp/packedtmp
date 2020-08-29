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

import app.packed.component.ComponentModifierSet;

/**
 *
 */
// InitializationContext <= AssemblyContext
// Vi kan sagtens kun instantiere en guest...

// Super identical to AssemblyContext
public interface InitializationContext {

    /**
     * Returns the assembly context that we are now initializing.
     * 
     * @return the assembly context
     */
    // assembly.modifiers().isImage()... to see if we are created from an image...
    AssemblyContext assembly();

    // modifiers.isGuest()
    ComponentModifierSet modifiers();
}

// Main Purpose

// Instantiate stuff
// Inject Stuff
// Run @OnInitialize

// AssemblyContext bliver holdt

// Initialization context kan bliver splittet op per guest. Men ikke andre...
// Ideet guests altid har identisk lifecycle

// Guest, NoneRootIEnAssemblyContext?? <--idk how to express this
// GuestWirelets.delayInitialization() <--- only makes sense 