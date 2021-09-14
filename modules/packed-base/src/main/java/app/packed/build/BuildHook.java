package app.packed.build;

import app.packed.bean.hooks.BeanBuildHook;
import app.packed.bundle.sandbox.AssemblyBuildHook;

public sealed interface BuildHook permits AssemblyBuildHook,BeanBuildHook {

}
