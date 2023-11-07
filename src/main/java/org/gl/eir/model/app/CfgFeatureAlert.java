package org.gl.eir.model.app;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cfg_feature_alert")
public class CfgFeatureAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    @Column(name = "alert_id")
    private String alertId;

    @Column(name = "description")
    private String description;

    @Column(name = "feature")
    private String feature;

    public CfgFeatureAlert() {
    }

    public CfgFeatureAlert(Integer id, LocalDateTime createdOn, LocalDateTime modifiedOn, String alertId, String description, String feature) {
        this.id = id;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.alertId = alertId;
        this.description = description;
        this.feature = feature;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CfgFeatureAlert{");
        sb.append("id=").append(id);
        sb.append(", createdOn=").append(createdOn);
        sb.append(", modifiedOn=").append(modifiedOn);
        sb.append(", alertId='").append(alertId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", feature='").append(feature).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
