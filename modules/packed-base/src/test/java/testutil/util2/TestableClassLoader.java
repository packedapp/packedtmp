/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testutil.util2;

/**
 *
 */
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Kasper Nielsen
 */
public class TestableClassLoader extends ClassLoader {

    public static final String URL_PROTOCOL = "packed";

    /** The binary definition of all classes. */
    private final ConcurrentHashMap<String, byte[]> classes = new ConcurrentHashMap<>();

    private boolean findResourcesDisabled;

    /** the binary definition definition of all agents. */
    final ConcurrentHashMap<String, List<byte[]>> resources = new ConcurrentHashMap<>();

    public TestableClassLoader() {}

    public TestableClassLoader(ClassLoader parent) {
        super(parent);
    }

    // ///////////////////////// HELPER METHODS ///////////////////////

    /**
     * Adds the definition of the class.
     *
     * @param name
     *            the name of the class
     * @param def
     *            the binary definition of the class
     * @return this class loader
     */
    public TestableClassLoader addClass(String name, byte[] def) {
        classes.put(name, def);
        return this;
    }

    public TestableClassLoader addResource(String name, byte[] contents) {
        if (!resources.containsKey(name)) {
            resources.put(name, new ArrayList<byte[]>());
        }
        resources.get(name).add(requireNonNull(contents));
        return this;
    }

    /**
     * Adds the definition of the agent
     *
     * @param name
     *            the name of the agent
     * @param lines
     *            the lines to add to the agent
     * @return this class loader
     */
    public TestableClassLoader addResourceWithLines(String name, String... lines) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        for (String l : lines) {
            pw.println(l);
        }
        pw.flush();
        return addResource(name, baos.toByteArray());
    }

    /** Clears all definitions of classes or agents. */
    public TestableClassLoader clear() {
        classes.clear();
        resources.clear();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> findClass(String s) throws ClassNotFoundException {
        byte[] bytes = classes.get(s);
        if (bytes == null) {
            return super.findClass(s);
        } else {
            return defineClass(s, bytes, 0, bytes.length);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) {
        return getResources(name, true);
    }

    public Enumeration<URL> getResources(String name, boolean includeParents) {
        Iterator<Entry<String, List<byte[]>>> iterator = resources.entrySet().iterator();

        Enumeration<URL> superResources;
        if (includeParents) {
            try {
                superResources = super.getResources(name);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            superResources = Collections.emptyEnumeration();
        }

        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext() || superResources.hasMoreElements();
            }

            @Override
            public URL nextElement() {
                if (iterator.hasNext()) {
                    String archiveId = iterator.next().getKey();
                    return createResourceUrl(archiveId, name);
                }
                return superResources.nextElement();
            }
        };
    }

    private URL createResourceUrl(String archiveId, String path) {
        String host = System.identityHashCode(this) + '.' + archiveId;
        try {
            return new URL(URL_PROTOCOL, host, 0, "/" + path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    // ///////////////////////// CLASSLOADER METHODS ///////////////////////

    /** {@inheritDoc} */
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        if (findResourcesDisabled) {
            throw new AssertionError("FindResources has been disabled");
        }
        ArrayList<URL> result = new ArrayList<>();
        List<byte[]> l = resources.get(name);
        if (l == null) {
            l = new ArrayList<>();
        }
        for (byte[] b : l) {
            result.add(createURL(b));
        }
        return Collections.enumeration(result);
    }

    /**
     * Makes sure {@link #findResources(String)} will fail with {@link AssertionError}.
     *
     * @param disabled
     *            whether or not findResources should be disabled
     * @return this class loader
     */
    public TestableClassLoader setFindResourceDisabled(boolean disabled) {
        this.findResourcesDisabled = disabled;
        return this;
    }

    static URL createURL(final byte[] contents) throws MalformedURLException {
        return new URL("", "", 0, "", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new URLConnection(u) {

                    @Override
                    public void connect() throws IOException {}

                    /** {@inheritDoc} */
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(contents);
                    }
                };
            }
        });
    }
}