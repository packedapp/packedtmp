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
package internal.app.packed.bean.scanning;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.extension.Extension;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.util.handlers.BeanHandlers;

/**
 * An instance of this class is created for each extension that participates in the introspection of a single bean.
 * <p>
 * The main purpose of the class is to make sure that the extension points to the same bean introspector for the whole
 * of the introspection.
 */
public final class BeanIntrospectorSetup implements Comparable<BeanIntrospectorSetup> {

    /** The extension instance the bean introspector is a part of, lazily initialized. */
    private ExtensionSetup extension;

    /** The extension the introspector is part of. */
    public final Class<? extends Extension<?>> extensionClass;

    /** Whether or not the introspector has full access (get, set, invoke). */
    private boolean hasFullAccess;

    /** The bean introspector instance. */
    final BeanIntrospector<?> introspector;

    /** The bean scanner. */
    public final BeanScanner scanner;

    private BeanIntrospectorSetup(Class<? extends Extension<?>> extensionClass, BeanScanner scanner, BeanIntrospector<?> introspector) {
        this.extensionClass = extensionClass;
        this.introspector = introspector;
        this.scanner = scanner;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(BeanIntrospectorSetup o) {
        return extension.compareTo(o.extension);
    }

    public ExtensionSetup extension() {
        ExtensionSetup e = extension;
        if (e == null) {
            e = extension = scanner.bean.container.useExtension(extensionClass, null);
        }
        return e;
    }

    public boolean hasFullAccess() {
        return hasFullAccess;
    }

    static BeanIntrospectorSetup create(BeanScanner scanner, BeanIntrospectorClassModel bim) {
        BeanIntrospector<?> introspector = bim.newInstance();

        BeanIntrospectorSetup setup = new BeanIntrospectorSetup(bim.extensionClass, scanner, introspector);

        // Call BeanIntrospector#initialize(BeanIntrospectorSetup)
        BeanHandlers.invokeBeanIntrospectorInitialize(introspector, setup);

        // Notify the bean introspector that it is being used
        introspector.onStart();

        return setup;
    }
}
