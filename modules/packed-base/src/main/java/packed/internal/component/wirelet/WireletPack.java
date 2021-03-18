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
package packed.internal.component.wirelet;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedArtifactDriver;
import packed.internal.component.PackedComponentDriver;

/** A holder of wirelets and wirelet pipelines. */
public final class WireletPack {

    private static final Empty<?> EMPTY = new Empty<>();

    private final ArrayList<Ent> list = new ArrayList<>();

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    String name; // kan komme i map... og saa saetter vi et flag istedet for...

    /** Creates a new pack. */
    private WireletPack() {}

    /**
     * @param w
     */
    private void create0(Wirelet w) {
        if (w instanceof InternalWirelet bw) {
            bw.process(this);
        } else if (w instanceof WireletList wl) {
            for (Wirelet ww : wl.wirelets) {
                create0(ww);
            }
        } else {
            list.add(new Ent(w));
        }
    }

    public <T extends Wirelet> WireletHandle<T> handleOf(Module module, Class<? extends T> wireletClass) {
        if (module != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet must be in module " + module + ", was " + module.getName());
        }
        return new HandleImpl<>(wireletClass);
    }

    // That name wirelet.. should only be used by the top-container....
    @Nullable
    public String nameWirelet() {
        return name;
    }

    /**
     * Creates a new wirelet pack or returns existing if the array of wirelets is empty.
     * 
     * @param wirelets
     *            the wirelets
     * @return stuff
     */
    @Nullable
    private static WireletPack create(WireletPack parent, Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return null;
        }

        WireletPack wc = new WireletPack();
        for (Wirelet w : wirelets) {
            requireNonNull(w, "wirelets contained a null");
            wc.create0(w);
        }
        return wc;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Wirelet> WireletHandle<T> empty() {
        return (WireletHandle<T>) EMPTY;
    }

    public static <T extends Wirelet> WireletHandle<T> extensionHandle(WireletPack containerWirelets, Class<? extends Extension> extensionClass,
            Class<? extends T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");
        if (extensionClass.getModule() != wireletClass.getModule()) {
            throw new InternalExtensionException("oops mismatching module");
        }
        if (containerWirelets == null) {
            return empty();
        } else {
            return containerWirelets.handleOf(extensionClass.getModule(), wireletClass);
        }
    }

    public static <T extends Wirelet> WireletHandle<T> handleOf(Class<? extends T> wireletClass, Wirelet... wirelets) {
        if (wirelets.length < 1) {
            throw new IllegalArgumentException("Must specify at least 1 wirelet.");
        }
        return create(null, wirelets).handleOf(wireletClass.getModule(), wireletClass);
    }

    @Nullable
    public static WireletPack ofChild(@Nullable WireletPack parent, PackedComponentDriver<?> driver, Wirelet... wirelets) {
        if (driver.modifiers().isContainer()) {
            return create(parent, wirelets);
        }
        return null;
    }

    @Nullable
    public static WireletPack ofImage(ComponentSetup component, Wirelet... wirelets) {
        return create(null, wirelets);
    }

    @Nullable
    public static WireletPack ofRoot(PackedArtifactDriver<?> pac, PackedComponentDriver<?> pcd, Wirelet... wirelets) {
        Wirelet w = Wirelet.of(wirelets);
        if (pac.wirelet != null) {
            w = pac.wirelet.andThen(w);
        }
        return create(null, w);
    }

    public static final class Empty<T extends Wirelet> implements WireletHandle<T> {

        @Override
        public void forEach(Consumer<? super T> action) {}

        @Override
        public boolean isAbsent() {
            return true;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public Optional<T> last() {
            return Optional.empty();
        }
    }

    public static class Ent {
        public boolean isReceived;

        public final Wirelet wirelet;

        Ent(Wirelet wirelet) {
            this.wirelet = requireNonNull(wirelet);
        }
    }

    public final class HandleImpl<T extends Wirelet> implements WireletHandle<T> {

        private final Class<? extends T> wireletClass;

        HandleImpl(Class<? extends T> wireletClass) {
            // We should check all public wirelet types here
            if (Wirelet.class == wireletClass) {
                throw new IllegalArgumentException("Cannot specify " + Wirelet.class.getSimpleName() + ".class");
            }

            this.wireletClass = requireNonNull(wireletClass, "wireletClass is null");
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public void forEach(Consumer<? super T> action) {
            requireNonNull(action, "action is null");
            for (Ent e : list) {
                if (!e.isReceived && wireletClass.isInstance(e.wirelet)) {
                    action.accept((T) e.wirelet);
                    e.isReceived = true;
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isAbsent() {
            boolean result = true;
            for (Ent e : list) {
                if (!e.isReceived && wireletClass.isInstance(e.wirelet)) {
                    result = false;
                    e.isReceived = true;
                }
            }
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPresent() {
            return !isAbsent();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public Optional<T> last() {
            T result = null;
            for (Ent e : list) {
                if (!e.isReceived && wireletClass.isInstance(e.wirelet)) {
                    if (result == null) {
                        result = (T) e.wirelet;
                    }
                    e.isReceived = true;
                }
            }
            return Optional.ofNullable(result);
        }
    }
}
