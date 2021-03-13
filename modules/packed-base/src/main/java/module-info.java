module app.packed.base {
    exports app.packed.attribute; // then have @Preview @DeprecatedWithReason
    exports app.packed.base; // then have @Preview @DeprecatedWithReason
    exports app.packed.cli;
    exports app.packed.component;
    exports app.packed.component.drivers;
    exports app.packed.container;
    exports app.packed.conversion;
    exports app.packed.inject;
    exports app.packed.inject.sandbox;
    exports app.packed.hooks;
    exports app.packed.state;
    exports app.packed.validate;
    
    opens app.packed.cli to foo;
    /*
     * exports packed.internal.classscan.invoke to app.packed.sidecar2; exports packed.internal.util to app.packed.sidecar2;
     */
    // Temporary...
//    exports packed.internal.invoke to app.packed.banana, app.packed.function, app.packed.conta;
//    exports packed.internal.invoke.typevariable to app.packed.banana, app.packed.function;
//    exports packed.internal.util to app.packed.configuration, app.packed.cli, app.packed.conta;
//
//    exports packed.internal.component to app.packed.errorhandling;
//    exports packed.internal.component.wirelet to app.packed.errorhandling;
//    exports packed.internal.container to app.packed.errorhandling, app.packed.conta;
//    exports packed.internal.hook to app.packed.errorhandling;
//    exports packed.internal.lifecycle.old to app.packed.errorhandling;
//    exports packed.internal.errorhandling to app.packed.errorhandling;
//    exports packed.internal.base.attribute to app.packed.attribute;
//    exports packed.internal.sidecar.old to app.packed.conta;
//    exports packed.internal.hook.applicator to app.packed.errorhandling, app.packed.cli;

    // opens app.packed.service to app.packed.service;
    
    uses packed.internal.util.Plugin;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
// provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;