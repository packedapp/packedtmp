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
package zpp.packed.config;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import packed.internal.config.ConfigSite;
import packed.internal.config.ConfigSiteVisitor;
import packed.internal.util.StringFormatter;

/**
 *
 */
final class ConfigSiteVisitors {

    static final class DefaultFormatting implements ConfigSiteVisitor {

        private final StringBuilder sb;

        /**
         * @param sb
         */
        DefaultFormatting(StringBuilder sb) {
            this.sb = requireNonNull(sb, "sb is null");
        }

        /** {@inheritDoc} */
        @Override
        public void visitAnnotatedMethod(ConfigSite configSite, Method method, Annotation annotation) {
            Optional<ConfigSite> parent = configSite.parent();
            if (parent.isPresent()) {
                sb.append(parent.get());
                sb.append(" via annotated method ");
            }
            sb.append("@");
            sb.append(annotation.annotationType().getSimpleName());
            sb.append(" ");
            sb.append(StringFormatter.formatShortWithParameters(method));
        }

        /** {@inheritDoc} */
        @Override
        public void visitStackFrame(ConfigSite configSite, StackFrame stackFrame) {
            sb.append(configSite.toString());
        }

        /** {@inheritDoc} */
        @Override
        public void visitUnknown(ConfigSite configSite) {
            sb.append(configSite.toString());
        }

    }
}
