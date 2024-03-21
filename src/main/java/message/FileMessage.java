package message;

import java.nio.ByteBuffer;
import java.util.Objects;

public class FileMessage extends Message {
    private long size;
    private String name;
    private ByteBuffer byteBuffer;
    private boolean status;
    private long nowPos = 0;
    private String sha256;
    private SendType sendType;

    public FileMessage() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public long getSize() {
        return size;
    }

    public FileMessage setSize(long size) {
        this.size = size;
        return this;
    }

    public String getName() {
        return name;
    }

    public FileMessage setName(String name) {
        this.name = name;
        return this;
    }

    public long getNowPos() {
        return nowPos;
    }

    public void setNowPos(long nowPos) {
        this.nowPos = nowPos;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public SendType getSendType() {
        return sendType;
    }

    public void setSendType(SendType sendType) {
        this.sendType = sendType;
    }

    @Override
    public int getType() {
        return FileMessage;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "size=" + size +
                ", name='" + name + '\'' +
                ", byteBuffer=" + byteBuffer +
                ", status=" + status +
                ", nowPos=" + nowPos +
                ", sha256='" + sha256 + '\'' +
                ", sendType=" + sendType +
                '}';
    }

    public static class SendType{
        private String type;
        public static final SendType SENDFILE = new SendType("sendfile");
        public static final SendType MMAP = new SendType("mmap");

        SendType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SendType)) return false;
            SendType sendType = (SendType) o;
            return getType().equals(sendType.getType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType());
        }

        @Override
        public String toString() {
            return "SendType{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }
}
