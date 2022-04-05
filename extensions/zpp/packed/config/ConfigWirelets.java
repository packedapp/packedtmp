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
package zpp.packed.config;

import java.lang.module.Configuration;

import app.packed.container.Wirelet;

/**
 *
 */
public class ConfigWirelets {

    public static Wirelet configure(Configuration c) {
        // This is for App, but why not for Injector also...
        // we need config(String) for wire()..... configOptional() also maybe...
        // Would be nice.. if config extends WiringOperations
        // alternative c.wire();
        // c.get("/sdfsdf").wire();

        // Maaske skal nogle klasser bare implementere WiringOperation...
        throw new UnsupportedOperationException();
    }

    // wire(new XBundle(), Wirelet.configure(Configuration.read("dddd");
    // mapConfiguration, childConfiguration()
    // ConfigurationWirelets.provide(Configuration c)
    // ConfigurationWirelets.map(ConfigurationTransformer transformer)
    // or extract childOf
    // ConfigurationWirelets.mapChild(String childName) //calls map
    // ConfigurationWirelets.mapChild(Configuration c, ConfigurationTransformer)
    // ConfigWirelets.provide(c)
    // ConfWirelets.provide(c)
    // ConfSite

    public static Wirelet configure(Configuration c, String child) {
        // configure(c, "child")
        throw new UnsupportedOperationException();
    }

    public static Wirelet configure(String childName) {
        // Extracts the child named 'childName' for a configuration in the current context
        // configure(c, "child")
        throw new UnsupportedOperationException();
    }

    // protected void validate(); Validates that the operation can be used

    // I think we have a special ContainerWirelet in package private package
    // This is a pre
    static Wirelet disableStackCapturing() {
        // Man skal vel ogsaa kunne enable den igen....
        throw new UnsupportedOperationException();
    }
}
