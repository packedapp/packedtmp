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
package support.testutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.StackWalker.StackFrame;

import app.packed.config.ConfigSite;

/**
 *
 */
public class ConfigSiteTestHelper {

    public static StackFrame caller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(1).findFirst()).get();
    }

    public static void assertIdenticalPlusLine(StackFrame expected, int lineDifference, ConfigSite cs) {
        int line = expected.getLineNumber();
        assertThat(cs).hasToString(expected.toString().replace(":" + line, ":" + (line + lineDifference)));
    }
}
