package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.nio.MappedByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TelnetClient {
    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
//    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8992" : "8023"));
    static final int PORT = 8088;

    public static void main(String[] args) throws InterruptedException {
        new TelnetClient().start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();                //1
            b.group(group)                                //2
                    .channel(NioSocketChannel.class)            //3
                    .remoteAddress(new InetSocketAddress(HOST, PORT))    //4
                    .handler(new ChannelInitializer<SocketChannel>() {    //5
                                 @Override
                                 public void initChannel(SocketChannel ch) {
                                     ch.pipeline().addLast(new StringDecoder());
                                     ch.pipeline().addLast(new StringEncoder());
//                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>(){
//                                @Override
//                                public void channelActive(ChannelHandlerContext ctx) {
//                                    ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
//                                }
//
//                                @Override
//                                public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
//                                    System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));    //3
//
//                                }
//
//                                @Override
//                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {                    //4
//                                    cause.printStackTrace();
//                                    ctx.close();
//                                }
//                            });
                                     ch.pipeline().addLast("idle", new SimpleChannelInboundHandler<String>() {

                                         int readIdleTimes = 0;

                                         @Override
                                         protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
                                             System.out.println(" ====== > [server] message received : " + s);
                                             if ("Heartbeat Packet".equals(s)) {
                                                 ctx.channel().writeAndFlush("ok");
                                             } else {
                                                 System.out.println(" 其他信息处理 ... ");
                                             }
                                         }

                                         @Override
                                         public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                             IdleStateEvent event = (IdleStateEvent) evt;

                                             String eventType = null;
                                             switch (event.state()) {
                                                 case READER_IDLE:
                                                     eventType = "读空闲";
                                                     readIdleTimes++; // 读空闲的计数加1
                                                     break;
                                                 case WRITER_IDLE:
                                                     eventType = "写空闲";
                                                     // 不处理
                                                     break;
                                                 case ALL_IDLE:
                                                     eventType = "读写空闲";
                                                     // 不处理
                                                     break;
                                             }


                                             System.out.println(ctx.channel().remoteAddress() + "超时事件：" + eventType);
                                             if (readIdleTimes > 3) {
                                                 System.out.println(" [server]读空闲超过3次，关闭连接，释放更多资源");
                                                 ctx.channel().writeAndFlush("idle close");
                                                 ctx.channel().close();
                                             }
                                         }

                                         @Override
                                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                             System.err.println("=== " + ctx.channel().remoteAddress() + " is active ===");
                                         }
                                     });
                                     ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                                         public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                             System.out.println("OutBoundHandlerB: " + msg);
                                             ctx.write(msg, promise);
                                         }
                                         public void handlerAdded(final ChannelHandlerContext ctx) {
                                             // 定时任务。模拟用户写操作
                                             ctx.executor().schedule(() -> {
                                                 ctx.channel().write("hello world"); // 1
                                                 // ctx.write("hello world"); // 2
                                             }, 3, TimeUnit.SECONDS);
                                         }
                                     });
                                 }
                             });

            ChannelFuture f = b.connect().sync();        //6

            Channel channel = f.channel();
            String text = "Heartbeat Packet";
            Random random = new Random();
            while (channel.isActive()) {
                int num = random.nextInt(10);
                Thread.sleep(num * 1000);
                channel.writeAndFlush(text);
            }

            f.channel().closeFuture().sync();            //7
        } finally {
            group.shutdownGracefully().sync();            //8
        }
    }
}

class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }
    public static void main(String[] args) throws Exception {
        new EchoServer(8088).start();                //2
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(); //3
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)                                //4
                    .channel(NioServerSocketChannel.class)        //5
                    .localAddress(new InetSocketAddress(port))    //6
                    .childHandler(new ChannelInitializer<SocketChannel>() { //7
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
//                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
//                                @Override
//                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
//                                    if(msg instanceof ByteBuf){
//                                        ByteBuf in = (ByteBuf) msg;
//                                        System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8));        //2
////                                    ctx.write(in);                            //3
//                                        ch.writeAndFlush(in);
//                                    }
//                                    ctx.fireChannelRead(msg);
//                                }
//
//                                @Override
//                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//                                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);//4
////                                            .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
//                                }
//
//                                @Override
//                                public void exceptionCaught(ChannelHandlerContext ctx,
//                                        Throwable cause) {
//                                    cause.printStackTrace();                //5
//                                    ctx.close();                            //6
//                                }
//                            });
                            ch.pipeline().addLast("IDLE",new IdleStateHandler(3,0,0, TimeUnit.SECONDS));
                            ch.pipeline().addLast("idleHandler",new SimpleChannelInboundHandler<String>() {

                                int readIdleTimes = 0;

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String s) {
                                    System.out.println(" ====== > [server] message received : " + s);
                                    if ("Heartbeat Packet".equals(s)) {
                                        ctx.channel().writeAndFlush("ok");
                                    } else {
                                        System.out.println(" 其他信息处理 ... ");
                                    }
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;

                                    String eventType = null;
                                    switch (event.state()) {
                                        case READER_IDLE:
                                            eventType = "读空闲";
                                            readIdleTimes++; // 读空闲的计数加1
                                            break;
                                        case WRITER_IDLE:
                                            eventType = "写空闲";
                                            // 不处理
                                            break;
                                        case ALL_IDLE:
                                            eventType = "读写空闲";
                                            // 不处理
                                            break;
                                    }

                                    System.out.println(ctx.channel().remoteAddress() + "超时事件：" + eventType);
                                    if (readIdleTimes > 3) {
                                        System.out.println(" [server]读空闲超过3次，关闭连接，释放更多资源");
                                        ctx.channel().writeAndFlush("idle close");
                                        ctx.channel().close();
                                    }
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.err.println("=== " + ctx.channel().remoteAddress() + " is active ===");
                                }
                            });
                        }
                    });

            ChannelFuture f = b.bind().sync();            //8
            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();            //9
        } finally {
            group.shutdownGracefully().sync();            //10
        }
    }

}

