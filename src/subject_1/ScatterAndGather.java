package subject_1;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/05 9:37
 */
public class ScatterAndGather {

    /**
     * 分散读取与聚集写入
     */
    @Test
    public void test1() throws IOException {
        RandomAccessFile file = new RandomAccessFile("src/subject_1/target.txt", "rw");
        FileChannel channel = file.getChannel();
        // 分配两个缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate((int) channel.size() - 100);
        // 分散读取到缓冲区
        ByteBuffer[] buffers = {buf1, buf2};
        channel.read(buffers); // 将输入通道的中的数据依次分散读取到不同的缓冲区
        for (ByteBuffer buffer : buffers) {
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, buffer.limit()));
            System.out.println("-----< 分割 >-----");
        }

        // 聚集写入到文件
        FileChannel bakChannel = new RandomAccessFile("src/subject_1/target_bak.txt", "rw").getChannel();
        bakChannel.write(buffers);
    }
}
