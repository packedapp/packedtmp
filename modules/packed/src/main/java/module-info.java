module app.packed {
    requires transitive jdk.jfr;
//    requires jdk.incubator.concurrent;

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
    exports sandbox.lifetime;
    exports sandbox.extension.bean;
    exports sandbox.extension.container;
    exports sandbox.extension.context;
    exports sandbox.extension.operation;
    exports sandbox.extension.sandbox;

    /* Special support for packed-devtoolks */
    uses internal.app.packed.framework.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.framework.devtools to app.packed.devtools;
}
