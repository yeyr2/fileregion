package message;

public enum ChannelStatus {
    close(0,"close"),
    active(1,"active"),
    reconnect(2,"reconnect");

    ChannelStatus(int statusId, String status) {}
}
