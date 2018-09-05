package subject_1;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/04 13:24
 */
public class TestBuffer {
    @Test
    public void test() {
        // 分配一个指定容量的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("-----< allocate >-----");
        showAttribute(buffer); // position:0 - limit:1024 - capacity:1024

        // 存数据到缓冲区
        buffer.put("abcde".getBytes()); // 存入了5个字节
        System.out.println("-----< put >-----");
        showAttribute(buffer); // position:5 - limit:1024 - capacity:1024

        // 切换到读取数据，将缓冲区的界限设置为当前位置（5），并将当前位置充值为 0（从0开始读取）
        buffer.flip();
        System.out.println("-----< flip >-----");
        showAttribute(buffer); // position:从 0 开始读 - limit:5 - capacity:1024

        // 这个时候如果存入数据会报错，因为指针在0，界限是5，而存入的数据长度10大于界限。
//        buffer.put("zhangyu123".getBytes());

        // 取数据
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(new String(bytes, 0, bytes.length));
        System.out.println("-----< get >-----");
        showAttribute(buffer); // position:5 - limit:5 - capacity:1024

        // 重复读取
        buffer.rewind();
        System.out.println("-----< rewind >-----");
        showAttribute(buffer); // position:0 - limit:5 - capacity:1024

        // 清空缓存区，缓存区的数据依旧存在，只是position到了0，limit和capacity变为最大容量数值，数据处于游离状态。
        buffer.clear();
        System.out.println("-----< clear >-----");
        showAttribute(buffer); // position:0 - limit:1024 - capacity:1024
    }

    @Test
    public void testMark() {
        String s = "abcde";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(s.getBytes());
        buffer.flip();

        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes, 0, 2); // 从缓冲区获取从0开始的两个字符
        System.out.println(new String(bytes, 0, bytes.length)); // ab
        System.out.println("position:" + buffer.position()); // 2

        buffer.mark(); // 对位置 2 做标记，继续往下读。

        buffer.get(bytes, 2, 2);
        System.out.println(new String(bytes, 0, bytes.length)); // abcd
        System.out.println("position:" + buffer.position()); // 4

        buffer.reset(); // position 重新恢复到了 2
        System.out.println("position:" + buffer.position()); // 2

        System.out.println((char) buffer.get()); // 获取下一个字节 c
        if (buffer.hasRemaining()) System.out.println(buffer.remaining()); // 获得剩余字节数量 2
    }

    @Test
    public void test3() {
        //分配直接缓冲区
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        System.out.println(buf.isDirect());
    }

    public void showAttribute(ByteBuffer buffer) {
        System.out.println("position:" + buffer.position());
        System.out.println("limit:" + buffer.limit());
        System.out.println("capacity:" + buffer.capacity());
    }
}
