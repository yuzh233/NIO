package subject_1;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: yu_zh
 * @DateTime: 2018/09/05 9:52
 * <p>
 * 随机访问文件类的demo
 */
public class TestRandomAccessFile {

    /**
     * 读取任意位置的文件内容
     * <p>
     * r 代表以只读方式打开指定文件
     * rw 以读写方式打开指定文件
     * rws 读写方式打开，并对内容或元数据都同步写入底层存储设备
     * rwd 读写方式打开，对文件内容的更新同步更新至底层存储设备
     */
    @Test
    public void randomReadFile() throws IOException {
        RandomAccessFile read = new RandomAccessFile("src/subject_1/TestBuffer.java", "r");
        System.out.println("文件指针初始位置：" + read.getFilePointer());
        read.seek(1000); // 将指针定位到某个位置
        FileChannel readChannel = read.getChannel();  // 从指定位置开始读取，获得通道
        ByteBuffer buffer = ByteBuffer.allocate(1024); // 创建缓冲区，可以将容量设为通道大小`(int) readChannel.size()`这样就不用循环读取

        // 控制台打印的同时还将数据传输到输出通道
        RandomAccessFile write = new RandomAccessFile("src/subject_1/TestBuffer.txt", "rw");
        FileChannel writeChannel = write.getChannel();
        while (readChannel.read(buffer) != -1) { // 不断的将输入通道中的数据读取到缓冲区
            buffer.flip(); // 开启读取模式，读取完毕之后 position 到了数据末尾，进行下一次读取。
            System.out.println(new String(buffer.array(), 0, buffer.limit()));
            writeChannel.write(buffer);
            buffer.clear();
        }
    }

    /**
     * 追加数据到文件的指定位置
     */
    @Test
    public void randomWriteFile() throws IOException {
        RandomAccessFile write = new RandomAccessFile("src/subject_1/TestBuffer.txt", "rw");
        write.seek(write.length()); // 将文件指针移动到末尾
        write.write("---------< this is append content >--------".getBytes());
    }

    /**
     * 在任意处插入数据，如果不将插入点之后的数据备份的话，插入的数据会覆盖插入点之后的数据。
     * <p>
     * - 将插入点之后的数据存入临时文件
     * - 将数据插入到插入点
     * - 将临时文件中的内容插入到旧文件
     */
    @Test
    public void randomReadWriteFile() throws IOException {
        File tempFile = File.createTempFile("src/subject_1/tmp", "txt", null);
        tempFile.deleteOnExit(); // JVM退出时删除
        // 建立临时文件的输入输出通道
        FileChannel temInChannel = new FileInputStream(tempFile).getChannel();
        FileChannel temOutChannel = new FileOutputStream(tempFile).getChannel();

        RandomAccessFile target = new RandomAccessFile("src/subject_1/target.txt", "rw");
        int seekIndex = 1210; // 插入点
        target.seek(seekIndex);
        FileChannel targetChannel = target.getChannel();

        // 将目标文件插入点之后的数据创建通道传输到临时文件中的通道
        temOutChannel.transferFrom(targetChannel, 0, targetChannel.size());

        // 指针回到插入点，插入数据。
        target.seek(seekIndex);
        target.write("[ this is append content ]\n".getBytes());

        // 复制插入点之后的数据.读取临时文件通道中的数据，将临时文件通道数据传输到目标文件通道。如果能读取到说明数据传输成功
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        while (temInChannel.read(buffer) != -1) {
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, buffer.limit()));
            targetChannel.write(buffer);
            buffer.clear();
        }
    }
}
