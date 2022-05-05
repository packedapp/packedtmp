module app.packed {
    exports app.packed.application;
    exports app.packed.base;
    exports app.packed.bean;
    exports app.packed.component;
    exports app.packed.container;
    exports app.packed.inject;
    exports app.packed.lifecycle;
    exports app.packed.lifetime;
    exports app.packed.operation;

    exports app.packed.conversion;
    exports app.packed.mirror;
    exports app.packed.validate;
    
    
    
    exports app.packed.application.entrypoint;
    exports app.packed.operation.interceptor;
    exports app.packed.operation.mirror;
    exports app.packed.operation.lifecycle;

    exports app.packed.inject.sandbox;
    exports app.packed.inject.service;
    exports app.packed.inject.serviceexpose;
    exports app.packed.bean.hooks;
    
    /*  Special support for DevTools project */
    uses packed.internal.integrate.devtools.PackedDevToolsIntegration;
    exports packed.internal.integrate.devtools to app.packed.devtools;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
