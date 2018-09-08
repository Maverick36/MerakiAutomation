package com.meraki.automation;

import com.j256.ormlite.field.DatabaseField;

import javax.persistence.*;

/**
 * Created by miguelmorales on 1/25/16.
 */
@Entity(name = "Devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "deviceId", unique = true)
    private String deviceId;

    @Column(name = "macAddress", unique = true)
    private String mac;

    @DatabaseField(foreign = true)
    private Network network;

    private String status;

    private String networkToken;

    private String networkSlug;

    public Device(String deviceId, String mac, String status, String networkToken, String networkSlug) {
        this.deviceId = deviceId;
        this.mac = mac;
        setStatus(status);
        this.networkToken = networkToken;
        this.networkSlug = networkSlug;
    }

    public Device() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getStatus() {

        return this.status;

    }

    public void setStatus(String status) {

        if (status.substring(status.indexOf("ow-") + 3, status.indexOf(".png")).equals("1")) {
            this.status = "green";

        } else {
            this.status = status.substring(status.indexOf("ow-") + 3, status.indexOf(".png"));
        }


    }

    public String getNetworkToken() {
        return this.networkToken;
    }

    public void setNetworkToken(String networkToken) {
        this.networkToken = networkToken;
    }

    public Network getNetwork() {
        return this.network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getNetworkSlug() {
        return networkSlug;
    }

    public void setNetworkSlug(String networkSlug) {
        this.networkSlug = networkSlug;
    }
}
