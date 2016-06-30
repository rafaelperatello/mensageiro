package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Contact extends RealmObject {

    @PrimaryKey
    private String id;
    private String nome_completo;
    private String apelido;
    private ContactMessage contactMessage;

    public Contact() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome_completo() {
        return nome_completo;
    }

    public void setNome_completo(String nome_completo) {
        this.nome_completo = nome_completo;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public ContactMessage getContactMessage() {
        return contactMessage;
    }

    public void setContactMessage(ContactMessage contactMessage) {
        this.contactMessage = contactMessage;
    }
}
