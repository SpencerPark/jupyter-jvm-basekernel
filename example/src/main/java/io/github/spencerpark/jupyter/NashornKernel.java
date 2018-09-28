package io.github.spencerpark.jupyter;

import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.ReplacementOptions;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.kernel.util.CharPredicate;
import io.github.spencerpark.jupyter.kernel.util.SimpleAutoCompleter;
import io.github.spencerpark.jupyter.kernel.util.StringSearch;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class NashornKernel extends BaseKernel {
    private static final NashornScriptEngineFactory NASHORN_ENGINE_FACTORY = new NashornScriptEngineFactory();

    private static final SimpleAutoCompleter autoCompleter = SimpleAutoCompleter.builder()
            .preferLong()
            //Keywords from a great poem at https://stackoverflow.com/a/12114140
            .withKeywords("let", "this", "long", "package", "float")
            .withKeywords("goto", "private", "class", "if", "short")
            .withKeywords("while", "protected", "with", "debugger", "case")
            .withKeywords("continue", "volatile", "interface")
            .withKeywords("instanceof", "super", "synchronized", "throw")
            .withKeywords("extends", "final", "export", "throws")
            .withKeywords("try", "import", "double", "enum")
            .withKeywords("false", "boolean", "abstract", "function")
            .withKeywords("implements", "typeof", "transient", "break")
            .withKeywords("void", "static", "default", "do")
            .withKeywords("switch", "int", "native", "new")
            .withKeywords("else", "delete", "null", "public", "var")
            .withKeywords("in", "return", "for", "const", "true", "char")
            .withKeywords("finally", "catch", "byte")
            .build();

    private static final CharPredicate idChar = CharPredicate.builder()
            .inRange('a', 'z')
            .inRange('A', 'Z')
            .match('_')
            .build();

    private final ScriptEngine engine;
    private final LanguageInfo languageInfo;

    public NashornKernel() {
        this("-scripting");
    }

    public NashornKernel(String... args) {
        this(NASHORN_ENGINE_FACTORY.getScriptEngine(args));
    }

    public NashornKernel(ScriptEngine engine) {
        this.engine = engine;
        this.languageInfo = new LanguageInfo.Builder(engine.getFactory().getLanguageName())
                .version(engine.getFactory().getLanguageVersion())
                .mimetype("text/javascript")
                .fileExtension(".js")
                .pygments("javascript")
                .codemirror("javascript")
                .build();
    }

    @Override
    public LanguageInfo getLanguageInfo() {
        return languageInfo;
    }

    @Override
    public DisplayData eval(String expr) throws Exception {
        ScriptContext ctx = engine.getContext();

        //Redirect the streams
        ctx.setWriter(new OutputStreamWriter(System.out));
        ctx.setErrorWriter(new OutputStreamWriter(System.err));
        ctx.setReader(new InputStreamReader(System.in));

        //Evaluate the code
        Object res = engine.eval(expr, ctx);

        //If the evaluation returns a non-null value (the code is an expression like
        // 'a + b') then the return value should be this result as text. Otherwise
        //return null for nothing to be emitted for 'Out[n]'. Side effects may have
        //still printed something
        return res != null ? new DisplayData(res.toString()) : null;
    }

    @Override
    public DisplayData inspect(String code, int at, boolean extraDetail) throws Exception {
        StringSearch.Range match = StringSearch.findLongestMatchingAt(code, at, idChar);
        String id = "";
        Object val = null;
        if (match != null) {
            id = match.extractSubString(code);
            val = this.engine.getContext().getAttribute(id);
        }

        return new DisplayData(val == null ? "No memory value for '" + id + "'" : val.toString());
    }

    @Override
    public ReplacementOptions complete(String code, int at) throws Exception {
        StringSearch.Range match = StringSearch.findLongestMatchingAt(code, at, idChar);
        if (match == null)
            return null;
        String prefix = match.extractSubString(code);
        return new ReplacementOptions(autoCompleter.autocomplete(prefix), match.getLow(), match.getHigh());
    }
}
