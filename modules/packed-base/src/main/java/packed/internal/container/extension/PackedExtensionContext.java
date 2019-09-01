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
package packed.internal.container.extension;

import static java.util.Objects.requireNonNull;

import app.packed.container.extension.Extension;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
public final class PackedExtensionContext {

    final PackedContainerConfiguration pcc;

    public final Extension extension;

    PackedExtensionContext(PackedContainerConfiguration pcc, Extension extension) {
        this.pcc = requireNonNull(pcc);
        this.extension = requireNonNull(extension);
    }

    public static PackedExtensionContext create(Class<? extends Extension> extensionType, PackedContainerConfiguration pcc) {
        Extension e = ExtensionModel.newInstance(extensionType, pcc);
        return new PackedExtensionContext(pcc, e);
    }
}
