package io.github.spencerpark.jupyter.kernel.magic.common;

import io.github.spencerpark.jupyter.kernel.DisplayStream;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.kernel.display.Renderer;
import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;
import io.github.spencerpark.jupyter.kernel.magic.registry.CellMagic;
import io.github.spencerpark.jupyter.kernel.magic.registry.MagicsArgs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DisplayMagics {
    private static final MagicsArgs HTML_ARGS = MagicsArgs.builder()
            .keyword("isolated", MagicsArgs.KeywordSpec.ONCE)
            .onlyKnownFlags().onlyKnownKeywords()
            .build();

    private final Renderer renderer;
    private final DisplayStream out;

    public DisplayMagics(Renderer renderer, DisplayStream out) {
        this.renderer = renderer;
        this.out = out;
    }

    @CellMagic
    public void html(List<String> args, String body) {
        Map<String, List<String>> vals = HTML_ARGS.parse(args);
        boolean isolated = !vals.get("isolated").isEmpty();

        DisplayData data = this.renderer.renderAs(body, MIMEType.TEXT_HTML.toString());

        if (isolated) {
            Map<String, Boolean> meta = new LinkedHashMap<>();
            meta.put("isolated", true);
            data.putMetaData(MIMEType.TEXT_HTML, meta);
        }

        this.out.display(data);
    }

    @CellMagic
    public void markdown(List<String> args, String body) {
        this.out.display(
                this.renderer.renderAs(body, MIMEType.TEXT_MARKDOWN.toString())
        );
    }

    @CellMagic
    public void svg(List<String> args, String body) {
        this.out.display(
                this.renderer.renderAs(body, MIMEType.IMAGE_SVG.toString())
        );
    }

    @CellMagic
    public void latex(List<String> args, String body) {
        this.out.display(
                this.renderer.renderAs(body, MIMEType.TEXT_LATEX.toString())
        );
    }

    @CellMagic(aliases = "js")
    public void javascript(List<String> args, String body) {
        this.out.display(
                this.renderer.renderAs(body, MIMEType.APPLICATION_JAVASCRIPT.toString())
        );
    }
}
