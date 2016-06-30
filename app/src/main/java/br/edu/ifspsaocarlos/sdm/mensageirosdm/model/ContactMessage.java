package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContactMessage extends RealmObject {

    @PrimaryKey
    private String id;
    private String lastMessageId;

    public ContactMessage() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}
