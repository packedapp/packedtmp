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
package packed.internal.inject.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.service.ServiceLocator;

/**
 *
 */
class Z {

    public static void main(ServiceLocator sl) {

        List<CharSequence> l = new ArrayList<>();
        sl.selectWithAnyQualifiers(String.class).forEachInstance((s, i) -> l.add(i));
        Map<Integer, String> m = new HashMap<>();
        sl.selectWithAnyQualifiers(String.class).forEachInstance((s, st) -> m.put(s.attributes().keys().size(), st));

    }
}
