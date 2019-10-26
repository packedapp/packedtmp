package packed.internal.reflect;

import java.util.stream.Stream;

public class ParOrderedPeek {
    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("java.version"));
        System.out.println("Sequential with generate ---------------------");
        doIt(false, false);
        System.out.println("Sequential with iterate ----------------------");
        doIt(false, true);
        System.out.println("Parallel with generate -----------------------");
        doIt(true, false);
        System.out.println("Parallel with iterate ------------------------");
        doIt(true, true);
    }

    public static void doIt(boolean parallel, boolean useIterate) {
        Stream<Integer> si = (useIterate ? Stream.iterate(1, x -> 1) : Stream.generate(() -> 1))
                // .unordered()
                .limit(10)
        // .unordered()
        ;
        if (parallel)
            si = si.parallel();
        si = si.peek(x -> System.out.print("."));

        si.count();

        // System.out.println("Stream count " + Arrays.toString(count));

    }
}
