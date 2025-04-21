package server;

import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;
import org.Component.message.ExceptionMessage;
import org.Component.message.FileMessage;
import org.Component.message.StringMessage;
import org.Component.tool.HashUtil;

import java.io.File;

import static org.Component.message.ExceptionMessage.ExceptionFile;

public class FileServerHandler extends SimpleChannelInboundHandler<StringMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx+": start");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, StringMessage msg) {
        try{
            // 获取文件位置，校验
            File file = new File(msg.getString());
            long length = file.length();
            String sha256 = HashUtil.getHash(file, HashUtil.SHA256);
            boolean sendfile = ctx.pipeline().get(SslHandler.class) == null;

            FileMessage fileMessage = new FileMessage();
            fileMessage.setName(msg.getString()).setSize(length).setSha256(sha256);
            fileMessage.setSendType(sendfile ? FileMessage.SendType.SENDFILE : FileMessage.SendType.MMAP);
            if(msg.getStart() != 0){
                fileMessage.setNowPos(msg.getStart());
                ctx.fireChannelRead(fileMessage);
            }else{
                ctx.writeAndFlush(fileMessage);
            }
        } catch (Exception e) {
            exceptionCaught(ctx,e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(new ExceptionMessage().setCause("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage()).setExceptionType("文件不存在",ExceptionFile)).addListener(ChannelFutureListener.CLOSE);
        }
    }
}



