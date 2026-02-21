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
package app.packed.concurrent;

import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.namespaceold.OldNamespaceConfiguration;
import app.packed.namespaceold.OldNamespaceHandle;
import app.packed.service.ProvidableBeanConfiguration;

/**
 *
 */
// Can you have more than 1 compute in a JobNamespace
// Can you have multiple lifetimes in a JobNamespace? Or only 1?
// I think multiple is fine. I mean we support transient jobs which is kind of limited lifetimes
public class JobNamespaceConfiguration extends OldNamespaceConfiguration<BaseExtension> {

    protected JobNamespaceConfiguration(OldNamespaceHandle<BaseExtension, ?> namespace, BaseExtension extension, ComponentRealm actor) {
        super(namespace, extension, actor);
    }


    // Ideen er vel at man kan injected det et sted
    // Men altsaa kan man bruge det uden configuration???? BeanParams?
    // How do you get params into a bean from the outside?
    // Omvendt

    // 1.
    // Interface {  int sum(int p1, int p2) }  ->> @Compute must have this signature (incl exceptions)
    // Maaske den endda skal implementere den???
    // Saa frameworked laver beanen, kalder metoden, cleaner beanen op, og returner vaerdien
    // Til gengaeld bliver den registeret som et job

    // Tror vi bliver noedt til at tillade injection af JobContext
    // Saa Maake maa de match de x foerst eller x sidste parametere.

    public <T> ProvidableBeanConfiguration<T> installComputableJob(Class<? extends T> bean, Class<T> samType) {
        // Params bliver injected som service parameter i bean'en individuelt (ikke paa operationen)
        // Classen bliver saa genereret
        throw new UnsupportedOperationException();
        // Tilfoeje GenericType tror jeg, og maaske OP
    }
}
