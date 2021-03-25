module app.packed.base {
    exports app.packed.application;
    exports app.packed.attribute;
    exports app.packed.base;
    exports app.packed.cli;
    exports app.packed.component;
    exports app.packed.contract;
    exports app.packed.container;
    exports app.packed.conversion;
    exports app.packed.inject;
    exports app.packed.inject.sandbox;
    exports app.packed.hooks;
    exports app.packed.state;
    exports app.packed.validate;
    
    opens app.packed.cli to foo;
    uses packed.internal.util.Plugin;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
