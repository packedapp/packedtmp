module app.packed {
    requires transitive jdk.jfr;
    //  requires jdk.incubator.concurrent; // Sometimes test

    exports app.packed.application;
    exports app.packed.bean;
    exports app.packed.cli;
    exports app.packed.container;
    exports app.packed.context;
    exports app.packed.errorhandling;
    exports app.packed.extension;
    exports app.packed.lifetime;
    exports app.packed.operation;
    exports app.packed.service;
    exports app.packed.util;

    // temporary sandbox thingies
    exports sandbox.extension.bean;
    exports sandbox.extension.container;
    exports internal.app.packed.context.publish;
    exports sandbox.extension.operation;
    exports sandbox.extension.sandbox;

    /* Special support for packed-devtools */
    uses internal.app.packed.integration.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.integration.devtools to app.packed.devtools;
}
