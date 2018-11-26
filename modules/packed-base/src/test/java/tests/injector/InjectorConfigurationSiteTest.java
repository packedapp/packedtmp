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

import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.TypeLiteral;
import app.packed.util.ConfigurationSite;
import packed.internal.util.configurationsite.ConfigurationSiteType;
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
 * Tests {@link ServiceConfiguration#getConfigurationSite()} and {@link Injector#getConfigurationSite()}.
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
    private final IdentityHashMap<Class<?>, ConfigurationSite> sites = new IdentityHashMap<>();

    /** Tests that the various bind operations gets the right configuration site. */
    @Test
    public void binding() {
        Injector inj = Injector.of(conf -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            injectorCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(2).findFirst()).get();
            conf.lookup(MethodHandles.lookup());// The letter classes are not exported
            binding0(conf.bind(A.class));
            binding0(conf.bind(Factory.findInjectable(B.class)));
            binding0(conf.bind(C0));
            binding0(conf.bind(TypeLiteral.of(D.class)));
            binding0(conf.bindLazy(E.class));
            binding0(conf.bindLazy(Factory.findInjectable(F.class)));
            binding0(conf.bindLazy(TypeLiteral.of(G.class)));
            binding0(conf.bindPrototype(H.class));
            binding0(conf.bindPrototype(Factory.findInjectable(I.class)));
            binding0(conf.bindPrototype(TypeLiteral.of(J.class)));
        });
        for (Entry<Class<?>, ConfigurationSite> e : sites.entrySet()) {
            ConfigurationSite cs = inj.getService(e.getKey()).getConfigurationSite();
            assertThat(cs).isSameAs(e.getValue());
            assertThat(cs.parent().get()).isSameAs(inj.getConfigurationSite());
        }
    }

    /** A helper method for {@link #binding()}. */
    private void binding0(ServiceConfiguration<?> sc) {
        // A hack where we use the binding key of the service, to figure out the line number.
        int index = sc.getKey().getTypeLiteral().getRawType().getSimpleName().toString().charAt(0) - 'A';
        ConfigurationSite cs = sc.getConfigurationSite();
        int line = sfCreate.getLineNumber();
        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + index + 3)));
        assertThat(cs.operation()).isEqualTo(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND.operation());
        assertThat(cs.hasParent()).isTrue();
        assertThat(cs.parent().get().toString()).isEqualTo(injectorCreate.toString());
        sites.put(sc.getKey().getTypeLiteral().getRawType(), cs);
    }

    /**
     * Tests that imported service retain configuration sites when using
     * {@link InjectorConfiguration#importServicesFrom(Injector)}.
     */
    @Test
    public void importServiceFrom() {
        Injector i = Injector.of(c -> {
            c.bind(123);
        });

        Injector i2 = Injector.of(c -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            c.injectorBind(i);
        });

        ConfigurationSite cs = i2.getService(Integer.class).getConfigurationSite();
        // First site is "c.importServicesFrom(i);"
        int line = sfCreate.getLineNumber();
        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // Parent site is "c.bind(123);"
        assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).getConfigurationSite());

        // Lets make another injector and import the service yet again
        Injector i3 = Injector.of(c -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            c.injectorBind(i2);
        });

        cs = i3.getService(Integer.class).getConfigurationSite();
        // First site is "c.importServicesFrom(i);"
        line = sfCreate.getLineNumber();
        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // Parent site is "c.bind(123);"
        assertThat(cs.parent().get()).isSameAs(i2.getService(Integer.class).getConfigurationSite());
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
