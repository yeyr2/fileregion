package org.Component.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import message.CloseChannel;
import message.FileMessage;
import message.Message;
import message.StringMessage;
import tool.Json;

import java.util.List;

/**
 *  int                     |   String                  |   Object
 *  size(fileAttributes)    |  fileAttributes(file)     |   file
 */

public class MyDecoder extends MessageToMessageDecoder<ByteBuf> {
    long size;
    int type;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        if(byteBuf.readableBytes() < 8){
            return;
        }

        if(size == 0){
            size = byteBuf.readInt();
        }

        if(type == 0){
            type = byteBuf.readInt();
        }

        if(size > byteBuf.readableBytes()){
            return;
        }

        byte[] bytes = new byte[(int) size];

        byteBuf.readBytes(bytes);

        String num = new String(bytes);

        Class<? extends Message> tcl = Message.map.get(type);

        Message cl = Json.jsonToPojo(num,tcl);

        if (cl instanceof StringMessage || cl instanceof CloseChannel || cl instanceof FileMessage){
            list.add(cl);
        }

        size = 0;
        type = 0;
    }

}
