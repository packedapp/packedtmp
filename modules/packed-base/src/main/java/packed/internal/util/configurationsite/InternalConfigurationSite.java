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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;

import app.packed.util.ConfigurationSite;

/**
 *
 */
public interface InternalConfigurationSite extends ConfigurationSite {

    default InternalConfigurationSite spawnStack(ConfigurationSiteType cst) {
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(e -> e.filter(f -> !f.getClassName().startsWith("app.packed")).findFirst());
        return new ProgrammaticConfigurationSite(this, cst, sf);
    }

    static InternalConfigurationSite ofStack(ConfigurationSiteType cst) {
        Optional<StackFrame> sf = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(e -> e.filter(f -> !f.getClassName().startsWith("app.packed")).findFirst());
        return new ProgrammaticConfigurationSite(null, cst, sf);
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