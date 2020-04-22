package io.github.spencerpark.jupyter.client.system;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class KernelSpec {
    public static final String KERNEL_SPEC_FILE_NAME = "kernel.json";
    private static final Gson GSON = new Gson();

    public enum InterruptMode {
        @SerializedName("signal") SIGNAL,
        @SerializedName("message") MESSAGE
    }

    public static class Builder {
        private List<String> argv = new LinkedList<>();
        private String displayName;
        private String language;
        private InterruptMode interruptMode = InterruptMode.MESSAGE;
        private Map<String, String> env = new LinkedHashMap<>();
        private Map<String, Object> metadata = new LinkedHashMap<>();
        private Path resourceDir;

        public Builder withArgv(String... argv) {
            return this.withArgv(Arrays.asList(argv));
        }

        public Builder withArgv(List<String> argv) {
            this.argv = argv;
            return this;
        }

        public Builder addArgv(String... argv) {
            Collections.addAll(this.argv, argv);
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder withInterruptMode(InterruptMode interruptMode) {
            this.interruptMode = interruptMode;
            return this;
        }

        public Builder withEnv(Map<String, String> env) {
            this.env = env;
            return this;
        }

        public Builder addEnvVar(String var, String value) {
            this.env.put(var, value);
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder addMetadata(String var, Object value) {
            this.metadata.put(var, value);
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder addMetadataValueAtPath(Object value, String... path) {
            Map<String, Object> ns = this.metadata;

            // Add default namespaces up to the last entry
            for (int i = 0; i < path.length - 1; i++) {
                String seg = path[i];
                int subPathLen = i + 1;
                ns = (Map<String, Object>) ns.compute(seg, (key, current) -> {
                    if (current == null)
                        return new LinkedHashMap<>();
                    else if (!(current instanceof Map))
                        throw new IllegalStateException("Metadata already has a value at "
                                + Arrays.stream(path, 0, subPathLen).collect(Collectors.joining("."))
                                + " and it is not a json object.");
                    return current;
                });
            }

            // Put the value in the last namespace at the name of the last segment in the path.
            this.metadata.put(path[path.length - 1], value);

            return this;
        }

        public Builder withResourceDirectory(Path resourceDir) {
            this.resourceDir = resourceDir;
            return this;
        }

        public KernelSpec build() {
            return new KernelSpec(
                    this.argv.isEmpty()
                            ? Collections.emptyList()
                            : this.argv.size() == 1
                                    ? Collections.singletonList(this.argv.get(0))
                                    : Collections.unmodifiableList(this.argv),
                    this.displayName,
                    this.language,
                    this.interruptMode,
                    this.env.isEmpty()
                            ? Collections.emptyMap()
                            : Collections.unmodifiableMap(this.env),
                    this.metadata.isEmpty()
                            ? Collections.emptyMap()
                            : Collections.unmodifiableMap(this.metadata),
                    this.resourceDir == null
                            ? Paths.get("").toAbsolutePath()
                            : this.resourceDir
            );
        }
    }

    public static KernelSpec fromResourceDirectory(Path dir) throws IOException {
        Path kernelSpecFile;
        if (dir.endsWith(KERNEL_SPEC_FILE_NAME)) {
            kernelSpecFile = dir;
        } else {
            dir = dir.toAbsolutePath();
            if (!Files.isDirectory(dir))
                throw new IllegalArgumentException("Path '" + dir + "' is not a directory.");

            kernelSpecFile = dir.resolve(KERNEL_SPEC_FILE_NAME);
            if (!Files.exists(kernelSpecFile))
                throw new IllegalArgumentException("No " + KERNEL_SPEC_FILE_NAME + " file in '" + dir + "'.");
        }

        KernelSpec spec = GSON.fromJson(Files.newBufferedReader(kernelSpecFile), KernelSpec.class);
        if (spec.argv == null)
            throw new IllegalArgumentException("'" + kernelSpecFile + "' is missing the required field 'argv'.");
        if (spec.displayName == null)
            throw new IllegalArgumentException("'" + kernelSpecFile + "' is missing the required field 'display_name'.");
        if (spec.language == null)
            throw new IllegalArgumentException("'" + kernelSpecFile + "' is missing the required field 'language'.");

        spec.resourceDir = dir;

        return spec;
    }

    private final List<String> argv;

    @SerializedName("display_name")
    private final String displayName;

    private final String language;

    @SerializedName("interrput_mode")
    private final InterruptMode interruptMode;

    private final Map<String, String> env;

    // These values are deserialized as Map, List, Double, String, Boolean, or null
    private final Map<String, Object> metadata;

    private transient Path resourceDir;

    // No args cons for gson
    private KernelSpec() {
        this(null, null, null, null);
    }

    public KernelSpec(List<String> argv, String displayName, String language, Path resourceDir) {
        this(argv, displayName, language, InterruptMode.SIGNAL, Collections.emptyMap(), Collections.emptyMap(), resourceDir);
    }

    public KernelSpec(List<String> argv, String displayName, String language, InterruptMode interruptMode, Map<String, String> env, Map<String, Object> metadata, Path resourceDir) {
        this.argv = argv;
        this.displayName = displayName;
        this.language = language;
        this.interruptMode = interruptMode;
        this.env = env;
        this.metadata = metadata;
        this.resourceDir = resourceDir;
    }

    public List<String> getArgv() {
        return argv;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLanguage() {
        return language;
    }

    public InterruptMode getInterruptMode() {
        return interruptMode;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Path getResourceDirectory() {
        return resourceDir;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return "KernelSpec {\n" + "\targv = " + argv + '\n' +
                "\tdisplayName = '" + displayName + "'\n" +
                "\tlanguage = '" + language + "'\n" +
                "\tinterruptMode = " + interruptMode + '\n' +
                "\tenv = " + env + '\n' +
                "\tmetadata = " + metadata + '\n' +
                "\tresourceDir = " + resourceDir + '\n' +
                "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KernelSpec that = (KernelSpec) o;
        return Objects.equals(argv, that.argv) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(language, that.language) &&
                interruptMode == that.interruptMode &&
                Objects.equals(env, that.env) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(resourceDir, that.resourceDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argv, displayName, language, interruptMode, env, metadata, resourceDir);
    }
}
