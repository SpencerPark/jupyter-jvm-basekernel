package io.github.spencerpark.jupyter.kernel.magic.registry;

import io.github.spencerpark.jupyter.kernel.magic.MagicParserTest;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MagicsArgsTest {
    private static MagicsArgs args(Consumer<MagicsArgs.MagicsArgsBuilder> config) {
        MagicsArgs.MagicsArgsBuilder builder = MagicsArgs.builder();
        config.accept(builder);
        return builder.build();
    }

    private static List<String> list(String... args) {
        return Arrays.asList(args);
    }

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(args(b -> b.required("a")),
                        "value-a",
                        hasEntry("a", list("value-a"))),
                Arguments.of(args(b -> b.required("a").optional("b")),
                        "value-a",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list())
                        )),
                Arguments.of(args(b -> b.required("a").optional("b")),
                        "value-a value-b",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list("value-b"))
                        )),
                Arguments.of(args(b -> b.required("a").optional("b").varargs("c")),
                        "value-a value-b",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list("value-b")),
                                hasEntry("c", list())
                        )),
                Arguments.of(args(b -> b.required("a").optional("b").varargs("c")),
                        "value-a value-b value-c",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list("value-b")),
                                hasEntry("c", list("value-c"))
                        )),
                Arguments.of(args(b -> b.required("a").optional("b").varargs("c")),
                        "value-a value-b value-c-1 value-c-2",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list("value-b")),
                                hasEntry("c", list("value-c-1", "value-c-2"))
                        )),
                Arguments.of(args(b -> b.required("a").required("b").varargs("c")),
                        "value-a value-b value-c-1 value-c-2",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list("value-b")),
                                hasEntry("c", list("value-c-1", "value-c-2"))
                        )),
                Arguments.of(args(b -> b.optional("a")), "", hasEntry("a", list())),
                Arguments.of(args(b -> b.optional("a").varargs("b")),
                        "",
                        allOf(
                                hasEntry("a", list()),
                                hasEntry("b", list())
                        )),
                Arguments.of(args(b -> b.optional("a").varargs("b")),
                        "value-a",
                        allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list())
                        )),
                Arguments.of(args(b -> b.optional("a").varargs("b")),
                        "value-a", allOf(
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list())
                        )),
                Arguments.of(args(b -> b.varargs("a")),
                        "",
                        hasEntry("a", list())),
                Arguments.of(args(b -> b.varargs("a")),
                        "value-a",
                        hasEntry("a", list("value-a"))),
                Arguments.of(args(b -> b.required("a").optional("a")),
                        "value-a extra-a",
                        hasEntry("a", list("value-a", "extra-a"))),
                Arguments.of(args(b -> b.required("a").optional("a")),
                        "value-a",
                        hasEntry("a", list("value-a"))),
                Arguments.of(args(b -> b.required("a").varargs("a")),
                        "value-a extra-a extra-a-2",
                        hasEntry("a", list("value-a", "extra-a", "extra-a-2"))),
                // FLAGS
                Arguments.of(args(b -> {}), "-f", hasEntry("f", list(""))),
                Arguments.of(args(b -> {}), "-fff", hasEntry("f", list("", "", ""))),
                Arguments.of(args(b -> {}), "-fg -g", allOf(
                        hasEntry("f", list("")),
                        hasEntry("g", list("", ""))
                )),
                Arguments.of(args(b -> b.flag("test", 'f')), "", hasEntry("test", list())),
                Arguments.of(args(b -> b.flag("verbose", 'v', "true")),
                        "-v",
                        hasEntry("verbose", list("true"))),
                // KEYWORDS
                Arguments.of(args(b -> {}), "--f=10", hasEntry("f", list("10"))),
                Arguments.of(args(b -> {}), "--f=10 --f=11", hasEntry("f", list("10", "11"))),
                Arguments.of(args(b -> {}), "--f 10 --f=11 --f 12", hasEntry("f", list("10", "11", "12"))),
                Arguments.of(args(b -> b.keyword("test")), "--test=10 --test 11 --test=12", hasEntry("test", list("10", "11", "12"))),
                Arguments.of(args(b -> b.keyword("test", MagicsArgs.KeywordSpec.REPLACE)),
                        "--test=10 --test 11 --test=12",
                        hasEntry("test", list("12"))),
                Arguments.of(args(b -> b.keyword("test")), "", hasEntry("test", list())),
                // FLAGS and KEYWORDS
                Arguments.of(args(b -> b.flag("log-level", 'v', "100").keyword("log-level")),
                        "-v --log-level=200 --log-level 300",
                        hasEntry("log-level", list("100", "200", "300"))),
                // POSITIONALS and FLAGS and KEYWORDS
                Arguments.of(args(b -> b.required("a").optional("b").flag("log-level", 'v', "100").keyword("log-level")),
                        "-v value-a --log-level=200 value-b --log-level 300",
                        allOf(
                                hasEntry("log-level", list("100", "200", "300")),
                                hasEntry("a", list("value-a")),
                                hasEntry("b", list("value-b"))
                        )),
                // Exceptions
                Arguments.of(args(b -> b.required("a")), "", null),
                Arguments.of(args(b -> b.required("a")), "value-a extra-a", null),
                Arguments.of(args(b -> b.optional("a")), "value-a extra-a", null),
                Arguments.of(args(b -> b.onlyKnownKeywords()), "--unknown=val", null),
                Arguments.of(args(b -> b.onlyKnownKeywords()), "--unknown val", null),
                Arguments.of(args(b -> b.onlyKnownFlags()), "-idk", null),
                Arguments.of(args(b -> b.flag("test", 'i').onlyKnownFlags()), "-idk", null),
                Arguments.of(args(b -> b.keyword("a", MagicsArgs.KeywordSpec.ONCE)), "--a a --a not-ok...", null),
                // Strange
                Arguments.of(args(b -> b.keyword("a")),
                        "\"--a=value with spaces\"",
                        hasEntry("a", list("value with spaces"))),
                Arguments.of(args(b -> b.keyword("a")),
                        "--a=\"value with spaces\"",
                        hasEntry("a", list("value with spaces"))),
                Arguments.of(args(b -> b.keyword("a")),
                        "--a \"value with spaces\"",
                        hasEntry("a", list("value with spaces")))
        );
    }

    @ParameterizedTest(name = "{index}: \"{0}\" with \"{1}\"")
    @MethodSource("data")
    public void test(MagicsArgs schema, String args, Matcher<Map<String, List<String>>> test) {
        List<String> rawArgs = MagicParserTest.split(args);

        if (test == null) {
            assertThrows(MagicArgsParseException.class, () -> schema.parse(rawArgs));
        } else {
            Map<String, List<String>> parsed = schema.parse(rawArgs);
            assertThat(parsed, test);
        }
    }
}