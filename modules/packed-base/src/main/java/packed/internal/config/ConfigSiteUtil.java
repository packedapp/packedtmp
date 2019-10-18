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
package packed.internal.config;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;

import app.packed.config.ConfigSite;

/**
 *
 */
public class ConfigSiteUtil {

    // TODO maybe people need to implement this them self???
    public static ConfigSite captureStackFrame(ConfigSite previous, String operation) {
        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(ConfigSiteSupport.FILTER).findFirst());
        return sf.isPresent() ? new ConfigSiteSupport.StackFrameConfigSite(previous, operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    public static ConfigSite captureStackFrame(String operation) {
        // capture stack frame vs capture stack
        // Det eneste er egentlig, om vi vil have en settings saa man kan capture mere end kun en frame..
        // Men saa skal vi ogsaa rette visitoren.
        /// Maaske have en captureStackExtended
        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(e -> e.filter(ConfigSiteSupport.FILTER).findFirst());
        return sf.isPresent() ? new ConfigSiteSupport.StackFrameConfigSite(null, operation, sf.get()) : ConfigSite.UNKNOWN;
    }
}
