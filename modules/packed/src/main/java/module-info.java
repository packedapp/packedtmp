module app.packed {
    exports app.packed.application;
    exports app.packed.application.entrypoint;
    exports app.packed.application.sandbox;
    exports app.packed.base;
    exports app.packed.bean;
    exports app.packed.operation;
    exports app.packed.operation.interceptor;
    exports app.packed.operation.mirror;
    exports app.packed.operation.lifecycle;
    exports app.packed.component;
    exports app.packed.contract;
    exports app.packed.container;
    exports app.packed.conversion;
    exports app.packed.extension;
    exports app.packed.inject;
    exports app.packed.operation.mirror.ig;
    exports app.packed.inject.sandbox;
    exports app.packed.inject.service;
    exports app.packed.lifecycle;
    exports app.packed.lifetime;
    exports app.packed.bean.hooks;
    exports app.packed.mirror;
    exports app.packed.state;
    exports app.packed.state.sandbox;
    exports app.packed.validate;
    
    uses packed.internal.devtools.spi.PackedDevTools;
    exports packed.internal.devtools.spi to app.packed.devtools;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
