package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;

/**
 * Created by rapha on 7/4/2016.
 */
public class Subject {
    private char packageActual;
    private char packageCount;
    private char type;
    private char messageSize;
    private String message;

    public Subject(String message) {
        this.message = message;
        this.packageActual = 1;
        this.type = 1;
        if (message.length() > 150) {
            //this.packageCount = (byte) ((message.length() % 150) + 1);
            this.packageCount = (char) ((message.length() / 150) + 1);
            this.messageSize = (char) 150;
        } else {
            this.packageCount = 0x01;
            this.messageSize = (char) message.length();
        }
    }

    private char checksumCalc() {
        return (char) (packageActual + packageCount + type + messageSize);
    }

    public String finalSubject() {
        char[] cabecalho = new char[Constants.MAX_CABECALHO];
        cabecalho[0] = 0x01; //head
        cabecalho[1] = messageSize; //length high
        cabecalho[2] = packageActual; //package
        cabecalho[3] = packageCount; //packages
        cabecalho[4] = type; //type
        cabecalho[5] = checksumCalc(); //cks
        cabecalho[6] = 0x01; //tail

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < cabecalho.length; i++) {
            String valor = Integer.toHexString((int) cabecalho[i]);
            if (valor.length() == 1) {
                valor = "0" + valor;
            }
            hex.append(valor);
        }

        return hex.toString();
    }

    public String mensagemToSend() {
        String sub;
        if (packageActual < packageCount) {
            sub = message.substring(0, 150);
            message = message.substring(150, message.length());
        } else {
            sub = message;
        }
        this.packageActual++;
        return sub;
    }

    public boolean isReady() {
        if (packageActual > packageCount) {
            return false;
        }
        return true;
    }
}
