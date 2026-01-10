/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.build.action;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.namespace.sandbox.BuildPermission;

/**
 * Can output logging
 *
 * Can support Mirrors
 *
 * Can check permissions
 *
 * Can spawn build threads?
 */
public class BuildActionTemplate {

    public String name;

    BuildPermission permission;

    // Would use ThreadLocals
    public BuildAction start() {
        throw new UnsupportedOperationException();
    }

    // Would use ThreadLocals
    public NestedBuildAction startCloseable() {
        throw new UnsupportedOperationException();
    }

    static BuildActionTemplate of(String name) {
        throw new UnsupportedOperationException();
    }

    public interface Builder {

        // Ideen er lidt hvor vigtigt er det her build step...
        // Om vi skal gemme info
        Builder lavel(int level);

        // I don't know if we need to specify it
        Builder needsMultipleThreads();

        // Allows the framework to time the thing.
        // Would it make sense to time the sub components???
        // I mean if you load 100 small things, it would be nice to see what takes time
        // We are probably always dominated by class loading
        Builder supportTiming();
    }
}

// We need start up

class MyExt extends NewBaseExtension<MyExt> {

    /**
     * @param handle
     */
    protected MyExt(ExtensionHandle<MyExt> handle) {
        super(handle);
    }

    static final BuildActionTemplate UPDATE_NAME = BuildActionTemplate.of("UpdateName");

    String name;

    public void loadFoo(String name) {
        start(UPDATE_NAME).logTrace("updateName {s}", name);
    }

    public void setName(String name) {
        try (var ba = startClosable(UPDATE_NAME)) {
            this.name = name;
            ba.logTrace("updateName {s}", name);
        }
    }

    public void setName2(String name) {
        try (var _ = startClosable(UPDATE_NAME).log("updateName {s}", name)) {
            this.name = name;
        }
    }

    // Now we use ScopedValue
    public void setName4(String name) {
        try (var ba = UPDATE_NAME.startCloseable()) {
            this.name = name;
            ba.logTrace("updateName {s}", name);
        }
    }

    public void setName5(String name) {
        try (var _ = UPDATE_NAME.startCloseable().logTrace("updateName {s}", name)) {
            this.name = name;
        }
    }
}

class NewBaseExtension<E extends NewBaseExtension<E>> extends Extension<E> {

    /**
     * @param handle
     */
    protected NewBaseExtension(ExtensionHandle<E> handle) {
        super(handle);
    }

    protected BuildAction start(BuildActionTemplate ba) {
        throw new UnsupportedOperationException();
    }

    // Giver logging, giver permissions
    // Will check that the assembly is still open
    protected NestedBuildAction startClosable(BuildActionTemplate ba) {
        throw new UnsupportedOperationException();
    }
}