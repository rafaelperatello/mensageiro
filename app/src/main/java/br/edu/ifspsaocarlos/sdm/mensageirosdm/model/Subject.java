package br.edu.ifspsaocarlos.sdm.mensageirosdm.model;

/**
 * Created by rapha on 7/4/2016.
 */
public class Subject{
    //short pq esse não tem sinal (0..255) já o byte tem (-127..128)
    private short packageActual;
    private short packageCount;
    private short type;
    private short messageSize;
    private String message;

    public Subject (String message){
        this.message = message;
        this.packageActual = 1;
        this.type = 1;
        if (message.length() > 150)
        {
            //this.packageCount = (byte) ((message.length() % 150) + 1);
            this.packageCount = (short) ((message.length() / 150) + 1);
            this.messageSize = (short) 150;
        }
        else
        {
            this.messageSize = (short) message.length();
        }
    }

    private short checksumCalc(){
        return (short)(packageActual + packageCount + type + messageSize);
    }

    public String finalSubject(){
        short[] cabecalho = new short[7];
        cabecalho[0] = 0x01; //head
        cabecalho[1] = messageSize; //length high
        cabecalho[2] = packageActual; //package
        cabecalho[3] = packageCount; //packages
        cabecalho[4] = type; //type
        cabecalho[5] = checksumCalc(); //cks
        cabecalho[6] = 0x01; //tail

        return cabecalho.toString();
    }

    public String mensagemToSend(){
        String sub;
        if (packageActual < packageCount) {
            sub = message.substring(0, 150);
            message = message.substring(150, message.length());
        }
        else
        {
            sub = message;
        }
        this.packageActual++;
        return sub;
    }

    public boolean isReady(){
        if (packageActual > packageCount)
        {
            return false;
        }
        return true;
    }
}
