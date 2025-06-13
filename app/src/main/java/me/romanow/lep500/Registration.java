package me.romanow.lep500;

import android.provider.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Registration {
    public static long stringToLong(String ss){
        long out=0;
        for(char cc : ss.toCharArray()){
            out = (out<<4)| (cc>='0' && cc<='9' ? cc-'0' : cc-'a'+10);
        }
        return out;
    }
    public static byte[] longToBytes(long vv){
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(out1);
        try {
            out.writeLong(vv);
            out.flush();
            byte bb[] = out1.toByteArray();
            out.close();
            return bb;
        } catch (Exception ee){
            return new byte[0];
        }
    }
    public static long longFromBytes(byte bb[]){
        ByteArrayInputStream out1 = new ByteArrayInputStream(bb);
        DataInputStream out = new DataInputStream(out1);
        try {
            long vv = out.readLong();
            out.close();
            return vv;
        } catch (Exception ee){
            return 0;
        }
    }
    public static String toBase64(String ss){
        byte bb[] = longToBytes(stringToLong(ss));
        char cc[] = Base64Coder.encode(bb);
        return new String(cc);
    }
    public static long fromBase64(String ss){
        return longFromBytes(Base64Coder.decode(ss));
    }
    public static String createRegistrationCode(String ss){
        return createRegistrationCode(stringToLong(ss));
    }
    public static String createRegistrationCode64(String ss){
        try {
            byte bb[] = Base64Coder.decode(ss);
            long ll = longFromBytes(bb);
            return createRegistrationCode(ll);
        } catch(Exception ee){ return "ошибка формата"; }
    }
    public static String createRegistrationCode(long out){
        int v1=(int) out;
        int v2=(int)(out>>32);
        out = ((long)v1*v2) & 0x0FFFFFFFFL;
        String res="";
        while(out!=0){
            int dd = (int)(out & 0x03F);
            if (dd<26) res+=(char)(dd+'A');
            else
            if (dd<52) res+=(char)(dd-26+'a');
            else
            if (dd<62) res+=(char)(dd-52+'0');
            else
            if (dd==62) res+='*';
            else res+='+';
            out >>=6;
        }
        return res;
    }

    //----------------------------------------------------------------------------------
    public static void main(String aa[]){
        String ss="97a9801d2bfc950e";
        byte bb[] = longToBytes(stringToLong(ss));
        char cc[] = Base64Coder.encode(bb);
        System.out.println(new String(cc));
        System.out.println(createRegistrationCode64("l6mAHSv8lQ4="));
    }

}
