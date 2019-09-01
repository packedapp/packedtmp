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
package sec;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public class InMemoryModuleReference extends ModuleReference {

    private final Set<String> list;

    InMemoryModuleReference(ModuleDescriptor descriptor, URI location, Set<String> list) {
        super(descriptor, location);
        this.list = requireNonNull(list);
    }

    @Override
    public ModuleReader open() throws IOException {
        return new ModuleReader() {

            /** {@inheritDoc} */
            @Override
            public void close() {}

            /** {@inheritDoc} */
            @Override
            public Optional<URI> find(String name) throws IOException {
                return list().filter(r -> r.startsWith(location().get().toString())).map(InMemoryModuleReference::uri).filter(r -> r.toString().endsWith(name))
                        .findAny();
            }

            /** {@inheritDoc} */
            @Override
            public Stream<String> list() throws IOException {
                return list.stream();
            }
        };
    }

    public static URI uri(CharSequence uri) {
        try {
            return new URI(uri.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
