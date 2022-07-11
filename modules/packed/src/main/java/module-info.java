module app.packed {
    exports app.packed.application;
    exports app.packed.application.entrypoint;
    exports app.packed.base;
    exports app.packed.bean;
    exports app.packed.container;
    exports app.packed.inject;
    exports app.packed.inject.service;
    exports app.packed.lifetime.mirror;
    exports app.packed.lifetime.sandbox;
    exports app.packed.lifetime;
    exports app.packed.operation;

    // temporary
    exports app.packed.operation.dependency;

    /* Special support for DevTools project */
    uses internal.app.packed.integrate.devtools.PackedDevToolsIntegration;

    exports internal.app.packed.integrate.devtools to app.packed.devtools;
}

// requires static org.graalvm.sdk;
