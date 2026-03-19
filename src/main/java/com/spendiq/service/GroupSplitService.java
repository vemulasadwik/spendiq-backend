package com.spendiq.service;

import com.spendiq.dto.request.GroupSplitRequest;
import com.spendiq.dto.response.GroupSplitResponse;
import com.spendiq.entity.*;
import com.spendiq.exception.BadRequestException;
import com.spendiq.exception.ResourceNotFoundException;
import com.spendiq.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupSplitService {

    private final GroupSplitRepository groupSplitRepository;
    private final SplitOweRepository splitOweRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

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

        // Load owes directly and manually set them
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
        if (!split.getPaidBy().getId().equals(user.getId()))
            throw new BadRequestException("Only the person who paid can delete this split");
        // Use native SQL to delete children — bypasses Hibernate cascade entirely
        notificationRepository.deleteByGroupSplitIdNative(splitId);
        splitOweRepository.deleteByGroupSplitIdNative(splitId);
        groupSplitRepository.deleteMembersByGroupSplitId(splitId);
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

        Path uploadPath = Paths.get(uploadDir, "qr");
        Files.createDirectories(uploadPath);

        String ext = file.getOriginalFilename() != null
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                : ".png";
        String fileName = "qr_split_" + splitId + "_" + UUID.randomUUID() + ext;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        if (split.getQrImagePath() != null) {
            try { Files.deleteIfExists(Paths.get(split.getQrImagePath())); } catch (IOException ignored) {}
        }

        split.setQrImagePath(filePath.toString());
        groupSplitRepository.save(split);

        List<SplitOwe> owes = splitOweRepository.findByGroupSplitId(splitId);
        GroupSplit result = groupSplitRepository.findByIdWithOwes(splitId).orElseThrow();
        result.getOwes().clear();
        result.getOwes().addAll(owes);
        return GroupSplitResponse.from(result, baseUrl);
    }

    public byte[] getQrImage(Long splitId) throws IOException {
        GroupSplit split = groupSplitRepository.findById(splitId)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found"));
        if (split.getQrImagePath() == null) {
            throw new ResourceNotFoundException("No QR code uploaded");
        }
        return Files.readAllBytes(Paths.get(split.getQrImagePath()));
    }
}
