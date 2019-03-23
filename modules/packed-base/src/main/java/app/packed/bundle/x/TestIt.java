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
package app.packed.bundle.x;

import app.packed.bundle.Bundle;
import app.packed.bundle.Layer;

/**
 *
 */

/// Disse 3 koncepter skal passe.....
// Services
// Lifecycle
// Hooks
public class TestIt extends Bundle2 {
    public static final Layer EMPTY = new Layer();

    public static final Layer BUNDLE = new Layer();

    @Override
    protected void configure() {
        Layer logLayer = layer(new LoggingBundle(), EMPTY);
        layer(new DatabaseBundle(), logLayer);
    }

    protected void configureExsplicit() {
        // Hvordan siger vi saa vil gerne bruge toplaget???
        // 3 metoder -> Tag fra top laget, giv til toplaget, begge dele...

        Layer logLayer = layer(new LoggingBundle());
        use(layer(new DatabaseBundle(), logLayer));

        wire(new LoggingBundle());
        wire(new DatabaseBundle()).inLayer(newLayer("DatabaseLayer"));
    }

    protected void all() {
        // TakeFrom Top Layer
        {
            layer(new LoggingBundle());
        }
        // Give to Top Layer
        {
            use(layer(new LoggingBundle(), EMPTY));
        }
        // Both
        {
            use(layer(new LoggingBundle()));
        }
    }

    protected void alternatice() {
        // TakeFrom Top Layer
        {
            layer(new LoggingBundle(), BUNDLE);
        }
        // Give to Bundle Layer
        {
            use(layer(new LoggingBundle()));
        }
        // Both
        {
            use(layer(new LoggingBundle(), BUNDLE));
        }
    }

    protected void alternatice2() {
        // TakeFrom Top Layer
        {
            layer(new LoggingBundle(), BUNDLE);
        }
        // Give to Bundle Layer
        {
            useLayer(new LoggingBundle());
        }
        // Both
        {
            useLayer(new LoggingBundle(), BUNDLE);
        }

        // useLayer(new LoggingBundle(), BundleLayer.BUNDLE); ~= wire(new LoggingBundle());
        // Men wire laver ikke sit eget lag....

        // Saa vi har explicitte parent!!!!! Saa tit skal vi heller ikke lave lag
        // Maaske kan man specificere hvad der maa komme ind.... F.eks. kun configuration
    }

    static class LoggingBundle extends Bundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {}
    }

    static class DatabaseBundle extends Bundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {}

    }
}
