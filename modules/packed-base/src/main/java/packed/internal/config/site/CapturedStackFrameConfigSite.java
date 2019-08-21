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

import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;

/** A programmatic configuration site from a {@link StackFrame}. */
public class CapturedStackFrameConfigSite extends AbstractConfigSite {

    /** The stack frame. */
    private final StackFrame stackFrame;

    /**
     * @param parent
     * @param operation
     */
    public CapturedStackFrameConfigSite(ConfigSite parent, String operation, StackFrame caller) {
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
    public ConfigSite replaceParent(ConfigSite newParent) {
        return new CapturedStackFrameConfigSite(newParent, super.operation, stackFrame);
    }

    /** {@inheritDoc} */
    @Override
    public void visit(ConfigSiteVisitor visitor) {
        visitor.visitCapturedStackFrame(this);
    }
}