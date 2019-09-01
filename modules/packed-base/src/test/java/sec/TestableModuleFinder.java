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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 */
public class TestableModuleFinder implements ModuleFinder {

    private static final String MODULE_INFO_CLASS = "module-info.class";

    private final TestableClassLoader loader;

    public TestableModuleFinder(TestableClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public Optional<ModuleReference> find(String name) {
        return findAll().stream().filter(m -> name.equals(m.descriptor().name())).findAny();
    }

    @Override
    public Set<ModuleReference> findAll() {
        Enumeration<URL> resources = loader.getResources(MODULE_INFO_CLASS, false);

        Set<ModuleReference> references = new HashSet<>();
        while (resources.hasMoreElements()) {
            URL moduleInfo = resources.nextElement();
            ModuleDescriptor descriptor = parseModuleInfoByteCode(moduleInfo);
            URI uri = extractBaseUri(moduleInfo);
            Set<String> list = resourcesForThisModule(uri);
            references.add(new InMemoryModuleReference(descriptor, uri, list));

        }
        return references;
    }

    private Set<String> resourcesForThisModule(URI baseUri) {
        return Set.copyOf(loader.resources.keySet());
        // return loader.resources().map(Object::toString).filter(r ->
        // r.startsWith(baseUri.toString())).collect(Collectors.toSet());
    }

    private URI extractBaseUri(URL moduleInfo) {
        String url = moduleInfo.toString();
        return InMemoryModuleReference.uri(url.substring(0, url.length() - MODULE_INFO_CLASS.length()));
    }

    private ModuleDescriptor parseModuleInfoByteCode(URL moduleInfo) {
        try {
            try (var s = moduleInfo.openStream()) {
                return ModuleDescriptor.read(s);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
