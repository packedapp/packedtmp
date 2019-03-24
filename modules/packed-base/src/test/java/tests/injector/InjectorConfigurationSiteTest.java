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
package tests.injector;

import static org.assertj.core.api.Assertions.assertThat;
import static support.stubs.Letters.C0;

import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodHandles;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.TypeLiteral;
import packed.internal.config.site.ConfigurationSiteType;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.D;
import support.stubs.Letters.E;
import support.stubs.Letters.F;
import support.stubs.Letters.G;
import support.stubs.Letters.H;
import support.stubs.Letters.I;
import support.stubs.Letters.J;

/**
 * Tests {@link ServiceConfiguration#configurationSite()} and {@link Injector#configurationSite()}.
 * <p>
 * Most of the test are pretty hackish.
 */
@Disabled
public class InjectorConfigurationSiteTest {

    /** A helper stack frame */
    StackFrame sfCreate;

    /** Another helper stack frame */
    StackFrame injectorCreate;

    /**
     * We keep track of configuration sites when binding to make sure they are identical after the injector has been
     * created.
     */
    private final IdentityHashMap<Class<?>, ConfigSite> sites = new IdentityHashMap<>();

    /** Tests that the various bind operations gets the right configuration site. */
    @Test
    public void binding() {
        Injector inj = Injector.of(conf -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            injectorCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(2).findFirst()).get();
            conf.lookup(MethodHandles.lookup());// The letter classes are not exported
            binding0(conf.provide(A.class));
            binding0(conf.provide(Factory.findInjectable(B.class)));
            binding0(conf.provide(C0));
            binding0(conf.provide(TypeLiteral.of(D.class)));
            binding0(conf.provideLazy(E.class));
            binding0(conf.provideLazy(Factory.findInjectable(F.class)));
            binding0(conf.provideLazy(TypeLiteral.of(G.class)));
            binding0(conf.providePrototype(H.class));
            binding0(conf.providePrototype(Factory.findInjectable(I.class)));
            binding0(conf.providePrototype(TypeLiteral.of(J.class)));
        });
        for (Entry<Class<?>, ConfigSite> e : sites.entrySet()) {
            ConfigSite cs = inj.getDescriptor(e.getKey()).get().configurationSite();
            assertThat(cs).isSameAs(e.getValue());
            assertThat(cs.parent().get()).isSameAs(inj.configurationSite());
        }
    }

    /** A helper method for {@link #binding()}. */
    private void binding0(ServiceConfiguration<?> sc) {
        // A hack where we use the binding key of the service, to figure out the line number.
        int index = sc.getKey().typeLiteral().getRawType().getSimpleName().toString().charAt(0) - 'A';
        ConfigSite cs = sc.configurationSite();
        int line = sfCreate.getLineNumber();
        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + index + 3)));
        assertThat(cs.operation()).isEqualTo(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND.operation());
        assertThat(cs.hasParent()).isTrue();
        assertThat(cs.parent().get().toString()).isEqualTo(injectorCreate.toString());
        sites.put(sc.getKey().typeLiteral().getRawType(), cs);
    }

    /**
     * Tests that imported service retain configuration sites when using
     * {@link InjectorConfigurator#importServicesFrom(Injector)}.
     */
    @Test
    public void importServiceFrom() {
        Injector i = Injector.of(c -> {
            c.provide(123);
        });

        Injector i2 = Injector.of(c -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            c.wireInjector(i);
        });

        ConfigSite cs = i2.getDescriptor(Integer.class).get().configurationSite();
        // First site is "c.importServicesFrom(i);"
        int line = sfCreate.getLineNumber();
        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // Parent site is "c.bind(123);"
        assertThat(cs.parent().get()).isSameAs(i.getDescriptor(Integer.class).get().configurationSite());

        // Lets make another injector and import the service yet again
        Injector i3 = Injector.of(c -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            c.wireInjector(i2);
        });

        cs = i3.getDescriptor(Integer.class).get().configurationSite();
        // First site is "c.importServicesFrom(i);"
        line = sfCreate.getLineNumber();
        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // Parent site is "c.bind(123);"
        assertThat(cs.parent().get()).isSameAs(i2.getDescriptor(Integer.class).get().configurationSite());
    }

    @Test
    public void importServiceFromStaging() {

        // Den her test

        // Injector i = Injector.of(c -> {
        // c.bind(123);
        // });

        // // Tests importService(Class)
        // {
        // Injector i2 = Injector.of(c -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // c.importServicesFrom(i, t -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // ServiceConfiguration<Integer> sc = t.importService(Integer.class);
        // assertThat(sc.getConfigurationSite().parent().get()).isSameAs(i.getService(Integer.class).getConfigurationSite());
        // // Test that other method does not override configuration site
        // t.importService(Integer.class);
        // t.importAllServices();
        // t.importAllServices(e -> true);
        // t.importService(Key.of(Integer.class));
        // });
        // });
        // ConfigurationSite cs = i2.getService(Integer.class).getConfigurationSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).getConfigurationSite());
        // }
        // // Tests importService(Key)
        // {
        // Injector i2 = Injector.of(c -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // c.importServicesFrom(i, t -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // t.importService(Key.of(Integer.class));
        // // Test that other method does not override configuration site
        // t.importService(Integer.class);
        // t.importAllServices();
        // t.importAllServices(e -> true);
        // t.importService(Key.of(Integer.class));
        // });
        // });
        // ConfigurationSite cs = i2.getService(Integer.class).getConfigurationSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).getConfigurationSite());
        // }
        // // Tests importAllServices(Predicate)
        // {
        // Injector i2 = Injector.of(c -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // c.importServicesFrom(i, t -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // t.importAllServices(e -> true);
        // // Test that other method does not override configuration site
        // t.importService(Integer.class);
        // t.importAllServices();
        // t.importAllServices(e -> true);
        // t.importService(Key.of(Integer.class));
        // });
        // });
        // ConfigurationSite cs = i2.getService(Integer.class).getConfigurationSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).getConfigurationSite());
        // }
        // // Tests importAllServices()
        // {
        // Injector i2 = Injector.of(c -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // c.importServicesFrom(i, t -> {
        // sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
        // t.importAllServices();
        // // Test that other method does not override configuration site
        // t.importService(Integer.class);
        // t.importAllServices();
        // t.importAllServices(e -> true);
        // t.importService(Key.of(Integer.class));
        // });
        // });
        // ConfigurationSite cs = i2.getService(Integer.class).getConfigurationSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).getConfigurationSite());
        // }
    }

    // Also test @Provides....
    public void testProvides() {
        // Register stuff with Provides....
        // Creating some more build nodes
    }
    // And InjectorBundle
}
