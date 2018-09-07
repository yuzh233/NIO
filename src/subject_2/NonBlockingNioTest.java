package subject_2;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/07 10:15
 * <p>
 * 非阻塞式网络通信
 */
public class NonBlockingNioTest {
    @Test
    public void client() throws IOException {
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("localhost", 1998));
        sChannel.configureBlocking(false); //切换非阻塞模式
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put((new Date().toString()).getBytes());
        buf.flip();
        sChannel.write(buf);
        buf.clear();
        sChannel.close();
    }

    @Test
    public void server() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.configureBlocking(false); // 服务端通道切换非阻塞模式
        ssChannel.bind(new InetSocketAddress(1998));

        Selector selector = Selector.open(); // 获取选择器
        /*
         * 将服务器通道注册到选择器上, 并且指定“监听接收事件”。
         * - 读: SelectionKey.OP_READ
         * - 写: SelectionKey.OP_WRITE
         * - 连接: SelectionKey.OP_CONNECT
         * - 接收: SelectionKey.OP_ACCEPT
         */
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 轮询式的获取选择器上已经“准备就绪”的事件
        while (selector.select() > 0) {
            // 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey sk = it.next(); // 获取准备“就绪”的事件
                if (sk.isAcceptable()) { // 若事件是 “接收就绪”，获取客户端连接
                    SocketChannel sChannel = ssChannel.accept();
                    sChannel.configureBlocking(false); // 客户端切换非阻塞模式
                    sChannel.register(selector, SelectionKey.OP_READ); // 将客户端通道注册到选择器上

                } else if (sk.isReadable()) { // 获取当前选择器上“读就绪”状态的通道
                    SocketChannel sChannel = (SocketChannel) sk.channel();
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = sChannel.read(buf)) > 0) {
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len));
                        buf.clear();
                    }
                }
                it.remove(); // 取消选择键 SelectionKey
            }
        }
    }
}
