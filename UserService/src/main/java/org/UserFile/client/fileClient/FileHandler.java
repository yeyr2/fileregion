package org.UserFile.client.fileClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.ObjectUtil;
import org.Component.coder.FileDecoder;
import org.Component.message.CloseChannel;
import org.Component.message.FileMessage;
import org.Component.message.StringMessage;
import org.Component.tool.HashUtil;
import org.Component.tool.OtherUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileHandler extends SimpleChannelInboundHandler<FileMessage> {
    private final String ADDR = "/home/bronya/file/testSendFileByNetty/";
    long fileSize = 0;
    long tempFileSize = 0;
    String fileName; // 接收方（自己）的接收地址
    String filePath; // 发送方的文件地址
    private String sha256;
    private FileDecoder fileDecoder;
    private FileReceive fileReceive;
    private FileMessage.SendType sendType;

    public FileHandler(String line, FileDecoder fileDecoder, FileReceive fileReceive) {
        filePath = ObjectUtil.checkNotNull(line,"file_path");
        this.fileDecoder = fileDecoder;
        this.fileReceive = fileReceive;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(fileDecoder.getStartTransit() && !"".equals(filePath)){
            ctx.writeAndFlush(new StringMessage(filePath,tempFileSize));
        }else{
            if (filePath != null) {
                // Sends the received line to the server.
                ctx.writeAndFlush(new StringMessage(filePath));
            }else{
                System.out.println("failed: filePath err");
                userEventTriggered(ctx,new CloseChannel());
            }
        }

        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        if (msg.isStatus()) {
            fileRead(msg.getByteBuffer());
            if(tempFileSize >= fileSize) {
                fileSize = 0;
                tempFileSize = 0;
                fileDecoder.setStartTransit(true,false);
                System.out.println();
                // 确认接收完毕，触发用户事件，告知服务器并关闭通道。
                userEventTriggered(ctx,new CloseChannel());
            }
            return;
        }

        sha256 = msg.getSha256();
        fileSize = (msg).getSize();
        sendType = msg.getSendType();
        String[] strings = (msg).getName().split("/");
        String oldName = strings[strings.length-1];

        if(tempFileSize == 0){
            // 保证创建文件名不冲突
            boolean orRenameFile;
            do{
                orRenameFile = createAndRenameFile(oldName);
            }while (!orRenameFile);
        }

        // 确认文件
        FileMessage fileMessage = new FileMessage();
        fileMessage.setName(msg.getName());
        fileMessage.setSendType(sendType);
        if(tempFileSize != 0){
            fileMessage.setNowPos(tempFileSize);
        }
        ctx.writeAndFlush(fileMessage);
    }

    private void fileRead(ByteBuffer buffer) throws IOException {
        File file = new File(fileName);
        if(!file.exists()){
            tempFileSize = 0;
            fileSize = 0;
            System.out.println("文件已被删除");
            return;
        }
        try(RandomAccessFile raf = new RandomAccessFile(file,"rwd")){
            FileChannel channel = raf.getChannel();
            channel.position(tempFileSize);
            while (buffer.hasRemaining()) {
                int write = channel.write(buffer);
                tempFileSize += write;
                OtherUtil.Progressbar(fileSize,tempFileSize,fileName);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("\n进入inactive: "+this.fileDecoder.getStartTransit());
        if(this.fileDecoder.getStartTransit() && tempFileSize != 0){
            fileReceive.ReconnectBound(ctx.channel(),this);
        }else{
            fileReceive.close(ctx.channel());
        }

        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 校验文件是否存在问题 -- sha256
        File file = new File(fileName);
        String sha256 = HashUtil.getHash(file,HashUtil.SHA256);
        if(!this.sha256.equals(sha256)){
            // 文件异常,删除文件，提示用户
            System.out.println("文件出错,正在删除,请重新下载...");
            if(file.exists()){
                boolean delete = file.delete();
                if(!delete){
                    System.out.println("文件删除失败...");
                }
            }
        }
        // 告知服务器并关闭通道。
        ctx.writeAndFlush(evt).addListener(future -> ctx.close());
        super.userEventTriggered(ctx,evt);
    }

    public boolean createAndRenameFile(String fileName){
        File from = new File(ADDR+fileName);
        String[] fileInfo = getFileInfo(from);
        return createOrRenameFile(from, fileInfo[0], fileInfo[1]);
    }

    /**
     * @param from yeyr2.jpg
     * @param prefix yeyr2
     * @param suffix yeyr2.jpg
     * @return
     */
    public boolean createOrRenameFile(File from, String prefix, String suffix) {
        File directory = from.getParentFile();
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("创建文件失败: " + directory.getAbsolutePath());
            }
        }

        boolean createFile;
        File newFile = from;
        synchronized (FileHandler.class) {
            for (int i = 1; newFile.exists() && i < Integer.MAX_VALUE; i++) {
                newFile = new File(directory, prefix + '(' + i + ')' + suffix);
            }
            createFile = createFile(newFile);
        }
        fileName = newFile.getAbsolutePath();
        return createFile;
    }

    // 占位创建文件.防止创建重复导致缺失。
    public boolean createFile(File file){
        boolean success;
        try{
            success = file.createNewFile();
            if(success){
                System.out.println(Thread.currentThread().getName() + " -- " +"success:create file "+file.getName());
            }else{
                System.out.println(Thread.currentThread().getName() + " -- " +"failed:create file "+file.getName());
            }
        }catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + " -- " +"create file failed "+file.getName() +": "+e.getMessage());
            return false;
        }

        return success;
    }

    public String[] getFileInfo(File from){
        String fileName=from.getName();
        int index = fileName.lastIndexOf(".");
        String toPrefix;
        String toSuffix="";
        if(index==-1){
            toPrefix=fileName;
        }else{
            toPrefix=fileName.substring(0,index);
            toSuffix=fileName.substring(index);
        }
        return new String[]{toPrefix,toSuffix};
    }

}
