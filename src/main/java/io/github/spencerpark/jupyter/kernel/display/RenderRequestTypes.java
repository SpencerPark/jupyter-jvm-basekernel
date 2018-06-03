package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class RenderRequestTypes {
    public static class Builder {
        private final MIMESuffixAssociation suffixAssociation;

        private boolean requestsWildcard;
        private final Set<String> entireGroupRequests;
        private final Set<MIMEType> requestedTypes;

        public Builder(MIMESuffixAssociation suffixAssociation) {
            this.suffixAssociation = suffixAssociation;

            this.requestsWildcard = false;
            this.entireGroupRequests = new LinkedHashSet<>();
            this.requestedTypes = new LinkedHashSet<>();
        }

        public Builder withType(String type) {
            MIMEType mimeType = MIMEType.parse(type);
            return this.withType(mimeType);
        }

        public Builder withType(MIMEType type) {
            if (type.isWildcard())
                this.requestsWildcard = true;
            else if (!type.hasSubtype() || type.subtypeIsWildcard())
                this.entireGroupRequests.add(type.getGroup());
            else
                this.requestedTypes.add(type);
            return this;
        }

        public RenderRequestTypes build() {
            return new RenderRequestTypes(
                    this.suffixAssociation,
                    this.requestsWildcard,
                    this.requestsWildcard || this.entireGroupRequests.isEmpty()
                            ? Collections.EMPTY_SET
                            : this.entireGroupRequests,
                    this.requestsWildcard || this.requestedTypes.isEmpty()
                            ? Collections.EMPTY_SET
                            : this.requestedTypes
            );
        }
    }

    private final MIMESuffixAssociation suffixAssociation;

    private final boolean requestsWildcard;
    private final Set<String> entireGroupRequests;
    private final Set<MIMEType> requestedTypes;

    public RenderRequestTypes(MIMESuffixAssociation suffixAssociation, boolean requestsWildcard, Set<String> entireGroupRequests, Set<MIMEType> requestedTypes) {
        this.suffixAssociation = suffixAssociation;
        this.requestsWildcard = requestsWildcard;
        this.entireGroupRequests = entireGroupRequests;
        this.requestedTypes = requestedTypes;
    }

    // Store the last resolution as when used manually many of the calls will require the
    // same information.
    private MIMEType cachedSupportedType;
    private MIMEType cachedResolvedType;

    private MIMEType checkResolutionCache(MIMEType supportedType) {
        if (Objects.equals(this.cachedSupportedType, supportedType))
            return this.cachedResolvedType;
        return null;
    }

    private void invalidateCache() {
        this.cachedSupportedType = null;
        this.cachedResolvedType = null;
    }

    private MIMEType cache(MIMEType supported, MIMEType resolved) {
        this.cachedSupportedType = supported;
        this.cachedResolvedType = resolved;
        return resolved;
    }

    public MIMEType resolveSupportedType(MIMEType supportedType) {
        if (supportedType.isWildcard() || supportedType.subtypeIsWildcard())
            throw new IllegalArgumentException("Cannot resolve type of wildcard MIME type: '" + supportedType.toString() + "'");

        // Everything is supported
        if (this.requestsWildcard)
            return supportedType;

        // Check cache
        MIMEType cached = this.checkResolutionCache(supportedType);
        if (cached != null)
            return cached;

        // If the exact type is supported or the group is supported then the exact type
        // is supported.
        if (this.requestedTypes.contains(supportedType)
                || this.entireGroupRequests.contains(supportedType.getGroup()))
            return supportedType;

        if (supportedType.hasSuffix()) {
            // If dropping the supported type without the suffix is supported then that
            // is compatible and is the resolved type.
            MIMEType withoutSuffix = supportedType.withoutSuffix();
            if (this.requestedTypes.contains(withoutSuffix))
                return cache(supportedType, withoutSuffix);

            // If the type association of the suffix is supported then use the association.
            MIMEType suffixDelegate = this.suffixAssociation.resolveSuffix(supportedType.getSuffix());
            if (suffixDelegate != null && (
                    this.requestedTypes.contains(suffixDelegate)
                            || this.entireGroupRequests.contains(suffixDelegate.getGroup())))
                return cache(supportedType, suffixDelegate);
        }

        // The type is not supported
        return null;
    }

    public boolean removeRequestedType(MIMEType type) {
        MIMEType resolved = this.resolveSupportedType(type);
        boolean removed = this.requestedTypes.remove(resolved);
        if (removed) this.invalidateCache();
        return removed;
    }

    public void removeFulfilledRequests(DisplayData out) {
        this.requestedTypes.forEach(type -> {
            MIMEType resolved = this.resolveSupportedType(type);
            if (out.hasDataForType(resolved))
                this.removeRequestedType(type);
        });
    }

    public boolean anyIsRequested(Set<MIMEType> supported) {
        for (MIMEType t : supported) {
            if (this.resolveSupportedType(t) != null)
                return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return !this.requestsWildcard
                && this.entireGroupRequests.isEmpty()
                && this.requestedTypes.isEmpty();
    }
}
