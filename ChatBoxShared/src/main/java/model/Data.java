package model;

import java.io.*;
import java.util.Base64;

public class Data {

    public static ServerAction readServerAction(String input)
    {
        Object o = null;
        try {
            byte[] data = Base64.getDecoder().decode(input);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ServerAction serverAction = (ServerAction) o;
        return  serverAction;
    }

    public static String writeServerAction(ServerAction serverAction) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream stream = new ObjectOutputStream(byteArrayOutputStream);
            stream.writeObject(serverAction);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String serialized = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        return serialized;
    }
}
