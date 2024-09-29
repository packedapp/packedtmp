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
package app.packed.concurrent;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.binding.KeyClassifier;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionMirror;
import internal.app.packed.util.QualifierUtil;

/**
 *
 */
// Not sure I want this...
// Do
public final class ThreadExtensionMirror extends ExtensionMirror<ThreadExtension> {

    /**
     * @param handle
     */
    protected ThreadExtensionMirror(ExtensionHandle<ThreadExtension> handle) {
        super(handle);
    }

    static final Function<String, KeyClassifier> FT = QualifierUtil.syntheticFunction(MethodHandles.lookup(), KeyClassifier.class, String.class, "value");
}
