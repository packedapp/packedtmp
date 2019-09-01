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

import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Predicate;

import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;

/** The various implements of {@link ConfigSite}. */
public interface ConfigSiteSupport {

    Predicate<StackFrame> FILTER = f -> !f.getClassName().startsWith("app.packed.") && !f.getClassName().startsWith("packed.")
            && !f.getClassName().startsWith("java.");

    boolean STACK_FRAME_CAPTURING_DIABLED = false;

    /** A config site for an annotated method.s */
    public static final class AnnotatedMethodConfigSite implements ConfigSite {

        /** The annotation value. */
        private final Annotation annotation;

        /** The annotated method. */
        private final MethodDescriptor method;

        /** The operation */
        private final String operation;

        /** Any parent config site. */
        @Nullable
        private final ConfigSite parent;

        public AnnotatedMethodConfigSite(@Nullable ConfigSite parent, String operation, MethodDescriptor method, Annotation annotation) {
            this.parent = parent;
            this.operation = requireNonNull(operation, "operation is null");
            this.method = requireNonNull(method, "method is null");
            this.annotation = requireNonNull(annotation, "annotation is null");
        }

        /** {@inheritDoc} */
        @Override
        public final String operation() {
            return operation;
        }

        /** {@inheritDoc} */
        @Override
        public final Optional<ConfigSite> parent() {
            return Optional.ofNullable(parent);
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite replaceParent(@Nullable ConfigSite newParent) {
            return new AnnotatedMethodConfigSite(newParent, operation, method, annotation);
        }

        /** {@inheritDoc} */
        @Override
        public void visit(ConfigSiteVisitor visitor) {
            visitor.visitAnnotatedMethod(this, method, annotation);
        }
    }

    /** A configuration site originating from an annotated method. */
    public static final class AnnotatedFieldConfigSite implements ConfigSite {

        /** The annotated field. */
        private final FieldDescriptor field;

        /** The annotation value. */
        private final Annotation annotation;

        /** The operation */
        private final String operation;

        /** Any parent config site. */
        @Nullable
        private final ConfigSite parent;

        public AnnotatedFieldConfigSite(@Nullable ConfigSite parent, String operation, FieldDescriptor field, Annotation annotation) {
            this.parent = parent;
            this.operation = requireNonNull(operation, "operation is null");
            this.field = requireNonNull(field, "field is null");
            this.annotation = requireNonNull(annotation, "annotation is null");
        }

        /** {@inheritDoc} */
        @Override
        public final String operation() {
            return operation;
        }

        /** {@inheritDoc} */
        @Override
        public final Optional<ConfigSite> parent() {
            return Optional.ofNullable(parent);
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite replaceParent(ConfigSite newParent) {
            return new AnnotatedFieldConfigSite(newParent, operation, field, annotation);
        }

        /** {@inheritDoc} */
        @Override
        public void visit(ConfigSiteVisitor visitor) {
            visitor.visitAnnotatedField(this, field, annotation);
        }

        // toString
        // x.op because of @Completes on field fox.sss.dd#sd
        // install.component[/sd/qwzzz] at sdkdkdk.sdsd.Dsksks:2323 .....Ahhh, saa skal man beholde references til registrerings
        // pointet????
        // Bundle.configure at xxxx:2323
        // embed
        // dkdkd
        // sdsd
        // App.of

        // CompletionSite.. for lifecycle as ConfigSite for configuration???
    }

    /** An unknown config site */
    public static final class UnknownConfigSite implements ConfigSite {

        /** The singleton. */
        public static final UnknownConfigSite INSTANCE = new UnknownConfigSite();

        /** {@inheritDoc} */
        @Override
        public ConfigSite replaceParent(ConfigSite newParent) {
            return UNKNOWN;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Unknown";
        }

        /** {@inheritDoc} */
        @Override
        public void visit(ConfigSiteVisitor visitor) {
            visitor.visitUnknown(this);
        }

        /** {@inheritDoc} */
        @Override
        public String operation() {
            return "Unknown";
        }

        /** {@inheritDoc} */
        @Override
        public Optional<ConfigSite> parent() {
            return Optional.empty();
        }
    }
}
