package io.github.spencerpark.jupyter.client.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KernelSpecManager {
    private static final Pattern VALID_KERNEL_NAME_PATTERN = Pattern.compile("^[-a-zA-Z0-9._]+$");

    private static boolean isKernelDir(Path path) {
        return Files.isRegularFile(path.resolve(KernelSpec.KERNEL_SPEC_FILE_NAME));
    }

    private static boolean isValidKernelName(String name) {
        return VALID_KERNEL_NAME_PATTERN.matcher(name).matches();
    }

    private static boolean resourceDirHasValidKernelName(Path dir) {
        return KernelSpecManager.isValidKernelName(dir.getFileName().toString());
    }

    public static KernelSpecManager fromPaths(JupyterPaths paths) {
        return new KernelSpecManager(
                paths.getDataDirs().stream()
                        .map(p -> p.resolve("kernels"))
                        .collect(Collectors.toList()));
    }

    private final List<Path> kernelDirs;

    public KernelSpecManager(List<Path> kernelDirs) {
        this.kernelDirs = kernelDirs;
    }

    private void forEachKernelDir(Consumer<Path> consumer) throws IOException {
        for (Path dir : this.kernelDirs) {
            if (!Files.isDirectory(dir))
                continue;

            try (Stream<Path> kernelDirs = Files.list(dir)) {
                kernelDirs
                        .filter(Files::isDirectory)
                        .filter(KernelSpecManager::isKernelDir)
                        .filter(KernelSpecManager::resourceDirHasValidKernelName)
                        .forEach(consumer);
            }
        }
    }

    private Optional<Path> findKernelDir(Predicate<Path> filter) throws IOException {
        for (Path dir : this.kernelDirs) {
            if (!Files.isDirectory(dir))
                continue;

            try (Stream<Path> kernelDirs = Files.list(dir)) {
                Optional<Path> matching = kernelDirs
                        .filter(Files::isDirectory)
                        .filter(KernelSpecManager::isKernelDir)
                        .filter(KernelSpecManager::resourceDirHasValidKernelName)
                        .filter(filter)
                        .findFirst();

                if (matching.isPresent())
                    return matching;
            }
        }

        return Optional.empty();
    }

    public Map<String, Path> findKernelSpecs() throws IOException {
        Map<String, Path> resourceLocs = new LinkedHashMap<>();

        this.forEachKernelDir(p ->
                resourceLocs.putIfAbsent(p.getFileName().toString().toLowerCase(), p));

        return resourceLocs;
    }

    public Map<String, List<Path>> findAllKernelSpecs() throws IOException {
        Map<String, List<Path>> resourceLocs = new LinkedHashMap<>();

        this.forEachKernelDir(p ->
                resourceLocs.compute(p.getFileName().toString().toLowerCase(), (name, locs) -> {
                    List<Path> paths = locs == null ? new LinkedList<>() : locs;
                    paths.add(p);
                    return paths;
                }));

        return resourceLocs;
    }

    public Optional<KernelSpec> getKernelSpec(String name) throws IOException {
        if (!KernelSpecManager.isValidKernelName(name))
            return Optional.empty();

        Optional<Path> dir = this.findKernelDir(p -> p.getFileName().toString().equalsIgnoreCase(name));
        if (!dir.isPresent())
            return Optional.empty();

        return Optional.of(KernelSpec.fromResourceDirectory(dir.get()));
    }

    public Map<String, KernelSpec> getAllSpecs() throws IOException {
        Map<String, KernelSpec> specs = new LinkedHashMap<>();

        for (Map.Entry<String, Path> entry : this.findKernelSpecs().entrySet())
            specs.put(entry.getKey(), KernelSpec.fromResourceDirectory(entry.getValue()));

        return specs;
    }
}
