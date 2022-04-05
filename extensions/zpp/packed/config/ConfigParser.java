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

import java.io.InputStream;

/**
 *
 */
// Installed via ServiceLoader...

// Support one or more filename extensions (.conf, .properties, ....)
// Maybe we just support property files out of the box....
abstract class ConfigParser {
    static final ConfigParser PROPERTY_FILES = null;

    // Throw IOException or ConfigException???
    Config parse(InputStream is) {
        throw new UnsupportedOperationException();
    }

    interface ConfigParserOption {
        static ConfigParserOption doobar() {
            throw new UnsupportedOperationException();
        }

//        static ConfigParserOption traceWith(Trace tracer) {
//            throw new UnsupportedOperationException();
//        }
    }
}
// Should we support serialization also?????????? ConfigMarshaller

// app.packed.base

// Properties (.properties) are supported out of the box...

// app.packed.config.hcl (embeds snake yaml...)
// app.packed.config.yaml (embeds snake yaml...)
// app.packed.config.json (embeds snake yaml...)
// app.packed.config.xml (embeds snake yaml...)

/// Syntes vi skal have nogle interfaces som de kan implemententere.
// app.packed.xml

// Ville vaere ubercool at supportere, f.eks. at man aendre port nummeret i sin configurationsfil. Men ikke

// Don't know how to parse

class ConfigTransformer {}
