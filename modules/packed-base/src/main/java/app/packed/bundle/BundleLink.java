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
package app.packed.bundle;

import app.packed.config.ConfigSite;
import app.packed.util.Attachable;

/**
 *
 */
// As an alternativ use attachments....
// Yes this is exactly what they are made for....

// Deploy
// Execute
// Link (link = container, wire = any)

// The first operation we do in a Container could link().. So we know nothing about the parent.
// Other than it is either a host or a Bundle...
public interface BundleLink extends Attachable {

    /**
     * Returns the place where the bundle was linked.
     * 
     * @return the place where the bundle was linked
     */
    ConfigSite configSite();

    // parent (permanent) secrets
    // child (permanent) secrets

    // Link secrets (temporary) (Link secrets have there own tmp hierachi I think)
    // D.v.s. top level containeren. Hvis de ikke har en faelles top level extension...
    // Saa kan de ikke kommunikere....

    // Vi kan ikke returnere Bundle. Saa vil vi finde en der finder paa at kalde BundleDescriptor.of()....
    Class<? extends AnyBundle> childType();

    // Use what??? Only think we have secrets... Vi kan jo ikke bare hive en extension ud, og begynde at modificere den...
    // Eller d.v.s. de skulle gerne vaere Unconfigurable....Men alligevel saa er der vel noget information vi ikke vil have
    // ud... F.eks. alle interne services...
    // <T> T use(Class<T> clazz);

    // AnyBundleDescriptor from();
    // AnyBundleDescriptor to();

    // onParent, onChild, maa have mulighed for installere en service af en eller anden form
    // Som vi saa kan hive ud her

    // WiringOperations, could be an abstract class taking Requirements in the constructor...
    // Or the object we use could have an requiresExtension
    // Hvilket foerst kan checkes i postProcess() fordi vi bliver noedt til at configurere bundlen foerst...

    Mode mode();

    enum Mode {

        /** Used when deploying */
        DEPLOY,

        EXECUTE,

        LINK;

        public void checkDeploy() {
            // List belastende vi ikke kan skrive f.eks. FFF Wiring operations requires DeployMode
        }

        public void checkExecute() {}

        public void checkLink() {}
    }
}
