module app.packed {
    requires transitive jdk.jfr;
    
    exports app.packed.application;
    exports app.packed.framework;
    exports app.packed.bean;
    exports app.packed.container;
    exports app.packed.operation;
    exports app.packed.lifetime;

    // Essential extensions
    exports app.packed.entrypoint;
    exports app.packed.service;

    // temporary sandbox thingies
    exports app.packed.operation.bindings;
    exports app.packed.operation.bindings.sandbox;
    exports app.packed.operation.context;

    /* Special support for packed-devtoolks */
    uses internal.app.packed.framework.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.framework.devtools to app.packed.devtools;
}

// requires static org.graalvm.sdk;
