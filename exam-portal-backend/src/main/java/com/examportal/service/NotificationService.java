package com.examportal.service;

import com.examportal.dto.response.NotificationResponse;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.model.Notification;
import com.examportal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ── All notifications for user ────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUser(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Unread notifications ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnread(Long userId) {
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Unread count ──────────────────────────────────────────────
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // ── Mark all read ─────────────────────────────────────────────
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
        log.info("All notifications marked read for user [{}]",
                userId);
    }

    // ── Mark single read ──────────────────────────────────────────
    @Transactional
    public void markRead(Long notificationId) {
        if (!notificationRepository.existsById(notificationId))
            throw new ResourceNotFoundException(
                    "Notification not found: " + notificationId);
        notificationRepository.markReadById(notificationId);
    }

    // ── Mapping ───────────────────────────────────────────────────
    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}