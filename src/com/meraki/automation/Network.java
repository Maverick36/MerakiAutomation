package com.meraki.automation;


import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by miguelmorales on 1/25/16.
 */
@Entity(name = "Networks")
public class Network {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "active")
    private int active;

    @DatabaseField(foreign = true)
    private Organization organization;

    @ManyToOne
    @ForeignCollectionField(foreignFieldName = "network")
    private ForeignCollection<Device> devices;


    public Network(Organization organization, int networkId, String name, String networkSlug, String networkToken) {
        this.organization = organization;
        this.id = networkId;
        this.name = name;
        this.slug = networkSlug;
        this.token = networkToken;

    }

    public Network() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return this.slug;
    }

    public void setSlug(String networkSlug) {
        this.slug = networkSlug;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String networkToken) {
        this.token = networkToken;
    }

    public int getActive() {
        return this.active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public boolean doesDeviceExist(Device device) throws SQLException {

        this.devices.refreshCollection();

        Iterator<Device> itr = this.devices.iterator();

        while (itr.hasNext()) {

            Device device1 = itr.next();

            if (device1.getDeviceId().equals(device.getDeviceId())) {
                return true;
            }
        }

        return false;

    }



}
