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
package internal.app.packed.operation.binding;

import app.packed.bindings.BindingMirror;
import app.packed.container.Realm;
import app.packed.extension.BaseExtension;
import internal.app.packed.application.PackedBridge;

/**
 *
 */
public class CompanionBinding  {

    PackedBridge<?> bridge;

    // Vi bygger en application
    
    // ApplicationDriver.wrapperBean
    // for each companion dependency..
    // Lav noget der tager (Object[]) -> Arg)
    
    // Altsaa Applicationen skal jo bare lave saadan en MH????
    
    // Man maa lave application foerst.
    // Foerend man kan lave launch metoden
    //// 
    
    // UserSpace, ExtensionSpace, FrameworkSpace
    
    
    // Kan vel kun paa ting man direkte depender paa?
    

    /** {@inheritDoc} */
    public Realm boundBy() {
        return Realm.extension(BaseExtension.class);
    }

    /** {@inheritDoc} */
    protected BindingMirror mirror0() {
        throw new UnsupportedOperationException();
    }

}
