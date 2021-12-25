module app.packed {
    exports app.packed.application;
    exports app.packed.application.entrypoint;
    exports app.packed.application.various;
    exports app.packed.base;
    exports app.packed.bean;
    exports app.packed.bean.mirror;
    exports app.packed.bean.operation;
    exports app.packed.build;
    exports app.packed.component;
    exports app.packed.contract;
    exports app.packed.container;
    exports app.packed.conversion;
    exports app.packed.extension;
    exports app.packed.inject;
    exports app.packed.inject.mirror;
    exports app.packed.inject.sandbox;
    exports app.packed.inject.service;
    exports app.packed.inject.variable;
    exports app.packed.lifecycle;
    exports app.packed.lifetime;
    exports app.packed.hooks;
    exports app.packed.mirror;
    exports app.packed.state;
    exports app.packed.state.sandbox;
    exports app.packed.validate;
    exports app.packed.request;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
