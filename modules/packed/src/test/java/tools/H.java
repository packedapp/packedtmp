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
package tools;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Assertions;

import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.FromLifetimeChannel;
import app.packed.extension.OperationHandle;
import app.packed.extension.BeanElement.BeanField;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.AnnotationList;

/**
 *
 */
public class H {

    /** An driver for creating App instances. */
    private static final BootstrapApp<H> DRIVER = BootstrapApp.of(H.class, c -> {
        c.managedLifetime();
        c.addChannel(BaseExtensionPoint.EXPORTED_SERVICE_LOCATOR);
    });

    final ServiceLocator sl;

    H(@FromLifetimeChannel ServiceLocator sl) {
        this.sl = sl;
    }

    public <T> T use(Class<T> t) {
        return sl.use(t);
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(int name, Object... args) throws Throwable {
        ArrayList<Object> l = new ArrayList<>();
        l.add(requireNonNull(HExtension.EC));
        l.addAll(List.of(args));
        return (T) mh(name).invokeWithArguments(l);
    }

    public <T> T invoke(Object... args) throws Throwable {
        return invoke(1, args);
    }

    public MethodHandle mh(int name) {
        return HExtension.M.get(name);
    }

    public static ApplicationMirror mirrorOf(ComposerAction<? super Composer> action, Wirelet... wirelets) {
        return DRIVER.mirrorOf(new ServiceLocatorAssembly(action), wirelets);

    }

    public static <T extends Throwable> T assertThrows(Class<? extends T> throwing, ComposerAction<? super Composer> action) {
        return Assertions.assertThrows(throwing, () -> of(action));
    }

    public static H of(ComposerAction<? super Composer> action) {
        return of(action, new Wirelet[0]);
    }

    static H of(ComposerAction<? super Composer> action, Wirelet... wirelets) {
        return DRIVER.launch(new ServiceLocatorAssembly(action), wirelets);
    }

    public static class Composer extends AbstractComposer {

        public <T> void generate(OperationHandle h) {
            h().generate(h);
        }

        HExtension h() {
            return use(HExtension.class);
        }

        public void onAnnotatedFieldHook(BiConsumer<? super AnnotationList, ? super BeanField> consumer) {
            h().addFieldCallback(consumer);
        }

        @Override
        protected void preCompose() {
            use(HExtension.class);
        }

        /**
         * @param class1
         */
        public ServiceableBeanConfiguration<?> provide(Class<?> cl) {
            return use(BaseExtension.class).install(cl).provide();
        }

        public ServiceableBeanConfiguration<?> install(Class<?> cl) {
            return use(BaseExtension.class).install(cl);
        }

        /**
         * @param dd
         */
        public void provideInstance(Object dd) {
            use(BaseExtension.class).installInstance(dd).provide();
        }
    }

    static class ServiceLocatorAssembly extends ComposerAssembly<Composer> {

        ServiceLocatorAssembly(ComposerAction<? super Composer> action) {
            super(new Composer(), action);
        }
    }
}
