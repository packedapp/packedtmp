package app.packed.extension.sandbox.convert;

import java.util.Map;
import java.util.function.Function;

import app.packed.extension.ExtensionBeanNEW;

final class ConvExtensor extends ExtensionBeanNEW implements ConvDiscovable {

    final Map<Class<?>, Function<?, ?>> m;

    ConvExtensor(ConvExtension e) {
        this.m = e.converters;
    }
}
