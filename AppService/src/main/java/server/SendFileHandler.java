package server;

import io.netty.channel.*;
import io.netty.handler.stream.ChunkedFile;
import org.Component.message.FileMessage;

import java.io.RandomAccessFile;

public class SendFileHandler extends SimpleChannelInboundHandler<FileMessage> {

    int test = 0;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,final FileMessage msg) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(msg.getName(), "r");
        System.out.println(raf);
        ChannelFuture promise;
        if (msg.getSendType().equals(FileMessage.SendType.SENDFILE)) {
            // sendFile: SSL not enabled - can use zero-copy file transfer.
            DefaultFileRegion defaultFileRegion = new DefaultFileRegion(raf.getChannel(), msg.getNowPos(), raf.length() - msg.getNowPos());
            promise = ctx.channel().writeAndFlush(defaultFileRegion,ctx.newProgressivePromise());
        } else if(msg.getSendType().equals(FileMessage.SendType.MMAP)) {
            // mmap
            ChunkedFile chunkedFile = new ChunkedFile(raf);
            // 加密
            promise = ctx.writeAndFlush(chunkedFile);
        }else{
            System.out.println("err address.");
            return;
        }
        promise.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("Operation result: " + future.channel() + " 传输完毕.");
            } else {
                Throwable cause = future.cause();
                System.err.println(ctx.channel()+" Operation failed: " + cause);
                ctx.fireExceptionCaught(cause);
            }
            raf.close();
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("err: "+ctx.channel());
        ctx.channel().close();
        super.channelInactive(ctx);
    }
}
