package tool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class OtherUtil {
    private static final ExecutorService executor = Executors.newScheduledThreadPool(1);
    private static final char INCOMPLETE = '░'; // U+2591 Unicode Character 表示还没有完成的部分
    private static final char COMPLETE = '█'; // U+2588 Unicode Character 表示已经完成的部分
    public static void Progressbar(long total, long progress,String fileName){
        Runnable runnable = () -> {
            double percentage = (progress * 1.0 / total) * 100;
            StringBuilder builder = new StringBuilder();
            Stream.generate(() -> COMPLETE).limit((long) percentage).forEach(builder::append);
            Stream.generate(() -> INCOMPLETE).limit(100 - (long) percentage).forEach(builder::append);
            String progressBar = "\r" + "正在下载： " + builder;
            String percent = " " + percentage + "%   ";
            System.out.print(progressBar + percent + fileName);
        };

        executor.submit(runnable);
    }
}
