module app.packed {
    exports app.packed.application;
    exports app.packed.base;
    exports app.packed.bean;
    exports app.packed.container;
    exports app.packed.operation;
    exports app.packed.lifetime;

    // Essential extensions
    exports app.packed.entrypoint;
    exports app.packed.service;

    // temporary sandbox thingies
    exports app.packed.lifetime.managed;
    exports app.packed.lifetime.sandbox;

    /* Special support for packed-devtoolks */
    uses internal.app.packed.base.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.base.devtools to app.packed.devtools;
}

// requires static org.graalvm.sdk;
