package com.locket.clone.moment.controller;

import com.locket.clone.moment.dto.MomentResponse;
import com.locket.clone.moment.entity.Moment;
import com.locket.clone.moment.service.IMomentService;
import com.locket.clone.reaction.dto.ReactionRequest;
import com.locket.clone.reaction.entity.Reaction;
import com.locket.clone.reaction.service.IReactionService;
import com.locket.clone.security.CurrentUser;
import com.locket.clone.storage.service.IStorageService;
import com.locket.clone.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/moments")
public class MomentController {

    private final IMomentService momentService;
    private final IReactionService reactionService;
    private final IStorageService storageService;
    private final CurrentUser currentUser;

    public MomentController(IMomentService momentService,
                           IReactionService reactionService,
                           IStorageService storageService,
                           CurrentUser currentUser) {
        this.momentService = momentService;
        this.reactionService = reactionService;
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
        User me = currentUser.get();
        return withReactions(momentService.feed(me), me);
    }

    @GetMapping("/mine")
    public List<MomentResponse> mine() {
        User me = currentUser.get();
        return withReactions(momentService.ownMoments(me), me);
    }

    @GetMapping("/{id}")
    public MomentResponse get(@PathVariable UUID id) {
        User me = currentUser.get();
        Moment moment = momentService.getVisible(me, id);
        return MomentResponse.from(moment, reactionService.reactionsFor(id), me.getId());
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

    /** React to a moment (or change an existing reaction) with an emoji. */
    @PostMapping("/{id}/reactions")
    public MomentResponse react(@PathVariable UUID id, @Valid @RequestBody ReactionRequest request) {
        User me = currentUser.get();
        reactionService.react(me, id, request.emoji());
        return MomentResponse.from(momentService.getVisible(me, id),
                reactionService.reactionsFor(id), me.getId());
    }

    /** Remove the current user's reaction from a moment. */
    @DeleteMapping("/{id}/reactions")
    public MomentResponse unreact(@PathVariable UUID id) {
        User me = currentUser.get();
        reactionService.removeReaction(me, id);
        return MomentResponse.from(momentService.getVisible(me, id),
                reactionService.reactionsFor(id), me.getId());
    }

    /** Enriches a list of moments with their reactions in a single batched query. */
    private List<MomentResponse> withReactions(List<Moment> moments, User me) {
        List<UUID> ids = moments.stream().map(Moment::getId).toList();
        Map<UUID, List<Reaction>> byMoment = reactionService.reactionsFor(ids).stream()
                .collect(Collectors.groupingBy(r -> r.getMoment().getId()));
        return moments.stream()
                .map(m -> MomentResponse.from(m, byMoment.getOrDefault(m.getId(), List.of()), me.getId()))
                .toList();
    }
}
