package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.api.LanguageInfo;

import java.util.List;

public class KernelInfo {
    /**
     * Semantic version string. X.Y.Z
     */
    protected String protocolVersion;

    /**
     * Ex. 'ipython' for IPython
     */
    protected String implementationName;

    /**
     * Semantic version string for the kernel
     */
    protected String implementationVersion;

    protected LanguageInfo langInfo;

    /**
     * An optional banner text about the kernel.
     */
    protected String banner;

    /**
     * Optional help links about the kernel language
     */
    protected List<LanguageInfo.Help> helpLinks;

    public KernelInfo(String protocolVersion, String implementationName, String implementationVersion, LanguageInfo langInfo, String banner, List<LanguageInfo.Help> helpLinks) {
        this.protocolVersion = protocolVersion;
        this.implementationName = implementationName;
        this.implementationVersion = implementationVersion;
        this.langInfo = langInfo;
        this.banner = banner;
        this.helpLinks = helpLinks;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getImplementationName() {
        return implementationName;
    }

    public String getImplementationVersion() {
        return implementationVersion;
    }

    public LanguageInfo getLangInfo() {
        return langInfo;
    }

    public String getBanner() {
        return banner;
    }

    public List<LanguageInfo.Help> getHelpLinks() {
        return helpLinks;
    }
}
