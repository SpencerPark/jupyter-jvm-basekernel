package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.display.DisplayDataRenderable;
import io.github.spencerpark.jupyter.api.display.RenderContext;
import io.github.spencerpark.jupyter.api.display.Renderer;
import io.github.spencerpark.jupyter.api.display.mime.MIMEType;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DefaultRendererTest {
    private Renderer renderer;

    @Before
    public void setUp() throws Exception {
        this.renderer = new DefaultRenderer();
        this.renderer.createRegistration(D.class)
                .preferring(MIMEType.TEXT_HTML)
                .supporting(MIMEType.TEXT_LATEX)
                .register((D d, RenderContext ctx) -> {
                    ctx.renderIfRequested(MIMEType.TEXT_HTML, d::html);
                    ctx.renderIfRequested(MIMEType.TEXT_LATEX, () -> "\\d");
                });
        this.renderer.createRegistration(F.class)
                .supporting(MIMEType.ANY)
                .register((F f, RenderContext ctx) -> {
                    ctx.renderIfRequested(MIMEType.TEXT_HTML, f::html);
                    ctx.renderIfRequested(MIMEType.TEXT_CSS, f::css);
                    ctx.renderIfRequested(MIMEType.APPLICATION_JAVASCRIPT, f::js);
                });
        this.renderer.createRegistration(H.class)
                .supporting(MIMEType.parse("text/*"))
                .supporting(MIMEType.APPLICATION_JAVASCRIPT)
                .register((H h, RenderContext ctx) -> {
                    ctx.renderIfRequested(MIMEType.TEXT_HTML, h::html);
                    ctx.renderIfRequested(MIMEType.TEXT_CSS, h::css);
                    ctx.renderIfRequested(MIMEType.APPLICATION_JAVASCRIPT, h::js);
                });
        this.renderer.createRegistration(J.class)
                .supporting(MIMEType.TEXT_PLAIN)
                .supporting(MIMEType.APPLICATION_JAVASCRIPT)
                .register((J j, RenderContext ctx) -> {
                    ctx.renderIfRequested(MIMEType.APPLICATION_JAVASCRIPT, j::js);
                    ctx.renderIfRequested(MIMEType.TEXT_PLAIN, j::pretty);
                });
    }

    class A {
        @Override
        public String toString() {
            return "A";
        }
    }

    class B implements DisplayDataRenderable {
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
            context.renderIfRequested(MIMEType.TEXT_MARKDOWN, () -> "**B**");
        }

        @Override
        public String toString() {
            return "B";
        }
    }

    class C implements DisplayDataRenderable {
        private final Set<MIMEType> supported = new LinkedHashSet<>();

        C() {
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
            context.renderIfRequested(MIMEType.TEXT_MARKDOWN, () -> "**C**");
            context.renderIfRequested(MIMEType.TEXT_CSS, () -> ".c{}");
        }

        @Override
        public String toString() {
            return "C";
        }
    }

    class D {
        public String html() {
            return "<d></d>";
        }

        @Override
        public String toString() {
            return "D";
        }
    }

    class E implements DisplayDataRenderable {
        @Override
        public Set<MIMEType> getSupportedRenderTypes() {
            return Collections.singleton(MIMEType.ANY);
        }

        @Override
        public void render(RenderContext context) {
            context.renderIfRequested(MIMEType.TEXT_HTML, () -> "<e></e>");
            context.renderIfRequested(MIMEType.TEXT_CSS, () -> ".e{}");
            context.renderIfRequested(MIMEType.APPLICATION_JAVASCRIPT, () -> "e();");
        }

        @Override
        public String toString() {
            return "E";
        }
    }

    class F {
        public String html() {
            return "<f></f>";
        }

        public String css() {
            return ".f{}";
        }

        public String js() {
            return "f();";
        }

        @Override
        public String toString() {
            return "F";
        }
    }

    class G implements DisplayDataRenderable {
        @Override
        public Set<MIMEType> getSupportedRenderTypes() {
            return new LinkedHashSet<>(Arrays.asList(MIMEType.parse("text/*"), MIMEType.APPLICATION_JAVASCRIPT));
        }

        @Override
        public void render(RenderContext context) {
            context.renderIfRequested(MIMEType.TEXT_HTML, () -> "<g></g>");
            context.renderIfRequested(MIMEType.TEXT_CSS, () -> ".g{}");
            context.renderIfRequested(MIMEType.TEXT_LATEX, () -> "\\g");
            context.renderIfRequested(MIMEType.APPLICATION_JAVASCRIPT, () -> "g();");
        }

        @Override
        public String toString() {
            return "G";
        }
    }

    class H {
        public String html() {
            return "<h></h>";
        }

        public String css() {
            return ".h{}";
        }

        public String js() {
            return "h();";
        }

        @Override
        public String toString() {
            return "H";
        }
    }

    class I implements DisplayDataRenderable {
        @Override
        public Set<MIMEType> getSupportedRenderTypes() {
            return new LinkedHashSet<>(Arrays.asList(MIMEType.TEXT_PLAIN, MIMEType.APPLICATION_JAVASCRIPT));
        }

        @Override
        public void render(RenderContext context) {
            context.renderIfRequested(MIMEType.APPLICATION_JAVASCRIPT, () -> "i();");
            context.renderIfRequested(MIMEType.TEXT_PLAIN, () -> "I!");
        }

        @Override
        public String toString() {
            return "I";
        }
    }

    class J {
        public String js() {
            return "j();";
        }

        public String pretty() {
            return "J!";
        }

        @Override
        public String toString() {
            return "J";
        }
    }

    @Test
    public void rendersPlainText() {
        DisplayData data = this.renderer.render(new A());

        assertEquals("A", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void alwaysRendersPlainText() {
        DisplayData data = this.renderer.render(new B());

        assertEquals("B", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void rendersPreferred() {
        DisplayData data = this.renderer.render(new B());

        assertEquals("**B**", data.getData(MIMEType.TEXT_MARKDOWN));
    }

    @Test
    public void rendersJustPreferred() {
        DisplayData data = this.renderer.render(new C());

        assertEquals(".c{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("C", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_MARKDOWN));
    }

    @Test
    public void rendersExternal() {
        DisplayData data = this.renderer.render(new D());

        assertEquals("<d></d>", data.getData(MIMEType.TEXT_HTML));
        assertEquals("D", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_LATEX));
    }

    @Test
    public void rendersAs() {
        DisplayData data = this.renderer.renderAs(new C(), "text/markdown");

        assertEquals("**C**", data.getData(MIMEType.TEXT_MARKDOWN));
        assertEquals("C", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_CSS));
    }

    @Test
    public void rendersAsExternal() {
        DisplayData data = this.renderer.renderAs(new D(), "text/latex");

        assertEquals("\\d", data.getData(MIMEType.TEXT_LATEX));
        assertEquals("D", data.getData(MIMEType.TEXT_PLAIN));
        assertNull(data.getData(MIMEType.TEXT_HTML));
    }

    @Test
    public void supportsPreferringAll() {
        DisplayData data = this.renderer.render(new E());

        assertEquals("<e></e>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".e{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("e();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("E", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllExternal() {
        DisplayData data = this.renderer.render(new F());

        assertEquals("<f></f>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".f{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("f();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("F", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllRequestingAll() {
        DisplayData data = this.renderer.renderAs(new E(), "*");

        assertEquals("<e></e>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".e{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("e();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("E", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllRequestingAllExternal() {
        DisplayData data = this.renderer.renderAs(new F(), "*");

        assertEquals("<f></f>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".f{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("f();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("F", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllRequestingSome() {
        DisplayData data = this.renderer.renderAs(new E(), "text/html");

        assertEquals("<e></e>", data.getData(MIMEType.TEXT_HTML));
        assertNull(data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("E", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllRequestingSomeExternal() {
        DisplayData data = this.renderer.renderAs(new F(), "text/html");

        assertEquals("<f></f>", data.getData(MIMEType.TEXT_HTML));
        assertNull(data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("F", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllRequestingGroup() {
        DisplayData data = this.renderer.renderAs(new E(), "text/*");

        assertEquals("<e></e>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".e{}", data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("E", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringAllRequestingGroupExternal() {
        DisplayData data = this.renderer.renderAs(new F(), "text/*");

        assertEquals("<f></f>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".f{}", data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("F", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringGroup() {
        DisplayData data = this.renderer.render(new G());

        assertEquals("<g></g>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".g{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("g();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("G", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringGroupExternal() {
        DisplayData data = this.renderer.render(new H());

        assertEquals("<h></h>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".h{}", data.getData(MIMEType.TEXT_CSS));
        assertEquals("h();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("H", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringGroupRequestingSome() {
        DisplayData data = this.renderer.renderAs(new G(), "text/html");

        assertEquals("<g></g>", data.getData(MIMEType.TEXT_HTML));
        assertNull(data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("G", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringGroupRequestingSomeExternal() {
        DisplayData data = this.renderer.renderAs(new H(), "text/html");

        assertEquals("<h></h>", data.getData(MIMEType.TEXT_HTML));
        assertNull(data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("H", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringGroupRequestingGroup() {
        DisplayData data = this.renderer.renderAs(new G(), "text/*");

        assertEquals("<g></g>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".g{}", data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("G", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsPreferringGroupRequestingGroupExternal() {
        DisplayData data = this.renderer.renderAs(new H(), "text/*");

        assertEquals("<h></h>", data.getData(MIMEType.TEXT_HTML));
        assertEquals(".h{}", data.getData(MIMEType.TEXT_CSS));
        assertNull(data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("H", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsOverridingTextRepresentation() {
        DisplayData data = this.renderer.render(new I());

        assertEquals("I!", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsOverridingTextRepresentationExternal() {
        DisplayData data = this.renderer.render(new J());

        assertEquals("J!", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsOverridingTextRepresentationWhenNotRequested() {
        DisplayData data = this.renderer.renderAs(new I(), "application/javascript");

        assertEquals("i();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("I!", data.getData(MIMEType.TEXT_PLAIN));
    }

    @Test
    public void supportsOverridingTextRepresentationWhenNotRequestedExternal() {
        DisplayData data = this.renderer.renderAs(new J(), "application/javascript");

        assertEquals("j();", data.getData(MIMEType.APPLICATION_JAVASCRIPT));
        assertEquals("J!", data.getData(MIMEType.TEXT_PLAIN));
    }
}