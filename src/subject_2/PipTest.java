package subject_2;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/07 10:59
 */
public class PipTest {
    @Test
    public void test1() throws IOException {
        Pipe pipe = Pipe.open(); // 获取管道
        ByteBuffer buf = ByteBuffer.allocate(1024); // 写数据线程：将缓冲区中的数据写入管道

        Pipe.SinkChannel sinkChannel = pipe.sink();
        buf.put("通过单向管道发送数据".getBytes());
        buf.flip();
        sinkChannel.write(buf);

        Pipe.SourceChannel sourceChannel = pipe.source(); // 读数据线程：读取缓冲区中的数据
        buf.flip();
        int len = sourceChannel.read(buf);
        System.out.println(new String(buf.array(), 0, len));

        sourceChannel.close();
        sinkChannel.close();
    }
}
