module app.packed {
    exports app.packed.application;
    exports app.packed.application.entrypoint;
    exports app.packed.base;
    exports app.packed.bean;
    exports app.packed.container;
    exports app.packed.inject;
    exports app.packed.inject.service;
    exports app.packed.lifecycle;
    exports app.packed.lifetime;
    exports app.packed.operation;

    // temporary
    exports app.packed.operation.dependency;
    exports app.packed.operation.mirror;

    /* Special support for DevTools project */
    uses packed.internal.integrate.devtools.PackedDevToolsIntegration;

    exports packed.internal.integrate.devtools to app.packed.devtools;
}

// requires static org.graalvm.sdk;
