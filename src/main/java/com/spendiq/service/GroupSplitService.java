package com.spendiq.service;

import com.spendiq.dto.request.GroupSplitRequest;
import com.spendiq.dto.response.GroupSplitResponse;
import com.spendiq.entity.*;
import com.spendiq.exception.BadRequestException;
import com.spendiq.exception.ResourceNotFoundException;
import com.spendiq.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupSplitService {

    private final GroupSplitRepository groupSplitRepository;
    private final SplitOweRepository splitOweRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public GroupSplitService(GroupSplitRepository groupSplitRepository,
                              SplitOweRepository splitOweRepository,
                              UserRepository userRepository,
                              NotificationRepository notificationRepository) {
        this.groupSplitRepository = groupSplitRepository;
        this.splitOweRepository = splitOweRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<GroupSplitResponse> getMySplits(User user, String baseUrl) {
        return groupSplitRepository.findAllByMemberId(user.getId())
                .stream().map(g -> GroupSplitResponse.from(g, baseUrl)).collect(Collectors.toList());
    }

    public GroupSplitResponse getById(User user, Long splitId, String baseUrl) {
        GroupSplit split = groupSplitRepository.findByIdWithOwes(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));
        boolean isMember = split.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) throw new BadRequestException("You are not a member of this split");
        return GroupSplitResponse.from(split, baseUrl);
    }

    @Transactional
    public GroupSplitResponse create(User currentUser, GroupSplitRequest request, String baseUrl) {
        if (!request.getMemberUserIds().contains(request.getPaidByUserId())) {
            throw new BadRequestException("Paid-by user must be in the members list");
        }

        List<User> memberList = userRepository.findAllById(request.getMemberUserIds());
        if (memberList.size() != request.getMemberUserIds().size()) {
            throw new BadRequestException("One or more member user IDs are invalid");
        }

        Set<User> members = new HashSet<>(memberList);

        User paidBy = members.stream()
                .filter(u -> u.getId().equals(request.getPaidByUserId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Paid-by user not found"));

        BigDecimal perHead = request.getTotalAmount()
                .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        GroupSplit split = GroupSplit.builder()
                .title(request.getTitle())
                .totalAmount(request.getTotalAmount())
                .perHead(perHead)
                .date(request.getDate())
                .note(request.getNote())
                .paidBy(paidBy)
                .members(members)
                .build();

        split = groupSplitRepository.save(split);
        final Long splitId = split.getId();

        for (User member : members) {
            if (!member.getId().equals(paidBy.getId())) {
                SplitOwe owe = SplitOwe.builder()
                        .groupSplit(split)
                        .user(member)
                        .amount(perHead)
                        .build();
                splitOweRepository.save(owe);

                Notification notif = Notification.builder()
                        .user(member)
                        .groupSplit(split)
                        .message(paidBy.getName() + " paid for \"" + request.getTitle()
                                + "\". You owe " + perHead)
                        .build();
                notificationRepository.save(notif);
            }
        }

        splitOweRepository.flush();
        notificationRepository.flush();
        groupSplitRepository.flush();

        List<SplitOwe> owes = splitOweRepository.findByGroupSplitId(splitId);
        GroupSplit result = groupSplitRepository.findByIdWithOwes(splitId).orElseThrow();
        result.getOwes().clear();
        result.getOwes().addAll(owes);
        return GroupSplitResponse.from(result, baseUrl);
    }

    @Transactional
    public void delete(User user, Long splitId) {
        GroupSplit split = groupSplitRepository.findById(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));
        // Only the person who paid (split creator) can delete
        if (!split.getPaidBy().getId().equals(user.getId()))
            throw new BadRequestException("Only the person who paid can delete this split");
        // Delete in correct FK order using native SQL with correct column names
        notificationRepository.deleteNotificationsBySplitId(splitId);
        splitOweRepository.deleteOwesBySplitId(splitId);
        groupSplitRepository.deleteMembersBySplitId(splitId);
        groupSplitRepository.deleteByIdNative(splitId);
    }

    @Transactional
    public GroupSplitResponse markPaid(User user, Long splitId, Long oweUserId, String baseUrl) {
        GroupSplit split = groupSplitRepository.findByIdWithOwes(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));
        boolean isMember = split.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) throw new BadRequestException("You are not a member of this split");

        SplitOwe owe = splitOweRepository.findByGroupSplitIdAndUserId(splitId, oweUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No owe record found"));
        owe.markPaid();
        splitOweRepository.save(owe);
        splitOweRepository.flush();

        notificationRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(oweUserId)
                .stream().filter(n -> n.getGroupSplit().getId().equals(splitId))
                .forEach(n -> { n.dismiss(); notificationRepository.save(n); });

        List<SplitOwe> owes = splitOweRepository.findByGroupSplitId(splitId);
        GroupSplit result = groupSplitRepository.findByIdWithOwes(splitId).orElseThrow();
        result.getOwes().clear();
        result.getOwes().addAll(owes);
        return GroupSplitResponse.from(result, baseUrl);
    }

    @Transactional
    public GroupSplitResponse unmarkPaid(User user, Long splitId, Long oweUserId, String baseUrl) {
        GroupSplit split = groupSplitRepository.findByIdWithOwes(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));
        boolean isMember = split.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) throw new BadRequestException("You are not a member of this split");

        SplitOwe owe = splitOweRepository.findByGroupSplitIdAndUserId(splitId, oweUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No owe record found"));
        owe.unmarkPaid();
        splitOweRepository.save(owe);
        splitOweRepository.flush();

        List<SplitOwe> owes = splitOweRepository.findByGroupSplitId(splitId);
        GroupSplit result = groupSplitRepository.findByIdWithOwes(splitId).orElseThrow();
        result.getOwes().clear();
        result.getOwes().addAll(owes);
        return GroupSplitResponse.from(result, baseUrl);
    }

    // ✅ QR stored as base64 in DB — persists across server restarts, visible to all users
    @Transactional
    public GroupSplitResponse uploadQr(User user, Long splitId, MultipartFile file, String baseUrl) throws IOException {
        GroupSplit split = groupSplitRepository.findByIdWithOwes(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));

        if (!split.getPaidBy().getId().equals(user.getId())) {
            throw new BadRequestException("Only the person who paid can upload the QR code");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        // Convert to base64 data URL and store directly in DB
        byte[] bytes = file.getBytes();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String mediaType = contentType != null ? contentType : "image/png";
        String dataUrl = "data:" + mediaType + ";base64," + base64;

        split.setQrImagePath(dataUrl);
        groupSplitRepository.save(split);

        List<SplitOwe> owes = splitOweRepository.findByGroupSplitId(splitId);
        GroupSplit result = groupSplitRepository.findByIdWithOwes(splitId).orElseThrow();
        result.getOwes().clear();
        result.getOwes().addAll(owes);
        return GroupSplitResponse.from(result, baseUrl);
    }

    // Returns base64 data URL stored in DB
    public String getQrDataUrl(Long splitId) {
        GroupSplit split = groupSplitRepository.findById(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));
        if (split.getQrImagePath() == null) {
            throw new ResourceNotFoundException("No QR code uploaded for this split");
        }
        return split.getQrImagePath();
    }
}
