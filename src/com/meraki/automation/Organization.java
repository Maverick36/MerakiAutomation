package com.meraki.automation;


import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by miguelmorales on 1/25/16.
 */
@Entity(name = "Organizations")
public class Organization implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "organizationId", unique = true)
    private String organizationId;

    @Column(name = "name")
    private String name;

    @Column(name = "token", unique = true)
    private String token;

    @ManyToOne
    @ForeignCollectionField(foreignFieldName = "organization")
    private ForeignCollection<Network> networks;

    public Organization(String organizationId, String name, String organizationToken) {

        this.organizationId = organizationId;
        this.name = name;
        this.token = organizationToken;
    }

    public Organization() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationToken() {
        return token;
    }

    public void setOrganizationToken(String organizationToken) {
        this.token = organizationToken;
    }

    public boolean doesNetworkExist(Network network) throws SQLException {

        this.networks.refreshCollection();

        Iterator<Network> itr = this.networks.iterator();

        while (itr.hasNext()) {

            Network network1 = itr.next();
            if (network1.getId() == network.getId()) {
                return true;
            }
        }
        return false;

    }

}
