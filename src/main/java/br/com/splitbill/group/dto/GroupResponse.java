package br.com.splitbill.group.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
    UUID publicId,
    String name,
    String description,
    String createdByName,
    LocalDateTime createdAt
) {}
