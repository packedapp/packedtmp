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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.container.Wirelet;

/** An operation that combines multiple operations. */
public final class WireletList extends Wirelet {

    /** The stages that have been combined */
    private final List<Wirelet> wirelets;

    private WireletList(Wirelet... wirelets) {
        this.wirelets = List.of(requireNonNull(wirelets, "wirelets is null"));
    }

    public List<Wirelet> list() {
        return wirelets;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(wirelets.get(0));
        for (int i = 1; i < wirelets.size(); i++) {
            sb.append(", ").append(wirelets.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    static List<Wirelet> operationsExtract(Wirelet[] operations, Class<?> type) {
        requireNonNull(operations, "operations is null");
        if (operations.length == 0) {
            return List.of();
        }
        ArrayList<Wirelet> result = new ArrayList<>(operations.length);
        for (Wirelet s : operations) {
            requireNonNull(s, "The specified array of operations contained a null");
            operationsExtract0(s, type, result);
        }
        return List.copyOf(result);
    }

    private static void operationsExtract0(Wirelet o, Class<?> type, ArrayList<Wirelet> result) {
        if (o instanceof WireletList) {
            for (Wirelet ies : ((WireletList) o).wirelets) {
                operationsExtract0(ies, type, result);
            }
        } else {
            result.add(o);
        }
    }

    public static WireletList of(Wirelet... wirelets) {
        return new WireletList(wirelets);
    }

}