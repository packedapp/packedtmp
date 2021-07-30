package app.packed.build;

import app.packed.bean.hooks.BeanBuildHook;
import app.packed.container.AssemblyHook;

public sealed interface BuildHook permits AssemblyHook,BeanBuildHook {

}
