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
import java.util.Optional;
import java.util.function.Predicate;

import app.packed.config.ConfigSite;

/** An abstract implementation of {@link ConfigSite}. */
public abstract class AbstractConfigSite implements ConfigSite {

    final String operation;

    final ConfigSite parent;

    public static final Predicate<StackFrame> FILTER = f -> !f.getClassName().startsWith("app.packed.") && !f.getClassName().startsWith("packed.")
            && !f.getClassName().startsWith("java.");

    public static final boolean STACK_FRAME_CAPTURING_DIABLED = false;

    AbstractConfigSite(ConfigSite parent, String operation) {
        this.parent = parent;
        this.operation = requireNonNull(operation);
    }

    /** {@inheritDoc} */
    @Override
    public final String operation() {
        return operation;
    }

    @Override
    public final Optional<ConfigSite> parent() {
        return Optional.ofNullable(parent);
    }

    // @Override
    // public String toString() {
    // if (!caller.isPresent()) {
    // return "<No Info>";
    // }
    // StringBuilder sb = new StringBuilder();
    // int i = 0;
    // ARegistrationPoint a = this;
    // while (a != null) {
    // for (int j = 0; j < i; j++) {
    // sb.append(" ");
    // }
    // sb.append(a.captureType.toString()).append(" -> ");
    // // sb.append(a.getCaller().get());
    // a = a.parent;
    // sb.append("\n");
    // }
    // return sb.toString();
    // }

}
