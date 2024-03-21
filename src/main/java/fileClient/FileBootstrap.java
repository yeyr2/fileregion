package fileClient;

public class FileBootstrap {
    static int numThreads = 100;

    public static void main(String[] args) throws Exception {
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        String path = in.readLine();
        String path = "/home/bronya/Downloads/book/Netty 4核心原理与手写RPC框架实战 (谭勇德) (z-lib.org).epub";
        FileReceive fileReceive = new FileReceive();
        for (int i = 0 ; i < numThreads ; i++){
            fileReceive.start(path);
        }

        fileReceive.syncAndClose();
    }

}
