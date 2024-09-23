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

import app.packed.bean.BeanIntrospector;
import internal.app.packed.extension.ExtensionSetup;

/**
 * An instance of this class is created for each extension that participates in the introspection of a single bean.
 * <p>
 * The main purpose of the class is to make sure that the extension points to the same bean introspector for the whole
 * of the introspection.
 */
public final class BeanScannerParticipant implements Comparable<BeanScannerParticipant> {

    /** The extension instance. */
    public final ExtensionSetup extension;

    boolean hasFullAccess;

    /** A bean introspector provided by the extension via {@link Extension#newBeanIntrospector} */
    final BeanIntrospector introspector;

    public final BeanScanner scanner;

    BeanScannerParticipant(BeanScanner scanner, ExtensionSetup extension, BeanIntrospector introspector) {
        this.extension = extension;
        this.introspector = introspector;
        this.scanner = scanner;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(BeanScannerParticipant o) {
        return extension.compareTo(o.extension);
    }

    public boolean hasFullAccess() {
        return hasFullAccess;
    }
}
