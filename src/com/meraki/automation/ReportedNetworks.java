package com.meraki.automation;

/**
 * Created by miguelmorales on 2/2/16.
 */
public class ReportedNetworks {
    private String organizationName;
    private String networkName;
    private String macAddress;
    private String deviceNumber;
    private String reportedDownTime;
    private String reportedUpTime;


    public ReportedNetworks(String organizationName, String networkName, String macAddress, String deviceNumber, String reportedDownTime, String reportedUpTime) {
        this.organizationName = organizationName;
        this.networkName = networkName;
        this.macAddress = macAddress;
        this.deviceNumber = deviceNumber;
        this.reportedDownTime = reportedDownTime;
        this.reportedUpTime = reportedUpTime;
    }

    public ReportedNetworks() {
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public String getReportedDownTime() {
        return reportedDownTime;
    }

    public String getReportedUpTime() {
        return reportedUpTime;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public void setReportedDownTime(String reportedDownTime) {
        this.reportedDownTime = reportedDownTime;
    }

    public void setReportedUpTime(String reportedUpTime) {
        this.reportedUpTime = reportedUpTime;
    }
}
