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
package app.packed.namespaceold;

/**
 *
 */


// Maybe it generic?? For example, who can configure Application??
// Thread locals are not great for this. As a user we can call into an extension that then has application authority
// Can be restricted on Container, Authority

// BuildPermission  (Fx DatabaseExtension NoInstall)

// Can we have metadata???

// AddExtension(FF


// Build it, Customs
// Cannot name a bean X <--- That is a custom one I would believe

// Maybe we have 2.
//// InstructionPermission
//// MirrorVerification (post build we validate it)


//// I think we are always open for adding permission restrictions. No actually open is needed
// Permissions -> Something that is checked, before an "operation". Limited Local Knowledge
// Verifacation -> Something is checked after the application has been built. Full Application Knowledge

////// No container should have more than 3 beans. I mean that is not a good instruction mechanism...
////// When should we validate it? Efter time a new bean is added? How would you even specificy it
////// For transformers, you would get onInstall() {} containerConfigurations.beans().size()==3 -> Fail

// Can we connect them to locals???


// Extension + Name

// Do permission have a component path?? Nahh, not really needed

// Are these binary??? Yes/No

// BuildPermission

// It makes sense to be kind of logging points as well
// install Bean
public class BuildPermission {

    public static final BuildPermission CONFIGURE = null;


    // Then we can check shit
}

// ContainerExtension.InstallExtension()