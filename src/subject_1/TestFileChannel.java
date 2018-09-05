package subject_1;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/04 23:18
 */
public class TestFileChannel {
    /**
     * 利用通道和非直接缓冲区传输文件
     */
    @Test
    public void test1() throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            in = new FileInputStream("img/mm.jpg");
            out = new FileOutputStream("img/mm2.jpg");
            // 通过输入输出流获得通道
            inChannel = in.getChannel();
            outChannel = out.getChannel();

            // 构建缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (inChannel.read(buffer) != -1) { // 从输入通道中读取内容到缓冲区
                buffer.flip(); // 切换成读取数据模式
                outChannel.write(buffer); // 写入缓冲区数据到输出通道
                buffer.clear(); // 清空缓冲区，用于下一次存储
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outChannel.close();
            inChannel.close();
            out.close();
            in.close();
        }
    }

    /**
     * 利用通道和直接缓冲区传输文件（内存映射文件）
     */
    @Test
    public void test2() throws IOException {
        /**
         * open() 方式打开通道
         *
         * StandardOpenOption.READ : 以只读方式创建通道
         * StandardOpenOption.CREATE: 若文件存在覆盖，不存在创建。
         * StandardOpenOption.CREATE_NEW: 若文件不存在创建，存在报错。
         */
        FileChannel inChannel = FileChannel.open(Paths.get("img/mm.jpg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("img/mm2.jpg"), StandardOpenOption.READ,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        // 构建内存映射文件（数据在物理内存中的映射，也是直接缓冲区）
        MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        // 对直接缓冲区进行数据读取
        System.out.println("byte:" + inMappedBuf.limit() + "k");
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);

        inChannel.close();
        outChannel.close();
    }

    /**
     * 直接利用通道进行传输，将源通道中的数据传输到目标通道中去。
     */
    @Test
    public void test3() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("img/mm.jpg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("img/mm2.jpg"), StandardOpenOption.READ,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        inChannel.transferTo(0, inChannel.size(), outChannel); // 把输入管道的数据传到输出管道中去
//        outChannel.transferFrom(inChannel,0,inChannel.size()); // 从输入管道中传输数据到输出管道中来

        outChannel.close();
        inChannel.close();
    }
}
