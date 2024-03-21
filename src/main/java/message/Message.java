package message;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {

    public static int StringMessage = 0;
    public static int FileMessage = 1;
    public static int ExceptionMessage = 2;
    public static int CloseChannel = -1;

    public abstract int getType();

    public static Map<Integer,Class<? extends Message>> map = new HashMap<>();

    static{
        map.put(StringMessage, StringMessage.class);
        map.put(FileMessage, FileMessage.class);
        map.put(ExceptionMessage, ExceptionMessage.class);
        map.put(CloseChannel, CloseChannel.class);
    }

}
