package server;

import io.netty.channel.*;
import message.CloseChannel;

public class CloseChannelHandler extends SimpleChannelInboundHandler<CloseChannel> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloseChannel msg) {
        System.out.println(ctx.channel()+" 请求关闭通道.");
        ChannelFuture close = ctx.channel().close();
        close.addListener(future1 -> {
            System.out.println(ctx.channel()+" 通道关闭完成.");
        });
    }
}
