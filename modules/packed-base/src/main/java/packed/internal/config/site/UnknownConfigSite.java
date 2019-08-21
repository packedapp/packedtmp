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

import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;

/**
 *
 */
public final class UnknownConfigSite extends AbstractConfigSite {

    public static final UnknownConfigSite INSTANCE = new UnknownConfigSite();

    private UnknownConfigSite() {
        super(null, "Unknown");
    }

    @Override
    public ConfigSite replaceParent(ConfigSite newParent) {
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
}
