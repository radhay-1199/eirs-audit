package org.gl.eir.model.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "sys_generated_alert")
public class RunningAlertDb implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "created_on")
	@CreationTimestamp
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date createdOn;

	@Column(name = "modified_on")
	@UpdateTimestamp
	private Date modifiedOn;

	@Column(name = "user_id", columnDefinition = "INT DEFAULT 0")
	private Integer userId;

	@Column(name = "alert_id", length = 20)
	private String alertId;

	private String description;

	@Column(name = "status", columnDefinition = "INT DEFAULT 0")
	private Integer status;

	private String username;

	public RunningAlertDb(String alertId, String description, Integer status) {
		super();
		this.alertId = alertId;
		this.description = description;
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RunningAlertDb{");
		sb.append("id=").append(id);
		sb.append(", createdOn=").append(createdOn);
		sb.append(", modifiedOn=").append(modifiedOn);
		sb.append(", userId=").append(userId);
		sb.append(", alertId='").append(alertId).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", status=").append(status);
		sb.append(", username='").append(username).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
