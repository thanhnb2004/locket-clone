package com.locket.clone.moment.controller;

import com.locket.clone.moment.dto.MomentResponse;
import com.locket.clone.moment.entity.Moment;
import com.locket.clone.moment.service.IMomentService;
import com.locket.clone.security.CurrentUser;
import com.locket.clone.storage.service.IStorageService;
import com.locket.clone.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moments")
public class MomentController {

    private final IMomentService momentService;
    private final IStorageService storageService;
    private final CurrentUser currentUser;

    public MomentController(IMomentService momentService,
                           IStorageService storageService,
                           CurrentUser currentUser) {
        this.momentService = momentService;
        this.storageService = storageService;
        this.currentUser = currentUser;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MomentResponse> create(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "caption", required = false) String caption) {
        Moment moment = momentService.create(currentUser.get(), image, caption);
        return ResponseEntity.status(HttpStatus.CREATED).body(MomentResponse.from(moment));
    }

    @GetMapping("/feed")
    public List<MomentResponse> feed() {
        return momentService.feed(currentUser.get()).stream()
                .map(MomentResponse::from)
                .toList();
    }

    @GetMapping("/mine")
    public List<MomentResponse> mine() {
        return momentService.ownMoments(currentUser.get()).stream()
                .map(MomentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MomentResponse get(@PathVariable UUID id) {
        return MomentResponse.from(momentService.getVisible(currentUser.get(), id));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable UUID id) {
        User me = currentUser.get();
        Moment moment = momentService.getVisible(me, id);
        byte[] data = storageService.load(moment.getImageFilename());
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(moment.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(data.length)
                .body(data);
    }
}
