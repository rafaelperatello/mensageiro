package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

import io.realm.annotations.PrimaryKey;

public class Message {

    @PrimaryKey
    private String id;
    private String origem_id;
    private String destino_id;
    private String assunto;
    private String corpo;

    public Message() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigem_id() {
        return origem_id;
    }

    public void setOrigem_id(String origem_id) {
        this.origem_id = origem_id;
    }

    public String getDestino_id() {
        return destino_id;
    }

    public void setDestino_id(String destino_id) {
        this.destino_id = destino_id;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }
}
