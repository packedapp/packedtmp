// is open because of Eclipse Junit
module app.packed {
    requires transitive jdk.jfr;
    //  requires jdk.incubator.concurrent; // Sometimes test

    exports app.packed.application;
    exports app.packed.assembly;
    exports app.packed.bean;
    exports app.packed.binding;
    exports app.packed.build;
    exports app.packed.build.hook;
    exports app.packed.component;
    exports app.packed.container;
    exports app.packed.context;
    exports app.packed.extension;
    exports app.packed.lifetime;
    exports app.packed.namespace;
    exports app.packed.operation;
    exports app.packed.runtime;
    exports app.packed.service;
    exports app.packed.service.mirror;
    exports app.packed.util;

    // temporary sandbox thingies
    exports sandbox.extension.container;
    exports internal.app.packed.context.publish;
    exports sandbox.extension.operation;

    /* Special support for packed-devtools */
    uses internal.app.packed.integration.devtools.PackedDevToolsIntegration;
    exports internal.app.packed.integration.devtools to app.packed.devtools;
}
