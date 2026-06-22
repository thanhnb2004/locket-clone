package com.locket.clone.moment.service;

import com.locket.clone.moment.entity.Moment;
import com.locket.clone.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IMomentService {

    Moment create(User owner, MultipartFile image, String caption);

    List<Moment> feed(User me);

    List<Moment> ownMoments(User me);

    Moment getVisible(User me, UUID momentId);
}
