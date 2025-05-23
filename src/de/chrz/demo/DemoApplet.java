package de.chrz.demo;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class DemoApplet extends Applet
{

    private static final byte INS_ENCRYPT = (byte) 0x01;

    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException
    {
        short offset = bOffset;
        offset += (short)(bArray[offset] + 1); // Instance
        offset += (short)(bArray[offset] + 1); // Privileges
        new DemoApplet(bArray, (short)(offset + 1), bArray[offset]).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    protected DemoApplet(byte[] parameters, short parametersOffset, byte parametersLength)
    {
        
    }

    public void process(APDU apdu)
    {
        final byte[] buffer = apdu.getBuffer();
        final byte ins = buffer[ISO7816.OFFSET_INS];

        // Verify command class
        if (!apdu.isISOInterindustryCLA())
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        short len = 0;
        boolean validInstruction = false;

        // Respond to applet selection
        if (selectingApplet())
        {
            validInstruction = true;
        }
        else
        {
            if(ins == INS_ENCRYPT)
            {
                
                validInstruction = true;
            }
        }

        if(validInstruction)
        {
            // Send out response
            short le = apdu.setOutgoing();
            len = le > 0 ? (le > len ? len : le) : len;
            apdu.setOutgoingLength(len);
            apdu.sendBytes((short) 0, len);
        }
        else
        {
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

}
