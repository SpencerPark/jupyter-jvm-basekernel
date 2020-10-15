package io.github.spencerpark.jupyter.kernel.magic.registry;

import io.github.spencerpark.jupyter.api.magic.registry.*;
import io.github.spencerpark.jupyter.kernel.magic.StaticMagics;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DefaultMagicsTest {
    private DefaultMagics magics;

    @Before
    public void setUp() {
        magics = new DefaultMagics();
    }

    @Test
    public void lineMagic() throws Exception {
        magics.registerLineMagic("test", args -> args);

        List<String> args = Arrays.asList("arg1", "arg2");
        List<String> out = magics.applyLineMagic("test", args);

        assertEquals(args, out);
    }

    @Test
    public void cellMagic() throws Exception {
        magics.registerCellMagic("test", (args, body) -> {
            List<String> out = new LinkedList<>();
            out.addAll(args);
            out.add(body);
            return out;
        });

        List<String> args = Arrays.asList("arg1", "arg2");
        String body = "body";
        List<String> out = magics.applyCellMagic("test", args, body);

        List<String> expected = new LinkedList<>();
        expected.addAll(args);
        expected.add(body);

        assertEquals(expected, out);
    }

    @Test
    public void lineCellMagic() throws Exception {
        class Magic implements LineMagicFunction<List<String>>, CellMagicFunction<List<String>> {
            @Override
            public List<String> execute(List<String> args, String body) throws Exception {
                List<String> out = new LinkedList<>();
                out.addAll(args);
                out.add(body);
                return out;
            }

            @Override
            public List<String> execute(List<String> args) throws Exception {
                return args;
            }
        }

        magics.registerLineCellMagic("test", new Magic());

        List<String> args = Arrays.asList("arg1", "arg2");
        String body = "body";

        List<String> lineOut = magics.applyLineMagic("test", args);
        List<String> cellOut = magics.applyCellMagic("test", args, body);

        List<String> expectedCell = new LinkedList<>();
        expectedCell.addAll(args);
        expectedCell.add(body);

        assertEquals(args, lineOut);
        assertEquals(expectedCell, cellOut);
    }

    @Test
    public void reflectionLineMagics() throws Exception {
        class Magic {
            @LineMagic
            public void list(List<String> args) {
            }

            @LineMagic
            public void iterable(Iterable<String> args) {
            }

            @LineMagic("named")
            public void unusedName(List<String> args) {
            }

            @LineMagic
            public int returnInt(List<String> args) {
                return args.size();
            }
        }

        magics.registerMagics(new Magic());

        List<String> args = Arrays.asList("arg1", "arg2");

        magics.applyLineMagic("list", args);
        magics.applyLineMagic("iterable", args);
        magics.applyLineMagic("named", args);
        assertEquals((Integer) 2, magics.applyLineMagic("returnInt", args));

        try {
            magics.applyLineMagic("unusedName", args);
            fail("named magic was also registered under the method name");
        } catch (UndefinedMagicException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void badReflectionLineMagicsType() {
        class BadMagic {
            @LineMagic
            public void set(Set<String> args) {
            }
        }

        magics.registerMagics(new BadMagic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badReflectionLineMagicsTypeParam() {
        class BadMagic {
            @LineMagic
            public void intList(List<Integer> args) {
            }
        }

        magics.registerMagics(new BadMagic());
    }

    @Test
    public void reflectionCellMagics() throws Exception {
        class Magic {
            @CellMagic
            public void list(List<String> args, String body) {
            }

            @CellMagic
            public void iterable(Iterable<String> args, String body) {
            }

            @CellMagic("named")
            public void unusedName(List<String> args, String body) {
            }

            @CellMagic
            public int returnInt(List<String> args, String body) {
                return args.size();
            }
        }

        magics.registerMagics(new Magic());

        List<String> args = Arrays.asList("arg1", "arg2");
        String body = "body";

        magics.applyCellMagic("list", args, body);
        magics.applyCellMagic("iterable", args, body);
        magics.applyCellMagic("named", args, body);
        assertEquals((Integer) 2, magics.applyCellMagic("returnInt", args, body));

        try {
            magics.applyCellMagic("unusedName", args, body);
            fail("named magic was also registered under the method name");
        } catch (UndefinedMagicException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void badReflectionCellMagicsType() {
        class BadMagic {
            @LineMagic
            public void set(Set<String> args, String body) {
            }
        }

        magics.registerMagics(new BadMagic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badReflectionCellMagicsTypeParam() {
        class BadMagic {
            @LineMagic
            public void intList(List<Integer> args, String body) {
            }
        }

        magics.registerMagics(new BadMagic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badReflectionCellMagicsBodyTypeParam() {
        class BadMagic {
            @LineMagic
            public void body(List<String> args, Character body) {
            }
        }

        magics.registerMagics(new BadMagic());
    }

    @Test
    public void reflectionLineCellMagics() throws Exception {
        class Magic {
            @LineMagic
            @CellMagic
            public void list(List<String> args, String body) {
            }

            @LineMagic
            @CellMagic
            public void iterable(Iterable<String> args, String body) {
            }

            @LineMagic("lineNamed")
            @CellMagic("cellNamed")
            public void unusedName(List<String> args, String body) {
            }

            @LineMagic
            @CellMagic
            public int returnInt(List<String> args, String body) {
                return args.size() + (body == null ? 1 : 2);
            }
        }

        magics.registerMagics(new Magic());

        List<String> args = Arrays.asList("arg1", "arg2");
        String body = "body";

        magics.applyLineMagic("list", args);
        magics.applyLineMagic("iterable", args);
        magics.applyLineMagic("lineNamed", args);
        assertEquals((Integer) 3, magics.applyLineMagic("returnInt", args));

        magics.applyCellMagic("list", args, body);
        magics.applyCellMagic("iterable", args, body);
        magics.applyCellMagic("cellNamed", args, body);
        assertEquals((Integer) 4, magics.applyCellMagic("returnInt", args, body));

        try {
            magics.applyCellMagic("unusedName", args, body);
            fail("named magic was also registered under the method name");
        } catch (UndefinedMagicException ignored) {
        }
    }

    @Test
    public void statefulMagic() throws Exception {
        class Magic {
            private int i = 0;

            @LineMagic
            public int getAndIncrement() {
                return i++;
            }
        }

        magics.registerMagics(new Magic());

        for (int i = 0; i < 3; i++)
            assertEquals((Integer) i, magics.applyLineMagic("getAndIncrement", Collections.emptyList()));
    }

    @Test
    public void staticMagic() throws Exception {
        magics.registerMagics(StaticMagics.class);

        assertEquals((Integer) 0, magics.applyLineMagic("staticMagic", Collections.emptyList()));
        assertEquals("body", magics.applyCellMagic("staticMagic", Collections.emptyList(), "body"));
    }
}