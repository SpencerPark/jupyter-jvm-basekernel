package io.github.spencerpark.jupyter.kernel.extension;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContainerClassLoaderTest {
    private static String getWhereFrom(Class clazz) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField("FROM");
        return (String) field.get(null);
    }

    private static String getWhereFrom(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Jar with classes A, B, C in the default package all with static field FROM = "jar 1".
     */
    URL jar1;

    /**
     * Jar with classes D, E, F in the default package all with static field FROM = "jar 2".
     */
    URL jar2;

    /**
     * Folder with classes A, B, C in the default package all with static field FROM = "folder 1".
     */
    URL folder1;

    /**
     * Folder with classes C, D, E in the default package all with static field FROM = "folder 2".
     */
    URL folder2;

    @Before
    public void setUp() throws Exception {
        this.jar1 = ContainerClassLoaderTest.class.getResource("ContainerClassLoaderTest/jar 1.jar");
        this.jar2 = ContainerClassLoaderTest.class.getResource("ContainerClassLoaderTest/jar 2.jar");
        this.folder1 = ContainerClassLoaderTest.class.getResource("ContainerClassLoaderTest/folder 1/");
        this.folder2 = ContainerClassLoaderTest.class.getResource("ContainerClassLoaderTest/folder 2/");
    }

    @Test
    public void testPrefersContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        Class C = container.loadClass("C");
        assertEquals("jar 2", getWhereFrom(C));

        Class A = container.loadClass("A");
        assertEquals("jar 1", getWhereFrom(A));
    }

    @Test
    public void testPrefersContainerEvenIfLoaded() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        Class C1 = app.loadClass("C");
        assertEquals("jar 1", getWhereFrom(C1));

        Class C2 = container.loadClass("C");
        assertEquals("jar 2", getWhereFrom(C2));

        assertNotEquals(C1, C2);
    }

    @Test
    public void testLoadsViaClassForName() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        Class C = Class.forName("C", true, container);
        assertEquals("jar 2", getWhereFrom(C));

        assertEquals(container, C.getClassLoader());
    }

    @Test
    public void testPrefersResourcesFromContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        InputStream a1 = app.getResourceAsStream("a.txt");
        assertEquals("jar 1", getWhereFrom(a1));

        InputStream a2 = container.getResourceAsStream("a.txt");
        assertEquals("jar 2", getWhereFrom(a2));
    }

    @Test
    public void testSuperClassAlsoPrefersContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        Class D = container.loadClass("D");
        assertEquals("jar 2", getWhereFrom(D));

        Class superC = D.getSuperclass();
        assertEquals("jar 2", getWhereFrom(superC));
    }

    @Test
    public void testReferencedClassAlsoPrefersContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        Class E = container.loadClass("E");
        assertEquals("jar 2", getWhereFrom(E));

        Field refCField = E.getDeclaredField("C");
        Class refC = (Class) refCField.get(null);
        assertEquals("jar 2", getWhereFrom(refC));
    }

    @Test
    public void testCanImplementInterfaceFromParent() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ jar1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ jar2 }, app);

        Class E = container.loadClass("E");
        assertEquals("jar 2", getWhereFrom(E));

        Class implementedA = E.getInterfaces()[0];
        assertEquals("jar 1", getWhereFrom(implementedA));
    }

    @Test
    public void testExplodedPrefersContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        Class C = container.loadClass("C");
        assertEquals("folder 2", getWhereFrom(C));

        Class A = container.loadClass("A");
        assertEquals("folder 1", getWhereFrom(A));
    }

    @Test
    public void testExplodedPrefersContainerEvenIfLoaded() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        Class C1 = app.loadClass("C");
        assertEquals("folder 1", getWhereFrom(C1));

        Class C2 = container.loadClass("C");
        assertEquals("folder 2", getWhereFrom(C2));

        assertNotEquals(C1, C2);
    }

    @Test
    public void testExplodedLoadsViaClassForName() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        Class C = Class.forName("C", true, container);
        assertEquals("folder 2", getWhereFrom(C));

        assertEquals(container, C.getClassLoader());
    }

    @Test
    public void testExplodedPrefersResourcesFromContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        InputStream a1 = app.getResourceAsStream("a.txt");
        assertEquals("folder 1", getWhereFrom(a1));

        InputStream a2 = container.getResourceAsStream("a.txt");
        assertEquals("folder 2", getWhereFrom(a2));
    }

    @Test
    public void testExplodedSuperClassAlsoPrefersContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        Class D = container.loadClass("D");
        assertEquals("folder 2", getWhereFrom(D));

        Class superC = D.getSuperclass();
        assertEquals("folder 2", getWhereFrom(superC));
    }

    @Test
    public void testExplodedReferencedClassAlsoPrefersContainer() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        Class E = container.loadClass("E");
        assertEquals("folder 2", getWhereFrom(E));

        Field refCField = E.getDeclaredField("C");
        Class refC = (Class) refCField.get(null);
        assertEquals("folder 2", getWhereFrom(refC));
    }

    @Test
    public void testExplodedCanImplementInterfaceFromParent() throws Exception {
        URLClassLoader app = new URLClassLoader(new URL[]{ folder1 }, null);
        ContainerClassLoader container = new ContainerClassLoader(new URL[]{ folder2 }, app);

        Class E = container.loadClass("E");
        assertEquals("folder 2", getWhereFrom(E));

        Class implementedA = E.getInterfaces()[0];
        assertEquals("folder 1", getWhereFrom(implementedA));
    }
}