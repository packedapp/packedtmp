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
package packed.internal.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class Fff {
    public static void main(String[] args) throws URISyntaxException {
        System.out.println(Fff.class.getModule());
        System.out.println(Fff.class.getModule().getClassLoader());

        System.out.println(Integer.class.getModule().getDescriptor());

        URI resource = Fff.class.getResource("/modules").toURI();

    }
}
