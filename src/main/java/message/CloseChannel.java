package message;

public class CloseChannel extends Message{
    @Override
    public int getType() {
        return CloseChannel;
    }
}
