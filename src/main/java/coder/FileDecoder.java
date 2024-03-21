package coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import message.ExceptionMessage;
import message.FileMessage;
import message.Message;
import tool.Json;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDecoder extends MessageToMessageDecoder<ByteBuf> {
    long size;
    int type;
    AtomicBoolean startTransit = new AtomicBoolean(false);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        if(startTransit.get()){
            ByteBuffer buffer = byteBuf.internalNioBuffer(byteBuf.readerIndex(),byteBuf.readableBytes());
            FileMessage fileMessage = new FileMessage();
            fileMessage.setStatus(true);
            fileMessage.setByteBuffer(buffer);
            list.add(fileMessage);
            return;
        }

        if(byteBuf.readableBytes() < 8){
            return;
        }

        if(size == 0){
            size = byteBuf.readInt();
        }

        if(type == 0){
            type = byteBuf.readInt();
            if(type != 1){
                size = 0;
                type = 0;
                return;
            }
        }

        if(size > byteBuf.readableBytes()){
            return;
        }

        byte[] bytes = new byte[(int) size];
        byteBuf.readBytes(bytes);
        String num = new String(bytes);
        Class<? extends Message> tcl = Message.map.get(type);
        Message cl = Json.jsonToPojo(num,tcl);

        if(cl instanceof FileMessage){
            startTransit.set(true);
            list.add(cl);
        } else if (cl instanceof ExceptionMessage) {
            list.add(cl);
        }

        size = 0;
        type = 0;
    }

    public void setStartTransit(boolean oldValue,boolean startTransit) {
        this.startTransit.compareAndSet(oldValue,startTransit);
    }

    public boolean getStartTransit() {
        return startTransit.get();
    }
}
