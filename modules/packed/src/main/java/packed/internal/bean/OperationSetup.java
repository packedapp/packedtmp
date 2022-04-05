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

import java.util.function.Supplier;

import app.packed.bean.operation.OperationMirror;
import app.packed.extension.Extension;
import packed.internal.bean.inject.DependencyNode;

/**
 *
 */

// Skal vi have flere forskellige????

public final class OperationSetup {

    public final BeanSetup bean;
    
    public final Class<? extends Extension<?>> operator;
    
    public DependencyNode depNode;
    
    public Supplier<? extends OperationMirror> mirrorSupplier;
    // dependencies
    
    public OperationSetup(BeanSetup bean, Class<? extends Extension<?>> operator) {
        this.bean = bean;
        this.operator = operator;
    }
    
    OperationMirror mirror() {
        return mirrorSupplier.get();
    }
}
