package app.packed.time;

import java.time.Clock;

public class Tstss {

    public static void main(String[] args) {
        System.out.println(Clock.systemUTC().instant());
        System.out.println(Clock.systemDefaultZone().instant());
    }
}
