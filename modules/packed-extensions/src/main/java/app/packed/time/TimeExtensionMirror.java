package app.packed.time;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Set;

import app.packed.base.Key;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;

@ExtensionMember(TimeExtension.class)
public class TimeExtensionMirror extends ExtensionMirror {

    public Set<Key<ZoneId>> zones() {
        throw new UnsupportedOperationException();
    }

    public Set<Key<Clock>> clocks() {
        throw new UnsupportedOperationException();
    }
}
