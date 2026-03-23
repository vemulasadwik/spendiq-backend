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

    private String getBaseUrl(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() +
               (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort());
    }

    // GET /api/splits
    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal User user, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Splits fetched",
                groupSplitService.getMySplits(user, getBaseUrl(req))));
    }

    // GET /api/splits/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@AuthenticationPrincipal User user,
                                      @PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Split fetched",
                groupSplitService.getById(user, id, getBaseUrl(req))));
    }

    // POST /api/splits
    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user,
                                     @Valid @RequestBody GroupSplitRequest request,
                                     HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Split created",
                groupSplitService.create(user, request, getBaseUrl(req))));
    }

    // DELETE /api/splits/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        groupSplitService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.ok("Split deleted", null));
    }

    // PATCH /api/splits/{id}/pay/{userId}
    @PatchMapping("/{id}/pay/{userId}")
    public ResponseEntity<?> markPaid(@AuthenticationPrincipal User user,
                                       @PathVariable Long id,
                                       @PathVariable Long userId,
                                       HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Marked as paid",
                groupSplitService.markPaid(user, id, userId, getBaseUrl(req))));
    }

    // PATCH /api/splits/{id}/unpay/{userId}
    @PatchMapping("/{id}/unpay/{userId}")
    public ResponseEntity<?> unmarkPaid(@AuthenticationPrincipal User user,
                                         @PathVariable Long id,
                                         @PathVariable Long userId,
                                         HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Unmarked as paid",
                groupSplitService.unmarkPaid(user, id, userId, getBaseUrl(req))));
    }

    // POST /api/splits/{id}/qr  — upload QR (only split owner)
    @PostMapping("/{id}/qr")
    public ResponseEntity<?> uploadQr(@AuthenticationPrincipal User user,
                                       @PathVariable Long id,
                                       @RequestParam("file") MultipartFile file,
                                       HttpServletRequest req) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("QR uploaded",
                groupSplitService.uploadQr(user, id, file, getBaseUrl(req))));
    }

    // GET /api/splits/{id}/qr  — get QR (visible to all members)
    @GetMapping("/{id}/qr")
    public ResponseEntity<?> getQr(@PathVariable Long id) {
        String dataUrl = groupSplitService.getQrDataUrl(id);
        return ResponseEntity.ok(ApiResponse.ok("QR fetched", dataUrl));
    }
}
