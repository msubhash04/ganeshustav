package com.ganeshutsav.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks auctioned items (Velampata) - e.g. the Laddu auction. Can happen
 * on any specific day of the festival, or on the final day; dayNumber is
 * nullable to support both.
 */
@Entity
@Table(name = "auction_items")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_year_id")
    @ToString.Exclude
    private FestivalYear festivalYear;

    // nullable: null means "final day / not tied to a specific day"
    private Integer dayNumber;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String winnerName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode; // nullable until paid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    @ToString.Exclude
    private Member recordedBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
