package de.chrz.demo;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.KeyBuilder;
import javacard.security.AESKey;
import javacardx.crypto.Cipher;
import javacard.security.RandomData;

public class DemoApplet extends Applet
{

    private static final byte INS_ENCRYPT = (byte) 0x01;
    private static final byte INS_DECRYPT = (byte) 0x02;

    private static AESKey key;
    private static Cipher cipher;


    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException
    {
        // Skip over system provided parameters
        short offset = bOffset;
        offset += (short)(bArray[offset] + 1); // Instance
        offset += (short)(bArray[offset] + 1); // Privileges

        // Instantiate applet class and register it with the OS
        // Pass the user provided installation parameters
        new DemoApplet(bArray, (short)(offset + 1), bArray[offset]).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    protected DemoApplet(byte[] parameters, short parametersOffset, byte parametersLength)
    {
        // Create new AES-128 key
        key = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        // Generate 16 Bytes of random data
        byte[] secretRandom = new byte[16];
        RandomData random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        random.generateData(secretRandom, (short) 0, (short) secretRandom.length);
        // Assign random data as AES key
        key.setKey(secretRandom, (short) 0);

        // Initialize symmetric crypto engine for AES-128
        cipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
    }

    public void process(APDU apdu)
    {
        // Access the incoming APDU commend
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
            // Parse length of passed data payload
            short dataLen = apdu.setIncomingAndReceive();

            // Ensure data length is valid for AES (must be multiple of 16 bytes)
            if ((dataLen % 16) != 0) 
            {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }

            if(ins == INS_ENCRYPT) // Handle encryption command
            {
                // Re-init crypto engine and do the encryption into the same buffer
                cipher.init(key, Cipher.MODE_ENCRYPT);
                len += cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, dataLen, buffer, len);
                validInstruction = true;
            }
            else if(ins == INS_DECRYPT) // Handle decryption command
            {
                // Re-init crypto engine and do the decryption into the same buffer
                cipher.init(key, Cipher.MODE_DECRYPT);
                len += cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, dataLen, buffer, len);
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
