/**
 * Created by Tal on 11/8/2017.
 * Last modified 11/9/2017 by Chase 
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.lang.StringBuffer;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;

public class Seclib{

	static boolean debug = false;

	public static String encodedKey = "abcdefghijklmnop";
	public static byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
	
	public static Signature createSig;
	public static Signature verifySig;
	
	public static byte[] createSignature(PrivateKey priv, String message) throws Exception{
		createSig = Signature.getInstance("SHA256withRSA");	
		createSig.initSign(priv);		
		createSig.update(message.getBytes());		
		byte[] realSig = createSig.sign();		
		return realSig;
	}
	
	public static boolean verifySignature(PublicKey pubKey, byte[] sentSig) throws Exception{
		verifySig = Signature.getInstance("SHA256withRSA");		
		verifySig.initVerify(pubKey);	
		verifySig.update(sentSig);
		
		boolean verified = verifySig.verify(sentSig);
		
		return true;
	}

    public static String messageHash(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(message.getBytes());
        return new String(md.digest(message.getBytes()));
    }

    public static String initializeSecurityParameters(Scanner reader, int securityArray[]){

        int flagC = 0;
        int flagI = 0;
        int flagA = 0;

        String securityArrayString = "";

        String s;

        while(flagC == 0){

            System.out.println("Does this session require confidentiality? Y/N");
            System.out.println();
            s = reader.next();
            System.out.println();

            if(s.equals("Y") || s.equals("y") || s.equals("N") || s.equals("n")){

                if(s.equals("Y") || s.equals("y")){
                    securityArray[0] = 1;
                    flagC = 1;
                }else{
                    securityArray[0] = 0;
                    flagC = 2;
                }
            }

            if(flagC == 0){
                System.out.println("Invalid entry, must be one of Y,y,N,n");
                System.out.println();
            }
        }

        while(flagI == 0){

            System.out.println("Does this session require integrity? Y/N");
            System.out.println();
            s = reader.next();
            System.out.println();

            if(s.equals("Y") || s.equals("y") || s.equals("N") || s.equals("n")){
                flagI = 1;
                if(s.equals("Y") || s.equals("y")){
                    securityArray[1] = 1;
                    flagI = 1;
                }else{
                    securityArray[1] = 0;
                    flagI = 2;
                }
            }

            if(flagI == 0){
                System.out.println("Invalid entry, must be one of Y,y,N,n");
                System.out.println();
            }
        }

        while(flagA == 0){

            System.out.println("Does this session require authentication? Y/N");
            System.out.println();
            s = reader.next();
            System.out.println();

            if(s.equals("Y") || s.equals("y") || s.equals("N") || s.equals("n")){

                if(s.equals("Y") || s.equals("y")){
                    securityArray[2] = 1;
                    flagA = 1;
                }else{
                    securityArray[2] = 0;
                    flagA = 2;
                }
            }

            if(flagA == 0){
                System.out.println("Invalid entry, must be one of Y,y,N,n");
                System.out.println();
            }
        }
		
		//create security string

        for(int i = 0; i < 3; i++){
            securityArrayString = securityArrayString + String.valueOf(securityArray[i]);
        }
		
		//Use security array values to create key, etc based on need
		
		if(securityArray[0] == 1){
			System.out.println("Encryption activated");
		}
		
		if(securityArray[1] == 1){
			System.out.println("Integrity check activated");
		}

		if(securityArray[2] == 1){
			System.out.println("Signature verification activated");
		}		

        return securityArrayString;
    }
	
	public static byte[] encryptMessage(String plainText, SecretKey sKey)throws Exception{
			if (debug == true) System.out.println("encryptMessage() called");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		
			cipher.init(Cipher.ENCRYPT_MODE, sKey);
			byte[] plainTextBytes = plainText.getBytes();
			byte[] encryptedBytes = cipher.doFinal(plainTextBytes);
            String out = new String(encryptedBytes);
			if (debug == true) System.out.println("Printing the string: "+out);
			return encryptedBytes;

	}
	
    public static String decryptMessage(byte[] encryptedBytes, SecretKey sKey) throws Exception {
			if (debug == true) System.out.println("decryptMessage() called");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	
			cipher.init(Cipher.DECRYPT_MODE, sKey);
			byte[] recoveredBytes = cipher.doFinal(encryptedBytes);
			String decryptionString = new String(recoveredBytes);
			return decryptionString;
    }	
}
