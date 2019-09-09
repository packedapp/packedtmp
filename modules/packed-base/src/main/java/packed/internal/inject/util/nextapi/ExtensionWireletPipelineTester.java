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

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletPipeline;

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

class MyExtensionNode extends ExtensionNode<MyExtension> {

    final MyExtension extension;

    /**
     * @param context
     */
    protected MyExtensionNode(MyExtension extension, ExtensionContext context) {
        super(context);
        this.extension = extension;
    }
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
class MyExtensionWireletPipeline extends ExtensionWireletPipeline<MyExtensionNode> {

    String name;

    /**
     * @param extension
     */
    MyExtensionWireletPipeline(MyExtensionNode extension) {
        super(extension);
    }

    MyExtensionWireletPipeline(MyExtensionWireletPipeline previous) {
        super(previous.node());
        this.name = previous.name;
    }

    String getName() {
        String n = name;
        return n == null ? node().extension.name : n;
    }
}
