module app.packed {
    requires transitive jdk.jfr;

    exports app.packed.application;
    exports app.packed.bean;
    exports app.packed.context;
    exports app.packed.container;
    exports app.packed.errorhandling;
    exports app.packed.util;
    exports app.packed.operation;
    exports app.packed.lifetime;
    exports app.packed.service;

    // Taenker de her ryger paa et tidspunkt
    exports app.packed.bindings;
    exports app.packed.extension;

    // Essential extensions
    exports app.packed.entrypoint;

    // temporary sandbox thingies
    exports app.packed.bindings.mirror;
    exports app.packed.lifetime.sandbox;

    /* Special support for packed-devtoolks */
    uses internal.app.packed.framework.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.framework.devtools to app.packed.devtools;
}
