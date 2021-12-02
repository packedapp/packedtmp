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

import static testutil.stubs.Letters.C0;

import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodHandles;
import java.util.IdentityHashMap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.base.TypeToken;
import app.packed.inject.ReflectionFactory;
import app.packed.inject.service.ServiceBeanConfiguration;
import packed.internal.config.ConfigSite;
import packed.internal.inject.service.sandbox.Injector;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.Letters.D;

/**
 * Tests config site.
 * <p>
 * Most of the test are pretty hackish.
 */
@Disabled
public class InjectorConfigSiteTest {

    /** A helper stack frame */
    StackFrame sfCreate;

    /** Another helper stack frame */
    StackFrame injectorCreate;

    /**
     * We keep track of configuration sites when binding to make sure they are identical after the injector has been
     * created.
     */
    final IdentityHashMap<Class<?>, ConfigSite> sites = new IdentityHashMap<>();

    /** Tests that the various bind operations gets the right configuration site. */
    @Test
    public void binding() {
        Injector inj = Injector.configure(conf -> {
            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
            injectorCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(2).findFirst()).get();
            conf.lookup(MethodHandles.lookup());// The letter classes are not exported
            binding0(conf.provide(A.class));
            binding0(conf.provide(ReflectionFactory.of(B.class)));
            binding0(conf.provideInstance(C0));
            binding0(conf.provideInstance(TypeToken.of(D.class)));
            // binding0(conf.provide(E.class).lazy());
            // binding0(conf.provide(Factory.findInjectable(F.class)).lazy());
            // binding0(conf.provideInstance(TypeLiteral.of(G.class)).lazy());
//            binding0(conf.provide(H.class).prototype());
//            binding0(conf.provide(Factory.find(I.class)).prototype());
//            binding0(conf.provideConstant(TypeLiteral.of(J.class)).prototype());
        });
//        for (Entry<Class<?>, ConfigSite> e : sites.entrySet()) {
//            ConfigSite cs = inj.find(e.getKey()).get().attribute(ConfigSite.ATTRIBUTE);
//            assertThat(cs).isSameAs(e.getValue());
//            // assertThat(cs.parent().get()).isSameAs(inj.configSite());
//        }
        System.out.println(inj);
    }

    /** A helper method for {@link #binding()}. */
    private void binding0(ServiceBeanConfiguration<?> sc) {
        // A hack where we use the binding key of the service, to figure out the line number.
        // int index = sc.key().get().rawType().getSimpleName().toString().charAt(0) - 'A';
        // ConfigSite cs = sc.configSite();
        // int line = sfCreate.getLineNumber();
//        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + index + 3)));
//        assertThat(cs.operation()).isEqualTo(ConfigSiteInjectOperations.COMPONENT_INSTALL);
//        assertThat(cs.hasParent()).isTrue();
//        assertThat(cs.parent().get().toString()).isEqualTo(injectorCreate.toString());
//        sites.put(sc.key().get().rawType(), cs);
    }

//    /**
//     * Tests that imported service retain configuration sites when using
//     * 
//     */
//    @Test
//    public void importServiceFrom() {
//        Injector i = Injector.configure(c -> {
//            c.provideInstance(123);
//        });
//
//        Injector i2 = Injector.configure(c -> {
//            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
//            c.provideAll(i);
//        });
//
//        ConfigSite cs = i2.find(Integer.class).get().attribute(ConfigSite.ATTRIBUTE);
//        // First site is "c.importServicesFrom(i);"
//        int line = sfCreate.getLineNumber();
//        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
//        // Parent site is "c.bind(123);"
//        assertThat(cs.parent().get()).isSameAs(i.find(Integer.class).get().attribute(ConfigSite.ATTRIBUTE));
//
//        // Lets make another injector and import the service yet again
//        Injector i3 = Injector.configure(c -> {
//            sfCreate = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.findFirst()).get();
//            c.provideAll(i2);
//        });
//
//        cs = i3.find(Integer.class).get().attribute(ConfigSite.ATTRIBUTE);
//        // First site is "c.importServicesFrom(i);"
//        line = sfCreate.getLineNumber();
//        assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
//        // Parent site is "c.bind(123);"
//        assertThat(cs.parent().get()).isSameAs(i2.find(Integer.class).get().attribute(ConfigSite.ATTRIBUTE));
//    }

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
        // assertThat(sc.getconfigSite().parent().get()).isSameAs(i.getService(Integer.class).configSite());
        // // Test that other method does not override configuration site
        // t.importService(Integer.class);
        // t.importAllServices();
        // t.importAllServices(e -> true);
        // t.importService(Key.of(Integer.class));
        // });
        // });
        // configSite cs = i2.getService(Integer.class).configSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).getconfigSite());
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
        // configSite cs = i2.getService(Integer.class).configSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).configSite());
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
        // configSite cs = i2.getService(Integer.class).configSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).configSite());
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
        // configSite cs = i2.getService(Integer.class).configSite();
        // // First site is "c.importServicesFrom(i);"
        // int line = sfCreate.getLineNumber();
        // assertThat(cs).hasToString(sfCreate.toString().replace(":" + line, ":" + (line + 1)));
        // // Parent site is "c.bind(123);"
        // assertThat(cs.parent().get()).isSameAs(i.getService(Integer.class).configSite());
        // }
    }

    // Also test @Provides....
    public void testProvides() {
        // Register stuff with Provides....
        // Creating some more build nodes
    }
}
