package app.packed.build;

import app.packed.bean.hooks.sandbox.BeanBuildHook;
import app.packed.bundle.sandbox.BundleHook;

// @BuildAdaptor.Hook(....)
public sealed interface BuildHook permits BundleHook,BeanBuildHook {}
