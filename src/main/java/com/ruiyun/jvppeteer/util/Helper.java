package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Helper implements Constant {
    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(RECV_MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
        JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String message = "Protocol error "+methodNode.asText()+": "+errorMsg;
        JsonNode dataNode = errNode.get(RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if(dataNode != null) {
            message += " "+dataNode.asText();
        }
        return message;
    }

    public static final boolean isWin64(){
        String arch = System.getProperty("os.arch");
        if(arch.contains("64")){
            return true;
        }
        return false;
    }

    public static final String paltform(){
        return System.getProperty("os.name");
    }

    public static final void chmod(String path,String perms) throws IOException {
        if(StringUtil.isEmpty(path))
            throw new IllegalArgumentException("Path must not be empty");

        char[] chars = perms.toCharArray();
        if(chars.length != 3) throw new IllegalArgumentException("perms length must be 3");

        Path path1 = Paths.get(path);
        Set<PosixFilePermission> permissions = new HashSet<>();
        //own
        if('1' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('2' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }else if('3' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('4' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_READ);
        }else if('5' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('6' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }else if('7' == chars[0]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        //group
        if('1' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('2' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }else if('3' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('4' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_READ);
        }else if('5' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('6' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }else if('7' == chars[1]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        //other
        if('1' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('2' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }else if('3' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('4' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_READ);
        }else if('5' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }else if('6' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }else if('7' == chars[2]){
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }

        Files.setPosixFilePermissions(path1,permissions);
    }

    public static final String  join(String root,String...args){
       return  Paths.get(root,args).toString().toString();
    }


}
