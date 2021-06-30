package app.packed.extension.sandbox.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionBeanDoubly;

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
public class ConvExtension extends Extension {

    static final Map<Class<?>, Function<?, ?>> DEFAULTS = Map.of();

    Map<Class<?>, Function<?, ?>> converters = new HashMap<>();

    ConvExtension() {}

    public void add(Class<?> from, Function<?, ?> f) {
        checkIsPreLinkage();
        converters.put(from, f);
    }

    // Clear all converters
    public void clear() {}

    @Override
    protected void onPreChildren() {
        ExtensionBeanDoubly<ConvExtension, ConvExtensor> ec = findFirst(ConvExtension.class, ConvExtensor.class);

        if (converters.isEmpty()) {
            converters = ec.extractOrElse(e -> e.converters, e -> e.m, DEFAULTS); // for child extensions
            if (ec.isPresent()) {
                return; // don't install a new extensor, no new converters, and some parent got us covered
            }
        } else {
            HashMap<Class<?>, Function<?, ?>> map = new HashMap<>(ec.extractOrElse(e -> e.converters, e -> e.m, DEFAULTS));
            map.putAll(converters);
            converters = Map.copyOf(map); // make immutable copy
        }
        // We only install an extensor if we are the root container or we have changes to the converters
        installExtensor(ConvExtensor.class); // instantiate a new ConvExtensor (may already have been auto installed)
    }

    public class Sub extends Subtension {

        public void add(Class<?> from, Function<?, ?> f) {
            ConvExtension.this.add(from, f);
        }
    }
}
