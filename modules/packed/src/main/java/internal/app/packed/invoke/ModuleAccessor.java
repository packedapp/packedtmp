package internal.app.packed.invoke;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessFlag;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ModuleAccessor {

    private static final Module THIS_MODULE = ModuleAccessor.class.getModule();

    // Level 2 Cache: Module -> Master Lookup (the one with the MODULE bit)
    // WeakHashMap allows the Module to be GC'd when its ClassLoader is unloaded.
    private static final Map<Module, Lookup> PER_MODULE_CACHE =
        Collections.synchronizedMap(new WeakHashMap<>());

    // Level 1 Cache: Class -> Specific Full Privilege Lookup
    private static final ClassValue<Lookup> PER_CLASS_CACHE = new ClassValue<>() {
        @Override
        protected Lookup computeValue(Class<?> type) {
            return resolveFullPrivilegeLookup(type);
        }
    };

    /**
     * Entry Point: Gets the full privilege lookup for any class.
     * Only works if the package is open to this library.
     */
    public static Lookup getLookup(Class<?> targetClass) {
        return PER_CLASS_CACHE.get(targetClass);
    }

    private static Lookup resolveFullPrivilegeLookup(Class<?> targetClass) {
        Module targetModule = targetClass.getModule();
        String packageName = targetClass.getPackageName();

        // 1. Security Check: Is the specific package open to us?
        if (!targetModule.isOpen(packageName, THIS_MODULE)) {
            throw new IllegalAccessError("Package " + packageName + " in module " +
                targetModule.getName() + " is not open to " + THIS_MODULE.getName());
        }

        // 2. Get the Master Lookup for the module
        Lookup masterLookup = PER_MODULE_CACHE.computeIfAbsent(targetModule, _ -> {
            // We use the current targetClass as the "anchor" for injection
            return injectMasterSpy(targetClass);
        });

        // 3. Teleport the Master Lookup to the specific class.
        // This adds 'PRIVATE' access while keeping the 'MODULE' bit.
        try {
            return MethodHandles.privateLookupIn(targetClass, masterLookup);
        } catch (IllegalAccessException e) {
            // This happens if access was revoked dynamically
            throw new RuntimeException("Access revoked for " + targetClass.getName(), e);
        }
    }

    private static Lookup injectMasterSpy(Class<?> anchorClass) {
        try {
            // Get a standard restricted lookup in the open package
            Lookup packageLookup = MethodHandles.privateLookupIn(anchorClass, MethodHandles.lookup());

            // Generate the spy bytecode
            String spyName = anchorClass.getPackageName() + ".PackedLookupHelper";
            byte[] bytes = generateSpyBytes(spyName);

            // Define into the user module
            Class<?> spyClass;
            try {
                spyClass = packageLookup.defineClass(bytes);
            } catch (LinkageError e) {
                // Handle race conditions where another thread injected first
                spyClass = Class.forName(spyName, true, anchorClass.getClassLoader());
            }

            // Capture the Lookup that carries the MODULE bit
            return (Lookup) spyClass.getMethod("get").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed for module " + anchorClass.getModule().getName(), e);
        }
    }

    private static byte[] generateSpyBytes(String fullClassName) {
        ClassFile cf = ClassFile.of();
        ClassDesc spyDesc = ClassDesc.ofInternalName(fullClassName.replace('.', '/'));
        ClassDesc lookupDesc = ClassDesc.ofDescriptor("Ljava/lang/invoke/MethodHandles$Lookup;");

        return cf.build(spyDesc, cb -> {
            cb.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL);
            cb.withMethod("get", MethodTypeDesc.of(lookupDesc),
                AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(),
                mb -> mb.withCode(code -> {
                    code.invokestatic(ClassDesc.of("java.lang.invoke.MethodHandles"),
                                     "lookup", MethodTypeDesc.of(lookupDesc));
                    code.areturn();
                }));
        });
    }
}