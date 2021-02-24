package app.packed.time.test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import app.packed.cli.Main;
import app.packed.container.BaseAssembly;
import app.packed.hooks.AutoService;
import app.packed.time.TimeWirelet;

@AutoService
public class ZestMe extends BaseAssembly {


    @Override
    protected void build() {
        time().clock(Clock.fixed(Instant.now(), ZoneId.systemDefault()));

        scheduler().schedule(() -> System.out.println("HejHej")).atFixedRate(213, 123, TimeUnit.HOURS);
    }
    public static void main(String[] args) {
        Main.run(new ZestMe(), TimeWirelet.clock(Clock.systemUTC()));
    }
}
