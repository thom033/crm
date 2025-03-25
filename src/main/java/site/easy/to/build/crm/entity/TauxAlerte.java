package site.easy.to.build.crm.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_rate")
public class TauxAlerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private int rateId;

    @Column(name = "alert_percentage", nullable = false)
    private BigDecimal alertPercentage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private int userId;

    // Getters and Setters
    public int getRateId() {
        return rateId;
    }

    public void setRateId(int rateId) {
        this.rateId = rateId;
    }

    public BigDecimal getAlertPercentage() {
        return alertPercentage;
    }

    public void setAlertPercentage(BigDecimal alertPercentage) {
        this.alertPercentage = alertPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
