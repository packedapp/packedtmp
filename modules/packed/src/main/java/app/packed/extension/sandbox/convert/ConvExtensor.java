package app.packed.extension.sandbox.convert;

import java.util.Map;
import java.util.function.Function;

import app.packed.extension.ExtensionBean;
import app.packed.extension.ExtensionMember;

@ExtensionMember(ConvExtension.class)
final class ConvExtensor extends ExtensionBean implements ConvDiscovable {

    final Map<Class<?>, Function<?, ?>> m;

    ConvExtensor(ConvExtension e) {
        this.m = e.converters;
    }
}
