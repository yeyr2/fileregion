/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.UserFile.client.fileClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.*;
import message.ChannelStatus;
import org.Component.tool.ServerUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FileReceive {
    private static final int ReconnectCount = 5;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private final boolean SSL = System.getProperty("ssl") != null;
    private String host = System.getProperty("host", "0.0.0.0");
    private int port = Integer.parseInt(System.getProperty("port", SSL ? "8992" : "8023"));
    private SslContext sslCtx;
    private final EventExecutorGroup executorGroup;
    private final Map<Channel,AtomicInteger> lastReconnectTimes;
    private final Map<Channel, ChannelStatus> status;
    private final List<Channel> nowChannel;

    public FileReceive() throws Exception {
        executorGroup = new DefaultEventExecutorGroup(1 + 2);
        lastReconnectTimes = new ConcurrentHashMap<>();
        this.status = new ConcurrentHashMap<>();
        nowChannel = new CopyOnWriteArrayList<>();
        // 构建Bootstrap
        createBootstrap();
        init();
    }

    private void createBootstrap() throws Exception {
        // Configure SSL.
        sslCtx = ServerUtil.buildSslContext();
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    public ChannelFuture start(String path) throws InterruptedException {
        // 构建sendfileHandler
        Bootstrap b = bootstrap.handler(new FileClientInitializer(sslCtx, path, host, port,this));

        ChannelFuture connect = b.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                lastReconnectTimes.put(future.channel(), new AtomicInteger(0));
                status.put(future.channel(), ChannelStatus.active);
            }
        });
        nowChannel.add(connect.channel());
        return connect;
    }

    public ChannelFuture reconnect0(FileHandler fileHandler){
        Bootstrap b = bootstrap.handler(new FileClientInitializer(sslCtx, host, port,fileHandler,this,true));
        ChannelFuture connect = b.connect(host, port).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                lastReconnectTimes.put(future.channel(),new AtomicInteger(0));
                status.put(future.channel(),ChannelStatus.active);
            }
        });
        nowChannel.add(connect.channel());
        return connect;
    }

    public FileReceive close() {
        for (Channel channel : nowChannel) {
            status.replace(channel,ChannelStatus.close);
            channel.close();
        }
        group.shutdownGracefully();
        return this;
    }

    public FileReceive close(Channel channel) {
        status.replace(channel,ChannelStatus.close);
        channel.close();
        return this;
    }

    public void syncAndClose() throws InterruptedException {
        do {
            for (Channel channel : nowChannel) {
                if (status.get(channel) != ChannelStatus.close) {
                    continue;
                }
                channel.closeFuture().sync();
                status.remove(channel);
                nowChannel.remove(channel);
            }
        } while (!nowChannel.isEmpty());

        System.out.println("关闭group");
        Future<?> future = group.shutdownGracefully();
        future.syncUninterruptibly();
        // 关闭客户端
        System.exit(0);
    }

    public FileReceive ReconnectBound(Channel channel, FileHandler fileHandler) {
        status.replace(channel,ChannelStatus.reconnect);
        if (!channel.isActive()) {
            System.out.println(channel + "尝试重连");
            ChannelFuture future = reconnect0(fileHandler).addListener(f -> {
                if (lastReconnectTimes.get(channel).get() >= ReconnectCount * 2) {
                    // 重连次数过多，抛弃该连接，
                    System.out.println(channel + "重连失败。");
                    status.replace(channel,ChannelStatus.close);
                    close(channel);
                    return;
                }
                if (f.isSuccess()) {
                    System.out.println("第" + (lastReconnectTimes.get(channel).get() / 2 + 1)  + "次重连成功。");
                    lastReconnectTimes.remove(channel);
                    nowChannel.remove(channel);
                } else {
                    System.out.println("\r" + Thread.currentThread().getName() + "第" + (lastReconnectTimes.get(channel).get() / 2 + 1) + "次重连失败。");
                    lastReconnectTimes.get(channel).set(2 + lastReconnectTimes.get(channel).get());
                    channel.eventLoop().schedule(() -> ReconnectBound(channel,fileHandler), 2 + lastReconnectTimes.get(channel).get(), TimeUnit.SECONDS);
                }
            });
        }
        return this;
    }

    private void init(){
        ScheduledFuture<?> scheduledFuture = executorGroup.scheduleAtFixedRate(() -> {
            for (Channel channel : nowChannel) {
                int lastTime = lastReconnectTimes.get(channel).get();
                if(lastTime >= ReconnectCount * 2){
                    lastReconnectTimes.remove(channel);
                    status.replace(channel,ChannelStatus.reconnect,ChannelStatus.close);
                }
            }
        }, 0, 20, TimeUnit.SECONDS);

//        scheduledFuture.cancel(false);
    }
}


