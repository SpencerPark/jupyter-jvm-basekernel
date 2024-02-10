package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenderRequestTypesResolutionTest {
    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("image/svg+xml", "image/svg+xml", List.of("image/svg+xml")),
                Arguments.of("image/svg+xml", "image/svg", List.of("image/svg")),
                Arguments.of("image/svg+xml", "image/svg+xml", List.of("image/*")),
                Arguments.of("image/svg+xml", "image/svg+xml", List.of("image")),
                Arguments.of("image/svg+xml", "application/xml", List.of("application/xml")),
                Arguments.of("image/svg+xml", "application/xml", List.of("application/*")),
                Arguments.of("image/svg+xml", "application/xml", List.of("application")),
                Arguments.of("image/svg+xml", "image/svg+xml", List.of("*")),

                Arguments.of("image/svg", "image/svg", List.of("image/svg")),

                Arguments.of("image/svg", null, List.of("application/xml")),
                Arguments.of("image/svg+xml", null, List.of("application/json"))
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void test(String supported, String expected, List<String> requestTypes) {
        RenderRequestTypes.Builder builder = new RenderRequestTypes.Builder(group -> {
            switch (group) {
                case "xml":
                    return MIMEType.APPLICATION_XML;
                case "json":
                    return MIMEType.APPLICATION_JSON;
                default:
                    return null;
            }
        });
        requestTypes.stream()
                .map(MIMEType::parse)
                .forEach(builder::withType);
        RenderRequestTypes renderRequestTypes = builder.build();

        MIMEType actual = renderRequestTypes.resolveSupportedType(MIMEType.parse(supported));
        assertEquals(expected == null ? null : MIMEType.parse(expected), actual);
    }
}