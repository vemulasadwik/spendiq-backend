package com.spendiq.controller;

import com.spendiq.dto.request.GroupSplitRequest;
import com.spendiq.dto.response.ApiResponse;
import com.spendiq.entity.User;
import com.spendiq.service.GroupSplitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/splits")
public class GroupSplitController {

    private final GroupSplitService groupSplitService;

    @Autowired
    public GroupSplitController(GroupSplitService groupSplitService) {
        this.groupSplitService = groupSplitService;
    }

    private String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    @GetMapping
    public ResponseEntity<?> getMySplits(@AuthenticationPrincipal User user, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(groupSplitService.getMySplits(user, getBaseUrl(req))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(groupSplitService.getById(user, id, getBaseUrl(req))));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GroupSplitRequest request,
            HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Split created",
                groupSplitService.create(user, request, getBaseUrl(req))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        groupSplitService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.ok("Split deleted", null));
    }

    @PatchMapping("/{id}/pay/{userId}")
    public ResponseEntity<?> markPaid(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @PathVariable Long userId,
            HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Marked as paid",
                groupSplitService.markPaid(user, id, userId, getBaseUrl(req))));
    }

    @PatchMapping("/{id}/unpay/{userId}")
    public ResponseEntity<?> unmarkPaid(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @PathVariable Long userId,
            HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Payment undone",
                groupSplitService.unmarkPaid(user, id, userId, getBaseUrl(req))));
    }

    @PostMapping("/{id}/qr")
    public ResponseEntity<?> uploadQr(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest req) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("QR uploaded",
                groupSplitService.uploadQr(user, id, file, getBaseUrl(req))));
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> getQr(@PathVariable Long id) throws IOException {
        byte[] image = groupSplitService.getQrImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}