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
package packed.internal.config.site;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Optional;
import java.util.function.Predicate;

import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;

/**
 * The interface used internally for a configuration. This method includes methods that we are not yet ready to put out
 * onto the public interface.
 */
public interface InternalConfigSite extends ConfigSite {

    static final boolean DISABLED = false;

    /** A site that is used if a location of configuration site could not be determined. */
    InternalConfigSite UNKNOWN = new InternalConfigSite() {

        @Override
        public String operation() {
            return "Unknown";
        }

        @Override
        public Optional<ConfigSite> parent() {
            return Optional.empty();
        }

        @Override
        public InternalConfigSite replaceParent(ConfigSite newParent) {
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return "Unknown";
        }

        @Override
        public void visit(ConfigSiteVisitor visitor) {
            visitor.visitUnknown(this);
        }
    };

    InternalConfigSite replaceParent(ConfigSite newParent);

    static Predicate<StackFrame> P = f -> !f.getClassName().startsWith("app.packed.") && !f.getClassName().startsWith("packed.")
            && !f.getClassName().startsWith("java.");

    default InternalConfigSite linkFromAnnotatedField(String cst, Annotation annotation, FieldDescriptor field) {
        if (DISABLED) {
            return UNKNOWN;
        }
        return new AnnotatedFieldConfigSite(this, cst, field, annotation);
    }

    default InternalConfigSite thenAnnotatedMember(String cst, Annotation annotation, Member member) {
        if (member instanceof MethodDescriptor) {
            return thenAnnotatedMethod(cst, annotation, (MethodDescriptor) member);
        } else {
            return linkFromAnnotatedField(cst, annotation, (FieldDescriptor) member);
        }
    }

    default InternalConfigSite thenAnnotatedMethod(String cst, Annotation annotation, MethodDescriptor method) {
        if (DISABLED) {
            return UNKNOWN;
        }
        return new AnnotatedMethodConfigSite(this, cst, method, annotation);
    }

    default InternalConfigSite thenStack(String cst) {
        // LinkFromCaptureStack
        if (DISABLED) {
            return UNKNOWN;
        }
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(P).findFirst());
        return sf.isPresent() ? new StackFrameConfigSite(this, cst, sf.get()) : UNKNOWN;
    }

    static InternalConfigSite ofStack(String cst) {
        if (DISABLED) {
            return UNKNOWN;
        }
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(P).findFirst());
        return sf.isPresent() ? new StackFrameConfigSite(null, cst, sf.get()) : UNKNOWN;
    }

    /** A programmatic configuration site from a {@link StackFrame}. */
    static class StackFrameConfigSite extends AbstractConfigSite {

        /** The stack frame. */
        private final StackFrame stackFrame;

        /**
         * @param parent
         * @param operation
         */
        StackFrameConfigSite(ConfigSite parent, String operation, StackFrame caller) {
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
        public InternalConfigSite replaceParent(ConfigSite newParent) {
            return new StackFrameConfigSite(newParent, super.operation, stackFrame);
        }

        /** {@inheritDoc} */
        @Override
        public void visit(ConfigSiteVisitor visitor) {
            visitor.visitTopStackFrame(this);
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
// Hvis vi laver noget fra en config file er det saa naermest parent???
