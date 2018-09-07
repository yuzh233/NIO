package subject_1;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/06 11:07
 */
public class TestCharset {

    /**
     * 查看支持的所有字符集
     */
    @Test
    public void test1() {
        SortedMap<String, Charset> map = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> set = map.entrySet();
        Iterator<Map.Entry<String, Charset>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Charset> entry = iterator.next();
            System.out.println(entry.getKey() + " <> " + entry.getValue());
        }
        System.out.println("defaultCharset: " + Charset.defaultCharset());
    }

    /**
     * 编码：字符 -> 字节数组
     * 解码：字节数组 -> 字符
     */
    @Test
    public void test2() throws CharacterCodingException {
        // 以指定编码加载字符集
        Charset gbk = Charset.forName("GBK");
        Charset utf8 = Charset.forName("UTF-8");
        // 创建编码器
        CharsetEncoder gbkEncoder = gbk.newEncoder();
        CharsetEncoder utf8Encoder = utf8.newEncoder();
        // 创建解码器
        CharsetDecoder utf8Decoder = utf8.newDecoder();
        CharsetDecoder gbkDecoder = gbk.newDecoder();
        // 将数据放到缓冲区
        CharBuffer buffer = CharBuffer.allocate(1024);
        buffer.put("测试编码解码");

        // 对字符使用 GBK 编码，切换到读模式，指针从0开始。
        buffer.flip();
        ByteBuffer byteBuffer = gbkEncoder.encode(buffer);
        // ByteBuffer byteBuffer = utf8Encoder.encode(buffer); // 使用 UTF8 编码
        for (int i = 0; i < byteBuffer.limit(); i++) {
            System.out.println(byteBuffer.get());
        }

        // 使用 GBK 解码
        byteBuffer.flip();
        CharBuffer cb = gbkDecoder.decode(byteBuffer);
        System.out.println(new String(cb.array(), 0, cb.limit()));

        // 使用 UTF8 解码，失败
        byteBuffer.flip();
        CharBuffer cb1 = utf8Decoder.decode(byteBuffer);
        System.out.println(new String(cb1.array(), 0, cb1.limit()));
    }
}
