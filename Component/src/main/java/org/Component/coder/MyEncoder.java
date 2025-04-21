package org.Component.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import message.ExceptionMessage;
import message.Message;
import tool.Json;

import java.util.List;

public class MyEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, List<Object> list) throws Exception {
        int type = (message).getType();
        byte[] bytes;
        ByteBuf byteBuf = ctx.alloc().buffer();

        if (type == Message.ExceptionMessage && ((ExceptionMessage)message).getCause() == null) {
            //Objects.equals(((ExceptionMessage) messages).getExceptionType(), ExceptionMessage.ExceptionFile)
            bytes = "NULL".getBytes();
        } else {
            String json = Json.pojoToJson(message);
            bytes = json.getBytes();
        }
        byteBuf.writeInt(bytes.length);
        byteBuf.writeInt(type);
        byteBuf.writeBytes(bytes);
        list.add(byteBuf);
    }
}
