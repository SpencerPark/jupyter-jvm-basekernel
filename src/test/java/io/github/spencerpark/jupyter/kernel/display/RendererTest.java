package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RendererTest {
    private Renderer renderer;

    @Before
    public void setUp() throws Exception {
        this.renderer = new Renderer();
        this.renderer.createRegistration(W.class)
                .preferring(MIMEType.TEXT_HTML)
                .supporting(MIMEType.TEXT_LATEX)
                .register((W w, RenderContext ctx) -> {
                    ctx.renderIfRequested(MIMEType.TEXT_HTML, w::w);
                    ctx.renderIfRequested(MIMEType.TEXT_LATEX, () -> "x");
                });
    }

    class T {
        @Override
        public String toString() {
            return "T";
        }
    }

    class U implements DisplayDataRenderable {
        @Override
        public Set<MIMEType> getSupportedRenderTypes() {
            return Collections.singleton(MIMEType.TEXT_MARKDOWN);
        }

        @Override
        public Set<MIMEType> getPreferredRenderTypes() {
            return Collections.singleton(MIMEType.TEXT_MARKDOWN);
        }

        @Override
        public void render(RenderContext context) {
            context.renderIfRequested(MIMEType.TEXT_MARKDOWN, () -> "**U**");
        }

        @Override
        public String toString() {
            return "U";
        }
    }

    class V implements DisplayDataRenderable {
        private final Set<MIMEType> supported = new LinkedHashSet<>();

        V() {
            this.supported.add(MIMEType.TEXT_MARKDOWN);
            this.supported.add(MIMEType.TEXT_CSS);
        }

        @Override
        public Set<MIMEType> getSupportedRenderTypes() {
            return supported;
        }

        @Override
        public Set<MIMEType> getPreferredRenderTypes() {
            return Collections.singleton(MIMEType.TEXT_CSS);
        }

        @Override
        public void render(RenderContext context) {
            context.renderIfRequested(MIMEType.TEXT_MARKDOWN, () -> "**V**");
            context.renderIfRequested(MIMEType.TEXT_CSS, () -> ".v{}");
        }

        @Override
        public String toString() {
            return "V";
        }
    }

    class W {
        public String w() {
            return "w";
        }

        @Override
        public String toString() {
            return "W";
        }
    }

    @Test
    public void rendersPlainText() {
        DisplayData data = this.renderer.render(new T());

        assertEquals("T", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void alwaysRendersPlainText() {
        DisplayData data = this.renderer.render(new U());

        assertEquals("U", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void rendersPreferred() {
        DisplayData data = this.renderer.render(new U());

        assertEquals("**U**", data.getData(MIMEType.TEXT_MARKDOWN));
    }

    @Test
    public void rendersJustPreferred() {
        DisplayData data = this.renderer.render(new V());

        assertEquals(".v{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("V", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_MARKDOWN));
    }

    @Test
    public void rendersExternal() {
        DisplayData data = this.renderer.render(new W());

        assertEquals("w", data.getData(MIMEType.TEXT_HTML));
        assertEquals("W", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_LATEX));
    }

    @Test
    public void rendersAs() {
        DisplayData data = this.renderer.renderAs(new V(), "text/markdown");

        assertEquals("**V**", data.getData(MIMEType.TEXT_MARKDOWN));
        assertEquals("V", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_CSS));
    }

    @Test
    public void rendersAsExternal() {
        DisplayData data = this.renderer.renderAs(new W(), "text/latex");

        assertEquals("x", data.getData(MIMEType.TEXT_LATEX));
        assertEquals("W", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_HTML));
    }
}