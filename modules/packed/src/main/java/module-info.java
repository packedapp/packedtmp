module app.packed {
    requires transitive jdk.jfr;

    exports app.packed.application;
    exports app.packed.bean;
    exports app.packed.context;
    exports app.packed.container;
    exports app.packed.errorhandling;
    exports app.packed.extension;
    exports app.packed.lifetime;
    exports app.packed.operation;
    exports app.packed.service;
    exports app.packed.util;

    // Essential extensions

    // temporary sandbox thingies
    exports app.packed.operation.mirror;
    exports app.packed.lifetime.sandbox;
    exports app.packed.extension.bean;
    exports app.packed.extension.container;
    exports app.packed.extension.context;
    exports app.packed.extension.operation;
    exports app.packed.extension.sandbox;

    /* Special support for packed-devtoolks */
    uses internal.app.packed.framework.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.framework.devtools to app.packed.devtools;
}
