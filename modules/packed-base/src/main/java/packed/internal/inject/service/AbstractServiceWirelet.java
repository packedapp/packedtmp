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
package packed.internal.inject.service;

import app.packed.block.ExtensionMember;
import app.packed.component.Wirelet;
import app.packed.inject.ServiceExtension;

/**
 *
 */

// Grunden til jeg gerne vil lave en enkelt er at der er nogle wirelets
// Der kun er to, nogle der er from og nogle der er begge dele...
// F.eks. contracts...

@ExtensionMember(ServiceExtension.class)
public abstract class AbstractServiceWirelet extends Wirelet {

    protected void processTo(ServiceBuildManager m) {}

    protected void processFrom(ServiceBuildManager m) {}

}
