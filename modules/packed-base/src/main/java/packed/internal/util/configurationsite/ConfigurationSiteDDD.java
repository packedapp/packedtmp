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
package packed.internal.util.configurationsite;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.StackFrame;
import java.util.Optional;

import app.packed.util.ConfigurationSite;

/**
 * A configuration site is the location where an object or parts of it was configured
 */
public interface ConfigurationSiteDDD {

    /**
     * 
     * @return
     */
    // Open file
    // Read line
    Optional<ConfigurationSiteDDD> getParent();

    /**
     * Returns whether or not this site has a parent.
     * 
     * @return whether or not this site has a parent
     */
    default boolean hasParent() {
        return getParent().isPresent();
    }

    /**
     * Returns the operation that was performed when configuring the object.
     * 
     * @return the operation that was performed when configuring the object
     */
    String operation();
}

class RegistrationPointNone implements ConfigurationSite {
    //
    // /** {@inheritDoc} */
    // @Override
    // public Optional<StackFrame> getCaller() {
    // return Optional.empty();
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public boolean hasInfo() {
    // return false;
    // }

    /** {@inheritDoc} */
    @Override
    public Optional<ConfigurationSite> parent() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public String operation() {
        return null;
    }
}

class RegistrationPointStackFrame implements ConfigurationSite {
    private final StackFrame f;
    private final Optional<ConfigurationSite> parent;

    RegistrationPointStackFrame(Optional<ConfigurationSite> parent, StackFrame f) {
        this.f = requireNonNull(f);
        this.parent = parent;
    }

    // /** {@inheritDoc} */
    // @Override
    // public Optional<StackFrame> getCaller() {
    // return Optional.of(f);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public boolean hasInfo() {
    // return true;
    // }

    @Override
    public String toString() {
        return f.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ConfigurationSite> parent() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    public String operation() {
        return null;
    }

    // /**
    // * Returnsf the method calling this method, ignoring frames associated with java.lang.reflect.Method.invoke() and its
    // * implementation.
    // *
    // * @return
    // */
    // Optional<StackFrame> getCaller();
    //
    // //////////////////////////////////// Stack frames
    //
    // /**
    // * Returns whether or not this registration has captured any kind of information.
    // *
    // * @return whether or not this registration has captured any kind of information
    // */
    // default boolean hasInfo() {
    // return getCaller().isPresent();
    // }
    // //////////////////////////////////// Stack frames
    // // Line, number....
    // default Optional<Path> getPath() {
    // return Optional.empty();
    // }

    //
    // /**
    // * Returns an immutable list of stack frames. If no frames has been captured for the declaration site returns an empty
    // * list.
    // *
    // * @return an immutable list of stack frames
    // */
    // default List<StackFrame> getFrames() {
    // return getCaller().isPresent() ? List.of(getCaller().get()) : List.of();
    // }

    // getLineNumber

    public static ConfigurationSite fromFrame(Optional<StackFrame> frame) {
        requireNonNull(frame, "frame is null");
        return frame.isPresent() ? new RegistrationPointStackFrame(null, frame.get()) : NO_INFO;
    }

    // default ConfigurationSite spawn(Optional<StackFrame> frame) {
    // requireNonNull(frame, "frame is null");
    // return frame.isPresent() ? new RegistrationPointStackFrame(Optional.of(this), frame.get()) : NO_INFO;
    // }
}
// Ideen er at kunne se hvor ting er registreret.
// Kan styres om det vil gemmes i environment.
// DeclarationSite -> getDeclaration();

// You can either
// 3 diffent types
// NoInfo captured
// Info about a programatically (java code) site.
// Info about declaratively (config) site. (Denne kan faktisk godt have information omkring hvor filen er blevet loaded,
// dvs stacktrace)

// Hvis vi gemmer information fra declarativ og har en BaseEnvironment som vi for dybden af callstacken, boer vi maaske
// have en anden vaerdi
// for loading af filer, end programmatisk...

// Taenker vi skal returnere StackTraceElement istedet for StackFrame.. Vi har kun brug for streng informationerne, ikke
// f.eks. typerner...

// Method (member???)
// String:23(); <- also if method!=null we can return the line
// Could also be: /sdsd.txt:231

// BaseEnvironment int captureRegistrationStacks(String type), where type is one of XXXXX: Programmatic, load_config (we
// might want more stack traces?)

// RegistrationSite???

// F.eks. @Listener paa et felt paa en komponent.
// Saa faar en chain af to registrering punkter.
// Feltet (kode) og der hvor Component er registeret. Og der hvor bundlen er blevet brugt o.s.v.

// causality analysis.
// TODO StackFrame skal ikke vaere en del af gettere.
// Man kan godt spawnne, oprette med en stack frame

// To hoved sources, Flade filer, Programmatisk

// ConfigurationPoint