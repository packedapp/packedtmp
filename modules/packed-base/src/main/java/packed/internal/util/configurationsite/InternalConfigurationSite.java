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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;
import java.util.function.Predicate;

import app.packed.util.ConfigurationSite;

/**
 * The interface used internally for a configuration. This method includes methods that we are not yet ready to put out
 * onto the public interface.
 */
public interface InternalConfigurationSite extends ConfigurationSite {

    /** A site that is used if a location of configuration site could not be determined. */
    InternalConfigurationSite UNKNOWN = new InternalConfigurationSite() {

        @Override
        public String operation() {
            return "Unknown";
        }

        @Override
        public Optional<ConfigurationSite> parent() {
            return Optional.empty();
        }

        @Override
        public InternalConfigurationSite replaceParent(ConfigurationSite newParent) {
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return "Unknown";
        }

    };

    InternalConfigurationSite replaceParent(ConfigurationSite newParent);

    static Predicate<StackFrame> P = f -> !f.getClassName().startsWith("app.packed.") && !f.getClassName().startsWith("packed.")
            && !f.getClassName().startsWith("java.");

    default InternalConfigurationSite spawnStack(ConfigurationSiteType cst) {
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(P).findFirst());
        return sf.isPresent() ? new StackFrameConfigurationSite(this, cst, sf.get()) : UNKNOWN;
    }

    static InternalConfigurationSite ofStack(ConfigurationSiteType cst) {
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(P).findFirst());
        return sf.isPresent() ? new StackFrameConfigurationSite(null, cst, sf.get()) : UNKNOWN;
    }

    /** A programmatic configuration site from a {@link StackFrame}. */
    static class StackFrameConfigurationSite extends AbstractConfigurationSite {

        /** The stack frame. */
        private final StackFrame stackFrame;

        /**
         * @param parent
         * @param operation
         */
        StackFrameConfigurationSite(ConfigurationSite parent, ConfigurationSiteType operation, StackFrame caller) {
            super(parent, operation);
            this.stackFrame = requireNonNull(caller);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return stackFrame.toString();
        }

        /** {@inheritDoc} */
        @Override
        public InternalConfigurationSite replaceParent(ConfigurationSite newParent) {
            return new StackFrameConfigurationSite(newParent, super.operation, stackFrame);
        }
    }

}
// 5 different types
//
// AnnotatedField : FieldDescriptor + Annotation
// AnnotatedMethod : MethodDescriptor + Annotation
// AnnotatedClass : Class + Annotation
// Programmatically: StackFrame (class, method, linenumber)
// FromFile : DocumentInfo URI + LineNumber + Maybe line number + column, settings.xml:333:12? L333:C12
//
// Vi har en unik
// operation