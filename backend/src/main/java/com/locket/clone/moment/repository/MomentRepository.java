package com.locket.clone.moment.repository;

import com.locket.clone.moment.entity.Moment;
import com.locket.clone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MomentRepository extends JpaRepository<Moment, UUID> {

    List<Moment> findByOwnerInOrderByCreatedAtDesc(Collection<User> owners);

    List<Moment> findByOwnerOrderByCreatedAtDesc(User owner);
}
