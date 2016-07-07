package br.edu.ifspsaocarlos.sdm.mensageirosdm.util;

import android.util.Log;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;

/**
 * Created by rapha on 7/7/2016.
 */
public class BigMessage {
    private static Message bigMessage = null;
    private static char sequence = 0x01;

    public final static int BIG_MESSAGE_NOT_DETECTED = 0;
    public final static int BIG_MESSAGE_DETECTED = 1;
    public final static int BIG_MESSAGE_CONCATENATED = 2;
    public final static int BIG_MESSAGE_ENDED = 3;

    public static Message getBigMessage(){
        return bigMessage;
    }

    public static int bigMessageValidation(Message message) {
        if (message.isBigMessage()) {
            char pkg = message.sequencePackage(sequence);
            if (pkg == 0x01) {
                bigMessage = message;
                sequence += 0x01;
                return BIG_MESSAGE_DETECTED;
            } else {
                if (bigMessage != null) {
                    bigMessage.concatenateMessage(message.getCorpo());
                    bigMessage.setId(message.getId());
                    if (pkg == 0x00) {
                        sequence = 0x01;
                        return BIG_MESSAGE_ENDED;

                    } else {
                        sequence += 0x01;
                        return BIG_MESSAGE_CONCATENATED;
                    }
                }
            }
        }
        sequence = 0x01;
        bigMessage = null;
        return BIG_MESSAGE_NOT_DETECTED;
    }
}
