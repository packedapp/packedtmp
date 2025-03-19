package internal.app.packed.bean.proxy;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.reflect.AccessFlag;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LookupUsage {

    public static void main(String[] args) throws IOException {
        record Entry(ClassModel cm, MethodModel mm) {}
        Map<Integer, List<Entry>> hits = new HashMap<>();
        Path javaBasePath = FileSystems.getFileSystem(java.net.URI.create("jrt:/")).getPath("/modules/java.base/java/");
        Files.walk(javaBasePath).filter(path -> path.toString().endsWith(".class")).forEach(classFile -> {
            try {
                ClassModel cm = ClassFile.of().parse(classFile);
                if (cm.flags().has(AccessFlag.PUBLIC)) {
                    for (MethodModel mm : cm.methods()) {
                        if (mm.flags().has(AccessFlag.PUBLIC)) {
                            for (int i = 0; i < mm.methodTypeSymbol().parameterCount(); i++) {
                                if (mm.methodTypeSymbol().parameterType(i).descriptorString().equals("Ljava/lang/invoke/MethodHandles$Lookup;")) {
                                    hits.computeIfAbsent(i, _ -> new ArrayList<>()).add(new Entry(cm, mm));
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        for (var e : hits.entrySet()) {
            System.out.println("\n------" + e.getKey() + " parameter");
            e.getValue().forEach(v -> {
                System.out.println(v.cm.thisClass().asInternalName() + "." + v.mm.methodName()
                        + v.mm.methodTypeSymbol().parameterList().stream().map(f -> f.displayName()).collect(Collectors.joining(", ", "(", ")")));
            });
        }
    }
}
