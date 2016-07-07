package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContactMessage extends RealmObject {

    @PrimaryKey
    private String id;
    private String lastFromContact;
    private String lastToContact;

    public ContactMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastFromContact() {
        return lastFromContact;
    }

    public void setLastFromContact(String lastMessageId) {
        this.lastFromContact = lastMessageId;
    }

    public String getLastToContact() {
        return lastToContact;
    }

    public void setLastToContact(String lastMessageId) {
        this.lastToContact = lastMessageId;
    }
}