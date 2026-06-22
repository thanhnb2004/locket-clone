package com.locket.clone.moment.service.iplm;

import com.locket.clone.common.ApiException;
import com.locket.clone.friendship.service.IFriendshipService;
import com.locket.clone.moment.entity.Moment;
import com.locket.clone.moment.repository.MomentRepository;
import com.locket.clone.moment.service.IMomentService;
import com.locket.clone.storage.service.IStorageService;
import com.locket.clone.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MomentService implements IMomentService {

    private final MomentRepository momentRepository;
    private final IFriendshipService friendshipService;
    private final IStorageService storageService;

    public MomentService(MomentRepository momentRepository,
                         IFriendshipService friendshipService,
                         IStorageService storageService) {
        this.momentRepository = momentRepository;
        this.friendshipService = friendshipService;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public Moment create(User owner, MultipartFile image, String caption) {
        String filename = storageService.store(image);
        Moment moment = new Moment(owner, filename, image.getContentType(), caption);
        return momentRepository.save(moment);
    }

    /**
     * The home feed: the user's own moments plus those of all accepted friends, newest first.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Moment> feed(User me) {
        List<User> owners = new ArrayList<>(friendshipService.listFriends(me));
        owners.add(me);
        return momentRepository.findByOwnerInOrderByCreatedAtDesc(owners);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Moment> ownMoments(User me) {
        return momentRepository.findByOwnerOrderByCreatedAtDesc(me);
    }

    /**
     * Loads a moment the given user is allowed to see (their own, or a friend's).
     */
    @Override
    @Transactional(readOnly = true)
    public Moment getVisible(User me, UUID momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Moment not found"));

        boolean isOwner = moment.getOwner().getId().equals(me.getId());
        boolean isFriend = friendshipService.listFriends(me).stream()
                .anyMatch(f -> f.getId().equals(moment.getOwner().getId()));

        if (!isOwner && !isFriend) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to view this moment");
        }
        return moment;
    }
}
