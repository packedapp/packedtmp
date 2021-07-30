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
import java.util.Set;

import app.packed.application.ApplicationDriver;
import app.packed.base.Completion;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import packed.internal.application.ApplicationLaunchContext;

/**
 * This class is the base class for contracts in Packed.
 */
// Det er end slags beskrivelse. Og 
// implements AttributeHolder???
public abstract class Contract {

    /** A daemon driver. */
    private static final ApplicationDriver<Completion> DAEMON = ApplicationDriver.builder()
            .buildOld(MethodHandles.empty(MethodType.methodType(Void.class, ApplicationLaunchContext.class)));

    /** {@inheritDoc} */
    @Override
    public abstract boolean equals(Object obj);

    /** {@inheritDoc} */
    @Override
    public abstract int hashCode();

    /**
     * Builds an application and returns a set of all of the contracts it exposes.
     * 
     * @param assembly
     *            the assembly used for building the application
     * @param wirelets
     *            optional wirelets
     * @return all contracts of the application
     */
    static Set<Contract> allOf(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
        // DAEMON.analyze(assembly, wirelets).
    }

    /**
     * Returns an artifact driver that can be used for analysis. Statefull
     * 
     * @return the default artifact driver for analysis
     */
    // maybe just analyzer
    // I think it should fail if used to create images/instantiate anything
    // contractAnalyzer paa Con
    // defaultIntrospector
    protected static ApplicationDriver<?> defaultAnalyzer() {
        return DAEMON;
    }
}
