package app.packed.extension.sandbox.convert;

import java.util.Map;
import java.util.function.Function;

import app.packed.extension.ContainerExtensor;

final class ConvExtensor extends ContainerExtensor<ConvExtension> {

    final Map<Class<?>, Function<?, ?>> m;

    ConvExtensor(ConvExtension e) {
        this.m = e.converters;
    }
}
