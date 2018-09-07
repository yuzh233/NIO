package subject_2;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/06 13:55
 * <p>
 * 阻塞式网络通信
 */
public class BlockingNioTest {
    /**
     * 客户端：发送数据
     */
    @Test
    public void client() throws IOException, InterruptedException {
        // 打开网络IO通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 1998));
        // 读模式打开本地传输文件通道
        FileChannel inChannel = FileChannel.open(Paths.get("img/mm.jpg"), StandardOpenOption.READ);
        // 传输
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (inChannel.read(buffer) != -1) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        /**
         * 接收服务端反馈信息
         * 调用 read()/write() 方法当前线程会进入阻塞状态，如果不强制停止write，不会进行下面的操作。
         */
        socketChannel.shutdownOutput();
        while (socketChannel.read(buffer) != -1) {
            System.out.println("接收服务器反馈中...");
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, buffer.limit()));
            buffer.clear();
        }
        inChannel.close();
        socketChannel.close();
    }

    /**
     * 服务器端：接收数据
     */
    @Test
    public void server() throws IOException {
        // 打开网络IO通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        // 绑定连接，进入阻塞状态
        ssChannel.bind(new InetSocketAddress("localhost", 1998));
        // 获取客户端的连接通道
        System.out.println("服务端等待获取客户端连接通道...");
        SocketChannel socketChannel = ssChannel.accept();
        System.out.println("成功获取到客户端连接通道！");
        // 从客户端连接通道读取数据到本地
        FileChannel outChannel = FileChannel.open(Paths.get("src/subject_2/mm.jpg"), StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        // 传输
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (socketChannel.read(buffer) != -1) {
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
        }
        /*发送反馈信息给客户端*/
        buffer.put("[服务端]：消息接收成功！".getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        outChannel.close();
        socketChannel.close();
        ssChannel.close();
    }
}
