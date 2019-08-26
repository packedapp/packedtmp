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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public class ExtensionWireletPipelineTester {

}

class MyExtension extends Extension {
    String name;
}

// Wirelets must be immutable....
class MyExtensionWirelet extends ExtensionWirelet<MyExtension, MyExtensionWireletPipeline> {

    private String newName;

    /** {@inheritDoc} */
    @Override
    protected MyExtensionWireletPipeline newPipeline(MyExtension extension) {
        return new MyExtensionWireletPipeline(extension);
    }

    /** {@inheritDoc} */
    @Override
    protected void process(MyExtensionWireletPipeline context) {
        context.name = newName;
    }
}

//// Supportere aldrig mere end en type per extension.....
class MyExtensionWireletPipeline extends ExtensionWireletPipeline<MyExtension> {

    String name;
    final MyExtension extension;

    /**
     * @param extension
     */
    protected MyExtensionWireletPipeline(MyExtension extension) {
        this.extension = requireNonNull(extension);
    }

    protected MyExtensionWireletPipeline(MyExtension extension, String name) {
        this.extension = requireNonNull(extension);
    }

    String getName() {
        String n = name;
        return n == null ? extension.name : n;
    }

    /** {@inheritDoc} */
    @Override
    protected ExtensionWireletPipeline<MyExtension> split() {
        return new MyExtensionWireletPipeline(extension, name);
    }
}