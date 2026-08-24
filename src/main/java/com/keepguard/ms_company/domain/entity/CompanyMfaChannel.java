package com.keepguard.ms_company.domain.entity;

import com.keepguard.ms_company.domain.enums.MfaChannelEnum;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class CompanyMfaChannel {

    private final UUID id;
    private final MfaChannelEnum channel;
    private boolean required;
    private boolean enabled;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CompanyMfaChannel(UUID id, MfaChannelEnum channel, boolean required, boolean enabled,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.channel = Objects.requireNonNull(channel, "Canal de MFA não pode ser nulo");
        this.required = required;
        this.enabled = enabled;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static CompanyMfaChannel create(MfaChannelEnum channel, boolean required, boolean enabled) {
        return new CompanyMfaChannel(null, channel, required, enabled, LocalDateTime.now(), LocalDateTime.now());
    }

    public static CompanyMfaChannel of(UUID id, MfaChannelEnum channel, boolean required, boolean enabled,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new CompanyMfaChannel(id, channel, required, enabled, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public MfaChannelEnum getChannel() { return channel; }
    public boolean isRequired() { return required; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(boolean required, boolean enabled) {
        this.required = required;
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
