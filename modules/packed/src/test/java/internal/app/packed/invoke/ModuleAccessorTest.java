package internal.app.packed.invoke;

import static java.lang.classfile.ClassFile.ACC_MODULE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.classfile.ClassFile;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import app.packed.extension.Extension;

class ModuleAccessorTest {

    // ... [Previous Tests: testHappyPath, testOptimization, etc. remain unchanged] ...

    @Test
    void testHappyPath_ForeignModuleOpenPackage(@TempDir Path tempDir) throws Exception {
        Class<?> targetClass = generateModuleAndLoadClass(tempDir, "com.example.open", "com.example.pkg", true);
        Lookup lookup = ModuleAccessor.lookupFor(targetClass);
        assertNotNull(lookup);
        assertTrue((lookup.lookupModes() & Lookup.MODULE) != 0);
    }

    @Test
    void testOptimization_SameModule() {
        Class<?> targetClass = this.getClass();
        Lookup lookup = ModuleAccessor.lookupFor(targetClass);
        assertNotNull(lookup);
    }

    @Test
    void testCaching_SameModuleInstance(@TempDir Path tempDir) throws Exception {
        Class<?> targetClass = generateModuleAndLoadClass(tempDir, "com.example.cached", "com.example.pkg", true);
        assertDoesNotThrow(() -> ModuleAccessor.lookupFor(targetClass));
        assertDoesNotThrow(() -> ModuleAccessor.lookupFor(targetClass));
    }

    @Test
    void testSecurity_ForeignModuleNotOpen(@TempDir Path tempDir) throws Exception {
        Class<?> targetClass = generateModuleAndLoadClass(tempDir, "com.example.closed", "com.example.pkg", false);
        assertThrows(IllegalAccessError.class, () -> ModuleAccessor.lookupFor(targetClass));
    }

    @Test
    void testSecurity_JavaBase() {
        assertThrows(IllegalAccessError.class, () -> ModuleAccessor.lookupFor(String.class));
    }

    // --- Fixed Helper Method ---

    private Class<?> generateModuleAndLoadClass(Path root, String moduleName, String pkgName, boolean openToFramework) throws Exception {
        Module frameworkModule = Extension.class.getModule();
        String frameworkModuleName = frameworkModule.getName();

        String className = pkgName + ".TargetBean";

        // 1. Generate module-info
        byte[] moduleInfoBytes = ClassFile.of().build(ClassDesc.of("module-info"), cb -> {
            cb.withFlags(ACC_MODULE);
            cb.with(ModuleAttribute.of(ModuleDesc.of(moduleName), mb -> {
                // Requires: (ModuleDesc, int flags, String version)
                // Flags: 0 (no special access like transitive/static)
                mb.requires(ModuleDesc.of("java.base"), 0, null);
                mb.requires(ModuleDesc.of(frameworkModuleName), 0, null);

                if (openToFramework) {
                    // Opens: (PackageDesc, int flags, ModuleDesc... targets)
                    // Target: Specific module
                    mb.opens(PackageDesc.of(pkgName), 0, ModuleDesc.of(frameworkModuleName));
                } else {
                    // Exports: (PackageDesc, int flags, ModuleDesc... targets)
                    // Target: No varargs argument means "Everyone" (Unqualified)
                    mb.exports(PackageDesc.of(pkgName), 0);
                }
            }));
        });

        Files.write(root.resolve("module-info.class"), moduleInfoBytes);

        // 2. Generate Target Class
        byte[] classBytes = ClassFile.of().build(ClassDesc.of(className), cb -> {
            cb.withFlags(ACC_PUBLIC);
        });

        Path classPath = root.resolve(className.replace('.', '/') + ".class");
        Files.createDirectories(classPath.getParent());
        Files.write(classPath, classBytes);

        // 3. Load Module
        ModuleFinder finder = ModuleFinder.of(root);
        Configuration parentConfig = frameworkModule.getLayer().configuration();

        Configuration config = parentConfig.resolve(
                finder,
                ModuleFinder.of(),
                Set.of(moduleName));

        ModuleLayer layer = ModuleLayer.defineModulesWithOneLoader(
                config,
                List.of(frameworkModule.getLayer()),
                ClassLoader.getSystemClassLoader()).layer();

        return layer.findLoader(moduleName).loadClass(className);
    }
}