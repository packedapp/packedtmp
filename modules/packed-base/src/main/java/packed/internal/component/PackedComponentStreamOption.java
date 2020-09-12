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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import app.packed.base.Nullable;
import app.packed.component.ComponentStream;
import app.packed.container.Extension;

/**
 *
 */
// Kopiere lidt paenere kode fra jdk.nashorn.internal.runtime.regexp.joni.Option
public final class PackedComponentStreamOption implements ComponentStream.Option {

    private static final int EXCLUDE_ORIGIN = 1;

    private static final int INCLUDE_EXTENSIONS = 2;

    private static final int IN_SAME_CONTAINER = 4;

    public static final PackedComponentStreamOption DEFAULT_OPTION = new PackedComponentStreamOption(0, null);

    public static final PackedComponentStreamOption EXCLUDE_ORIGIN_OPTION = new PackedComponentStreamOption(EXCLUDE_ORIGIN, null);

    public static final PackedComponentStreamOption INCLUDE_EXTENSION_OPTION = new PackedComponentStreamOption(INCLUDE_EXTENSIONS, null);

    public static final PackedComponentStreamOption IN_ORIGIN_CONTAINER_OPTION = new PackedComponentStreamOption(IN_SAME_CONTAINER, null);

    private final int s;

    @Nullable
    private final Set<Class<? extends Extension>> includeExtensions;

    PackedComponentStreamOption(int s, Set<Class<? extends Extension>> includeExtensions) {
        this.s = s;
        this.includeExtensions = includeExtensions;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        // sj.add("s = " + s);
        if (excludeOrigin()) {
            sj.add("excludeSelf");
        }
        if ((s & INCLUDE_EXTENSIONS) != 0) {
            sj.add("includeExtensions");
        } else if (includeExtensions != null) {
            sj.add("includeExtensions = " + includeExtensions.stream().map(e -> e.getSimpleName()).collect(Collectors.joining(", ", "{", "}")));
        }
        if ((s & IN_SAME_CONTAINER) != 0) {
            sj.add("inSameContainer");
        }
        return sj.toString();
    }

    public static PackedComponentStreamOption of(ComponentStream.Option... options) {
        requireNonNull(options, "options is null");
        if (options.length == 0) {
            return DEFAULT_OPTION;
        }
        PackedComponentStreamOption o0 = (PackedComponentStreamOption) options[0];
        if (options.length == 1) {
            return o0;
        }
        int s = o0.s;
        Set<Class<? extends Extension>> includeExtensions = o0.includeExtensions;
        for (int i = 1; i < options.length; i++) {
            PackedComponentStreamOption oo = (PackedComponentStreamOption) options[i];
            s |= oo.s;
            if (includeExtensions == null) {
                includeExtensions = oo.includeExtensions;
            } else {
                includeExtensions = new HashSet<>(includeExtensions);
                includeExtensions.addAll(oo.includeExtensions);
                includeExtensions = Set.copyOf(includeExtensions);
            }
        }
        return new PackedComponentStreamOption(s, includeExtensions);
    }

    boolean excludeOrigin() {
        return (s & EXCLUDE_ORIGIN) == 1;
    }

    public boolean processThisDeeper(ComponentNode origin, ComponentNode actual) {
        // Class<? extends Extension> extensionType = actual.model.extension().orElse(null);
        // if (s==0) return;
        // TODO just changed includeExtensions == null || !includeExtensions.contains(extensionType))) to &&, dobbel check
        // Also in next method
//        if (extensionType != null && ((s & INCLUDE_EXTENSIONS) != 0 || (includeExtensions == null || !includeExtensions.contains(extensionType)))) {
//            return false;
//        }
        if ((s & IN_SAME_CONTAINER) != 0 && !origin.isInSameContainer(actual)) {
            return false;
        }
        return true;
    }

    public boolean processThisDeeper(ComponentNodeConfiguration origin, ComponentNodeConfiguration actual) {
//        Class<? extends Extension> extensionType = actual.extension().orElse(null);
//        // if (s==0) return;
//        if (extensionType != null && ((s & INCLUDE_EXTENSIONS) != 0 || (includeExtensions == null || !includeExtensions.contains(extensionType)))) {
//            return false;
//        }
//        if ((s & IN_SAME_CONTAINER) != 0 && !origin.isInSameContainer(actual)) {
//            return false;
//        }
        return true;
    }
}
