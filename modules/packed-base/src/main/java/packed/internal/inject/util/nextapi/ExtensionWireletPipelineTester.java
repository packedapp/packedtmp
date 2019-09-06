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
package packed.internal.inject.util.nextapi;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionPipeline;
import app.packed.container.extension.ExtensionWirelet;

/**
 *
 */
public class ExtensionWireletPipelineTester {

    public static void main(String[] args) {

    }
}

class MyExtension extends Extension {
    String name;

}

// Wirelets must be immutable....
class MyExtensionWirelet extends ExtensionWirelet<MyExtensionWireletPipeline> {

    private String newName;

    /** {@inheritDoc} */
    @Override
    protected void process(MyExtensionWireletPipeline context) {
        context.name = newName;
    }
}

//// Supportere aldrig mere end en type per extension.....
class MyExtensionWireletPipeline extends ExtensionPipeline<MyExtensionWireletPipeline> {

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
    protected MyExtensionWireletPipeline split() {
        return new MyExtensionWireletPipeline(extension, name);
    }
}

abstract class EW<T extends ExtensionPipeline<?>> extends Wirelet {

}

class W extends EW<MyExtensionWireletPipeline> {

}
