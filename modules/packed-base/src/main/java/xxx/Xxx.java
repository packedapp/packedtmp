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
package xxx;

import app.packed.bundle.Bundle;
import xxx.Xxx.PModule.DBService;

/**
 *
 */
public class Xxx {

    static class PModule {
        static class PBundle extends Bundle {
            /** {@inheritDoc} */
            @Override
            protected void configure() {
                bind(DBService.class);
            }
        }

        static class DBService {}
    }

    static class DModule {
        static class DBundle extends Bundle {
            /** {@inheritDoc} */
            @Override
            protected void configure() {
                requireService(DBService.class); // AutoRequireServices

                bind(UsesDB.class);
            }
        }

        static class UsesDB {
            UsesDB(DBService s) {

            }
        }
    }

}
