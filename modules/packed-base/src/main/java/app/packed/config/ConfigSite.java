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
package app.packed.config;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;
import packed.internal.config.site.AbstractConfigSite;
import packed.internal.config.site.AnnotatedFieldConfigSite;
import packed.internal.config.site.AnnotatedMethodConfigSite;
import packed.internal.config.site.CapturedStackFrameConfigSite;
import packed.internal.config.site.UnknownConfigSite;

/**
 * A configuration site represents the location where an object was configured/registered. This can, for example, be a
 * filename and a line number, an annotated method, or a class file with a line number.
 * <p>
 * A configuration site can have a parent, thereby nesting for example, tthe parent of a service registration will be
 * the registration point of its injector.
 * 
 * @see ConfigSiteJoiner
 */
// We need to open up... If this a generic mechanism...

// Can we intern them????? ClassValue<ConfigSite>
// 99% of the time they will probably have the same parents...
// Maybe store a hash... for the total configuration site.
// No matter what, we should never new ConfigSite*** in any way

// Can lazily generate line numbers from AnnotatedMethods+fields via reading of classinfo

// https://api.flutter.dev/flutter/package-source_span_source_span/package-source_span_source_span-library.html

// ConfigSite chain
// https://api.flutter.dev/flutter/package-stack_trace_stack_trace/Chain-class.html

// Interface vs class...
// People are not going to implement their own... Because of visitors.. So might as well be a class
public interface ConfigSite {

    /** A special configuration site indicating that the actual configuration site could not be determined. */
    ConfigSite UNKNOWN = UnknownConfigSite.INSTANCE;

    /**
     * Performs the given action on each element in configuration site chain, traversing from the top configuration site.
     *
     * @param action
     *            an action to be performed on each {@code ConfigSite} in the chain
     */
    default void forEach(Consumer<? super ConfigSite> action) {
        requireNonNull(action, "action is null");
        var cs = this;
        while (cs != null) {
            action.accept(cs);
            cs = cs.parent().orElse(null);
        }
    }

    /**
     * Returns whether or not this site has a parent.
     * 
     * @return whether or not this site has a parent
     */
    default boolean hasParent() {
        return parent().isPresent();
    }

    /**
     * Returns the name of the configuration operation that was performed.
     * 
     * @return the name of the configuration operation that was performed
     */
    // If open up for custom operations... We should probably have a naming scheme...
    // maybe prefix all with packed.injectorBind
    String operation();

    /**
     * Returns any parent this site may have, or an empty {@link Optional} if this site has no parent.
     * 
     * @return any parent this site may have, or an empty {@link Optional} if this site has no parent
     */
    // Rename to cause????
    Optional<ConfigSite> parent();

    default void print() {
        forEach(e -> System.out.println(e));
    }

    ConfigSite replaceParent(ConfigSite newParent);

    default ConfigSite thenAnnotatedField(String cst, Annotation annotation, FieldDescriptor field) {
        return new AnnotatedFieldConfigSite(this, cst, field, annotation);
    }

    default ConfigSite thenAnnotatedMember(String cst, Annotation annotation, Member member) {
        if (member instanceof MethodDescriptor) {
            return thenAnnotatedMethod(cst, annotation, (MethodDescriptor) member);
        } else {
            return thenAnnotatedField(cst, annotation, (FieldDescriptor) member);
        }
    }

    default ConfigSite thenAnnotatedMethod(String cst, Annotation annotation, MethodDescriptor method) {
        return new AnnotatedMethodConfigSite(this, cst, method, annotation);
    }

    default ConfigSite thenCaptureStackFrame(String cst) {
        if (AbstractConfigSite.STACK_FRAME_CAPTURING_DIABLED) {
            return UNKNOWN;
        }
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(AbstractConfigSite.FILTER).findFirst());
        return sf.isPresent() ? new CapturedStackFrameConfigSite(this, cst, sf.get()) : UNKNOWN;
    }

    /**
     * Visits this configuration site. {@link #visitEach(ConfigSiteVisitor)} will also visit each descendant of this
     * configuration site;
     * 
     * @param visitor
     *            the visitor
     */
    void visit(ConfigSiteVisitor visitor);

    default void visitEach(ConfigSiteVisitor visitor) {
        forEach(s -> s.visit(visitor));
    }

    static ConfigSite captureStack(String cst) {
        // capture stack frame vs capture stack
        // Det eneste er egentlig, om vi vil have en settings saa man kan capture mere end kun en frame..
        // Men saa skal vi ogsaa rette visitoren.
        /// Maaske have en captureStackExtended
        if (AbstractConfigSite.STACK_FRAME_CAPTURING_DIABLED) {
            return UNKNOWN;
        }
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(AbstractConfigSite.FILTER).findFirst());
        return sf.isPresent() ? new CapturedStackFrameConfigSite(null, cst, sf.get()) : UNKNOWN;
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

// Example with Provides
// The exist because the "inject.provides" because of field xxxxx
// This annotation was scanned, because an object was registered at this point
// It was registered in the container xyz

// Actions that returns new configuration site by modifying the old ones.
// replace parent...
// splice.
// withOperation -> Changes the operation
// Many things we can do
