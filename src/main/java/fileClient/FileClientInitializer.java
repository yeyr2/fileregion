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
package fileClient;

import coder.FileDecoder;
import coder.MyEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import lombok.Data;

@Data
public class FileClientInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    // todo: 提供可修改项，避免使用final
    private String host;
    private  int port;
    private  String path;
    private FileReceive fileReceive;
    private boolean reconnect = false;
    private FileHandler fileHandler;
    private FileDecoder fileDecoder;

    public FileClientInitializer(SslContext sslCtx, String line, String host, int port, FileReceive fileReceive) {
        this.sslCtx = sslCtx;
        this.path = line;
        this.host = host;
        this.port = port;
        this.fileReceive = fileReceive;
    }

    public FileClientInitializer(SslContext sslCtx, String host, int port, FileHandler fileHandler, FileReceive fileReceive, boolean reconnect){
        this.reconnect = reconnect;
        this.sslCtx = sslCtx;
        this.host = host;
        this.port = port;
        this.fileReceive = fileReceive;
        this.fileDecoder = new FileDecoder();
        this.fileHandler = copyFileHandler(fileHandler,this.fileDecoder);
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if (sslCtx != null) {
            pipeline.addLast("ssl",sslCtx.newHandler(ch.alloc(),host, port));
        }

        if(!reconnect){
            // 使用多个channel,每个channel需要一个new handlerPipe，否则关闭一个channel时，其他的channel会报出channel关闭错误。
            FileDecoder fileDecoder = new FileDecoder();
            pipeline.addLast(fileDecoder);
            pipeline.addLast(new MyEncoder());
            pipeline.addLast(new FileHandler(path,fileDecoder, fileReceive));
            pipeline.addLast(new ExceptionHandler());
        }else{
            pipeline.addLast(fileDecoder);
            pipeline.addLast(new MyEncoder());
            pipeline.addLast(fileHandler);
            pipeline.addLast(new ExceptionHandler());
        }

    }

    private FileHandler copyFileHandler(FileHandler fileHandler, FileDecoder fileDecoder){
        FileHandler newFileHandler = new FileHandler(fileHandler.filePath,fileDecoder, fileReceive);
        newFileHandler.fileSize = fileHandler.fileSize;
        newFileHandler.tempFileSize = fileHandler.tempFileSize;
        newFileHandler.fileName = fileHandler.fileName;

        return newFileHandler;
    }


}
