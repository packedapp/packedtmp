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
package packed.internal.bean;

import app.packed.bean.operation.OperationMirror;
import packed.internal.bean.inject.DependencyNode;
import packed.internal.container.ExtensionSetup;

/**
 *
 */

// Skal vi have flere forskellige????

public final class OperationSetup {

    public final BeanSetup bean;
    
    public final ExtensionSetup operator;
    
    public DependencyNode depNode;
    
    // dependencies
    
    OperationSetup(BeanSetup bean, ExtensionSetup operator) {
        this.bean = bean;
        this.operator = operator;
    }
    
    OperationMirror mirror() {
        throw new UnsupportedOperationException();
    }
}
