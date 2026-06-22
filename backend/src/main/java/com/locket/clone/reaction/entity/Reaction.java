package com.locket.clone.reaction.entity;

import com.locket.clone.moment.entity.Moment;
import com.locket.clone.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * An emoji reaction a user puts on a moment. A user can have at most one reaction
 * per moment (enforced by the unique constraint); reacting again updates the emoji.
 */
@Entity
@Table(name = "reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"moment_id", "user_id"})
})
public class Reaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "moment_id")
    private Moment moment;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 16)
    private String emoji;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Reaction() {
    }

    public Reaction(Moment moment, User user, String emoji) {
        this.moment = moment;
        this.user = user;
        this.emoji = emoji;
    }

    public UUID getId() {
        return id;
    }

    public Moment getMoment() {
        return moment;
    }

    public User getUser() {
        return user;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
        this.createdAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
