package com.locket.clone.moment.entity;

import com.locket.clone.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "moments")
public class Moment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "image_filename", nullable = false)
    private String imageFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(length = 500)
    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Moment() {
    }

    public Moment(User owner, String imageFilename, String contentType, String caption) {
        this.owner = owner;
        this.imageFilename = imageFilename;
        this.contentType = contentType;
        this.caption = caption;
    }

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCaption() {
        return caption;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
