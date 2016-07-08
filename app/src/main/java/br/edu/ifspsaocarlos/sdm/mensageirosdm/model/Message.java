package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Message extends RealmObject {

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

    private boolean validateChar(char letra)
    {
        if (((letra >= 0x30) && (letra <= 0x39)) || // é numero
            ((letra >= 0x41) && (letra <= 0x46)) ||  // é maiusculo
            ((letra >= 0x61) && (letra <= 0x66))) // é minúsculo
        {
            return true;
        }
        return false;
    }

    private char[] convertHexToChar() {
        char[] bytes = new char[Constants.MAX_CABECALHO];
        char[] auxChar;
        int count = 0;
        boolean ok;

        if (this.assunto.length() >= (Constants.MAX_CABECALHO * 2)) {
            for (int i = 0; i < (Constants.MAX_CABECALHO * 2); i += 2) {
                String str = this.assunto.substring(i, i + 2);
                auxChar = str.toCharArray();
                ok = false;
                if (auxChar.length == 2) {
                    if (validateChar(auxChar[0])) {
                        if (validateChar(auxChar[1])) {
                            bytes[count] = (char) Integer.parseInt(str, 16);
                            count++;
                            ok = true;
                        }
                    }
                }

                if (!ok)
                {
                    break;
                }
            }
        }
        return bytes;
    }

    public boolean isBigMessage() {
        char[] bytes = convertHexToChar();
        if ((bytes[0] == 0x01) && (bytes[6] == 0x01)) {
            char cks = (char) (bytes[1] + bytes[2] + bytes[3] + bytes[4]);
            if (cks == bytes[5]) {
                if (bytes[3] > 0x01) // se há mais de um pacote então é bigMessage
                {
                    return true;
                }
            }
        }
        return false;
    }

    public char sequencePackage(char sequence) {
        char[] bytes = convertHexToChar();
        if (sequence == bytes[2]) {
            if (bytes[2] == bytes[3]) {
                return 0x00;
            }
        } else {
            return 0x00;
        }
        return bytes[2];
    }

    public void concatenateMessage(String msg) {
        this.corpo += msg;
    }
}
