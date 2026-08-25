package com.ganeshutsav.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Master list of sponsorship categories (e.g. "Vigraha Dhata" / Idol Sponsor,
 * "Laddu Dhata" / Laddu Sponsor). Managed by the President; feeds the
 * category dropdown on the General Sponsors page.
 */
@Entity
@Table(name = "sponsorship_categories")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorshipCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. "Vigraha Dhata (Idol Sponsor)"

    @Column(length = 500)
    private String description;

    // lets the President retire a category from the "add sponsor" dropdown
    // without deleting historical sponsors already assigned to it
    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @ToString.Exclude
    private Member createdBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (!this.active) this.active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
