package com.meraki.automation;

import javax.persistence.*;

/**
 * Created by miguelmorales on 4/18/16.
 */

@Entity(name = "StatusReport")
public class StatusReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "deviceId", nullable = false)
    private int deviceId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reportedDown", nullable = false)
    private String reportedDown;

    @Column(name = "reportedUp")
    private String reportedUp;


    public StatusReport(int deviceId, String status, String reportedDown) {
        this.deviceId = deviceId;
        this.status = status;
        this.reportedDown = reportedDown;
    }

    public StatusReport() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReportedDown() {
        return reportedDown;
    }

    public void setReportedDown(String reportedDown) {
        this.reportedDown = reportedDown;
    }

    public String getReportedUp() {
        return reportedUp;
    }

    public void setReportedUp(String reportedUp) {
        this.reportedUp = reportedUp;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}

