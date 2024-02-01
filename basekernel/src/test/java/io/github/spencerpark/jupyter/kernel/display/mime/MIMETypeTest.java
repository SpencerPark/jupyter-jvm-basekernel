package io.github.spencerpark.jupyter.kernel.display.mime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MIMETypeTest {
    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("application/json", "application", null, "json", null),
                Arguments.of("application/xhtml+xml", "application", null, "xhtml", "xml"),
                Arguments.of("image/*", "image", null, "*", null),
                Arguments.of("image/", "image", null, "", null),
                Arguments.of("video", "video", null, null, null),
                Arguments.of("video", "video", null, null, null),
                Arguments.of("application/vnd.media", "application", "vnd", "media", null),
                Arguments.of("application/vnd.media.producer", "application", "vnd", "media.producer", null),
                Arguments.of("application/vnd.media.producer+suffix", "application", "vnd", "media.producer", "suffix"),
                Arguments.of("application/vnd.media.named+producer+suffix", "application", "vnd", "media.named+producer", "suffix")
        );
    }

    @ParameterizedTest(name = "{index}: MIMEType.parse({0}) = new MIMEType({1}, {2}, {3}, {4})")
    @MethodSource("data")
    public void test(String raw, String type, String tree, String subtype, String suffix) {
        MIMEType parsed = MIMEType.parse(raw);
        MIMEType expected = new MIMEType(type, tree, subtype, suffix);

        assertEquals(expected, parsed);
    }
}