package io.github.spencerpark.jupyter.kernel.magic.core;

import io.github.spencerpark.jupyter.kernel.magic.registry.CellMagic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public class WriteFile {

    @CellMagic
    public static Void writefile(List<String> args, String body) throws Exception {
        if (args.isEmpty())
            throw new IllegalArgumentException("WriteFile magic expects a file name as an argument but one was not given");

        String fileName = args.get(0);

        File file = new File(fileName);

        if (file.isDirectory())
            throw new FileAlreadyExistsException("Cannot write to file " + fileName + ". It is a directory.");

        file.createNewFile();

        try (OutputStreamWriter fileOut = new OutputStreamWriter(new FileOutputStream(file))) {
            fileOut.write(body);
        }

        return null;
    }
}
