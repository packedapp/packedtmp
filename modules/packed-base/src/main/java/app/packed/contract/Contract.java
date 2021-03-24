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
package app.packed.contract;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import app.packed.application.ApplicationDriver;
import app.packed.base.Completion;
import packed.internal.component.PackedInitializationContext;

/**
 * This class is the base class for contracts in Packed.
 */
// Det er end slags beskrivelse. Og 
public abstract class Contract {

    /** A daemon driver. */
    public static final ApplicationDriver<Completion> DAEMON = ApplicationDriver.builder()
            .old(MethodHandles.empty(MethodType.methodType(Void.class, PackedInitializationContext.class)));

    /** {@inheritDoc} */
    @Override
    public abstract boolean equals(Object obj);

    /** {@inheritDoc} */
    @Override
    public abstract int hashCode();

    /**
     * Returns an artifact driver that can be used for analysis. Statefull
     * 
     * @return the default artifact driver for analysis
     */
    // maybe just analyzer
    // I think it should fail if used to create images/instantiate anything
    // contractAnalyzer paa Con
    protected static ApplicationDriver<?> defaultAnalyzer() {
        return DAEMON;
    }
}
