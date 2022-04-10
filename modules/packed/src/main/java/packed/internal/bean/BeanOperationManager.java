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
package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.bean.operation.OperationMirror;

/**
 *
 */
// Taenker vi har realm order?
// Men vi gider ikke sortere med mindre vi skal...

public class BeanOperationManager {

    private final ArrayList<BeanOperationSetup> operations = new ArrayList<>();

    public void addOperation(BeanOperationSetup os) {
        requireNonNull(os);
        operations.add(os);
    }

    public List<OperationMirror> toMirrors() {
        ArrayList<OperationMirror> mirrors = new ArrayList<>();
        for (BeanOperationSetup os : operations) {
            mirrors.add(os.mirror());
        }
        return List.copyOf(mirrors);
    }
    // Altsaa fx exportAll() koere alle services igennem paa all beans og laver en export... Saa det er noget vi goer
    // sent...

    // Et navn er class - Mirror
    /// Hvis den ikke har et mirror laver vi et navn?
}
