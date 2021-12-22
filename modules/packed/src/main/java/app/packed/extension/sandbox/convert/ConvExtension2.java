package app.packed.extension.sandbox.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.bean.BeanSupport;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBean;
import app.packed.extension.ExtensionSupport;

// Maaske er det ikke ContainerExtensor. But InheritableExtensor...
// "Problemet" er applikation hosts... Vi gider jo ikke have 25 forskellige extensors.
// Med if and else...

// Altsaa Conv er et eksempel paa noget hvor vi er 100% ligeglad omkring containere.
// Men er 100% hiraki styret...
// Og saa alligevel ikke... Man skal ikke kunne bruge wirelets...
// Man skal ikke kunne overskrive en converter udefra... Det er for invasivt

// Vil man godt kunne sige. Det her module exportere en Car->FooBar converter
// Ved faktisk ikke hvor brugbart inheritance af convertere er...

// Hvis konvertere bruger services i en container...
// Saa er det jo ikke super let at exportere...
// Maaske man bliver noedt til at bruge en service... IDK
// Den kraever jo en staerk binding...

// Converters er jo faktisk ligesom hooks...
// Der kan vaere et callsite binding process...
public class ConvExtension2 extends Extension implements ConvDiscovable {

    static final Map<Class<?>, Function<?, ?>> DEFAULTS = Map.of();

    Map<Class<?>, Function<?, ?>> converters = new HashMap<>();

    ConvExtension2() {}

    public void add(Class<?> from, Function<?, ?> f) {
        checkIsPreLinkage();
        converters.put(from, f);
    }

    // Clear all converters
    public void clear() {}

    // findParent();
    // findAncestor();

    protected <T extends ConvDiscovable> Optional<T> findFirst(Class<T> dis) {
        throw new UnsupportedOperationException();
    }

    protected <E extends Extension> Optional<E> findExtensionAncestor(Class<E> extension) {
        throw new UnsupportedOperationException();
    }

    protected <B extends ExtensionBean, E extends Extension, T> T findAncestorOrElse(Class<B> beanType, Function<B, T> f1, Class<E> extensionType,
            Function<E, T> f2, @Nullable T defaultValue) {
        // Will first look after a bean of the particular type. Then extension, or finally return default value
        throw new UnsupportedOperationException();
    }

    // Hvordan inheriter vi en AutoBean fra en parent container
    // inherit(Class<? extends AutoBean>) <--- er udelukkende taenkt som plads besparende
    @Override
    protected void onPostSetUp() {
        Optional<ConvDiscovable> d = findFirst(ConvDiscovable.class);
        Map<Class<?>, Function<?, ?>> existing;
        if (d.isEmpty()) {
            existing = DEFAULTS;
        } else {
            ConvDiscovable cd = d.get();
            if (cd instanceof ConvExtension e) {
                existing = e.converters;
            } else {
                existing = ((ConvExtensor) cd).m;
            }
        }

        Map<Class<?>, Function<?, ?>> ff = findAncestorOrElse(ConvExtensor.class, ce -> ce.m, ConvExtension.class, e -> e.converters, DEFAULTS);
        System.out.println(ff);
        if (converters.isEmpty()) {
            converters = existing;
            use(BeanSupport.class).inheritOrInstall(ConvExtensor.class);
        } else {
            HashMap<Class<?>, Function<?, ?>> map = new HashMap<>(existing);
            map.putAll(converters);
            converters = Map.copyOf(map); // make immutable copy
            use(BeanSupport.class).install(ConvExtensor.class);
        }
    }

    public class Sub extends ExtensionSupport {

        public void add(Class<?> from, Function<?, ?> f) {
            ConvExtension2.this.add(from, f);
        }
    }
}
