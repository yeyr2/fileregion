package org.UserFile.client.fileClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ExceptionMessage;

public class ExceptionHandler extends SimpleChannelInboundHandler<ExceptionMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExceptionMessage msg) throws Exception {
        // todo: 错误分析
        System.out.println("已触发： "+msg);
        exceptionCaught(ctx,new Exception(msg.getCause()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }
}
