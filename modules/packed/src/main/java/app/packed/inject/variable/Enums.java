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
package app.packed.inject.variable;

/**
 *
 */
public class Enums {
    
    // FUNCTIONHOOK??? Vi har ikke behov for annotering... eftersom de altid er programmatic
    enum HOOKTYPE { CLASS, METHOD, CONSTRUCTOR, FIELD, DEPENDENCY }
    
    enum STATEFULLNESS {STATELESS, STATEFULL}
    
    enum STAGE { BOOTSTRAP, BUILD, RUNTIME }
    
    enum SCOPE {CLASS, CLASS_INSTANCE, BEAN, BEAN_INSTANCE}
    enum SCOPE2 {COMPONENT, COMPONENT_INSTANCE}
    
    enum FUNCTIONALITY { PROVIDE_VALUE, ON_LIFECYCLE, STORE_DATA}
    
    // COMMUNICATION Extension|ExtensionBean<->Hooks<->Hooks<->UserBean
}
