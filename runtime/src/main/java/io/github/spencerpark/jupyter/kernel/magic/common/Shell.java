package io.github.spencerpark.jupyter.kernel.magic.common;

import io.github.spencerpark.jupyter.kernel.magic.registry.LineMagic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Shell {
    @LineMagic
    public static List<String> sh(List<String> args) throws Exception {
        Process p = new ProcessBuilder()
                .command(args)
                .start();

        List<String> output = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null)
            output.add(line);

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }

        return output;
    }
}
