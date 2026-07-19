package com.digitalservicing.types;

import java.time.Instant;

public record ProfileCreatedEvent(String eventType, Long userId, String profileUrl, Instant occurredAt) {
}
