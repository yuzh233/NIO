> JDK1.4 Java New IO 学习笔记

<!-- TOC -->

- [Java NIO 简介](#java-nio-简介)
- [Java NIO 与 IO 的主要区别](#java-nio-与-io-的主要区别)
- [缓冲区(Buffer)和通道(Channel)](#缓冲区buffer和通道channel)
    - [缓冲区](#缓冲区)
    - [通道](#通道)
- [文件通道(FileChannel)](#文件通道filechannel)
    - [RandomAccessFile 类](#randomaccessfile-类)
    - [分散和聚集](#分散和聚集)
- [NIO 的非阻塞式网络通信](#nio-的非阻塞式网络通信)
    - [选择器(Selector)](#选择器selector)
    - [SocketChannel、ServerSocketChannel、DatagramChannel](#socketchannelserversocketchanneldatagramchannel)
- [管道(Pipe)](#管道pipe)
- [Java NIO2 (Path、Paths 与 Files )](#java-nio2-pathpaths-与-files-)

<!-- /TOC -->

# Java NIO 简介
> Java NIO（New IO）是从Java 1.4版本开始引入的一个新的IO API，可以替代标准的Java IO API。NIO与原来的IO有同样的作用和目的，但是使用的方式完全不同，NIO支持面向缓冲区的、基于通道的IO操作。NIO将以更加高效的方式进行文件的读写操作。

# Java NIO 与 IO 的主要区别

|IO|NIO|
|-----|-----|
|面向流(Stream Oriented)| 面向缓冲区(Buffer Oriented)|
|阻塞IO(Blocking IO)| 非阻塞IO(Non Blocking IO)|
|(无)| 选择器(Selectors)|

# 缓冲区(Buffer)和通道(Channel)
> Java NIO系统的核心在于：通道(Channel)和缓冲区(Buffer)。通道表示打开到 IO 设备(例如：文件、套接字)的连接。若需要使用 NIO 系统，需要获取用于连接 IO 设备的通道以及用于容纳数据的缓冲区。然后操作缓冲区，对数据进行处理。简而言之，Channel 负责传输， Buffer 负责存储。

## 缓冲区
缓冲区（Buffer）：一个用于特定基本数据类型的容器。由 java.nio 包定义的，所有缓冲区都是 Buffer 抽象类的子类。

Java NIO 中的 Buffer 主要用于与 NIO 通道进行交互，数据是从通道读入缓冲区，从缓冲区写入通道中的。

Buffer 就像一个数组，可以保存多个相同类型的数据。根据数据类型不同(boolean 除外) ，有以下 Buffer 常用子类： 

`ByteBuffer` / `CharBuffer` / `ShortBuffer` / `IntBuffer` / `LongBuffer` / `FloatBuffer` / `DoubleBuffer`

上述 Buffer 类 他们都采用相似的方法进行管理数据，只是各自管理的数据类型不同而已。都是通过如下方法获取一个 Buffer 对象：

    static XxxBuffer allocate(int capacity) : 创建一个容量为 capacity 的 XxxBuffer 对象

基本属性：

- 容量 (capacity) ：表示 Buffer 最大数据容量，缓冲区容量不能为负，并且创建后不能更改。

- 限制 (limit)：第一个不应该读取或写入的数据的索引，即位于 limit 后的数据不可读写。缓冲区的限制不能为负，并且不能大于其容量。

- 位置 (position)：下一个要读取或写入的数据的索引。缓冲区的位置不能为负，并且不能大于其限制

- 标记 (mark)与重置 (reset)：标记是一个索引，通过 Buffer 中的 mark() 方法指定 Buffer 中一个特定的position，之后可以通过调用 reset() 方法恢复到这个 position.
 
- 标记、位置、限制、容量遵守以下不变式： 0 <= mark <= position <= limit <= capacity

![Alt text](/img/1.jpg)

常用方法：

|方 法| 描 述|
|-----|-----|
|byte get() |读取单个字节|
|Buffer put(byte b) |写入单个字节到缓冲区|
|Buffer clear()| 清空缓冲区并返回对缓冲区的引用|
|Buffer flip()|将缓冲区的界限设置为当前位置，并将当前位置充值为 0|
|int capacity()| 返回 Buffer 的 capacity 大小|
|boolean hasRemaining()| 判断缓冲区中是否还有元素|
|int limit()| 返回 Buffer 的界限(limit) 的位置|
|Buffer limit(int n)| 将设置缓冲区界限为 n, 并返回一个具有新 limit 的缓冲区对象|
|Buffer mark()| 对缓冲区设置标记|
|int position()| 返回缓冲区的当前位置 position|
|Buffer position(int n)| 将设置缓冲区的当前位置为 n , 并返回修改后的 Buffer 对象|
|int remaining()| 返回 position 和 limit 之间的元素个数|
|Buffer reset()| 将位置 position 转到以前设置的 mark 所在的位置|
|Buffer rewind()| 将位置设为为 0， 取消设置的 mark|

用例：
```java
public class TestBuffer {
    @Test
    public void test() {
        // 分配一个指定容量的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("-----< allocate >-----");
        System.out.println("position:" + buffer.position()); // 0
        System.out.println("limit:" + buffer.limit()); // 1024
        System.out.println("capacity:" + buffer.capacity()); // 1024

        // 存数据到缓冲区
        buffer.put("abcde".getBytes()); // 存入了5个字节
        System.out.println("-----< put >-----");
        System.out.println("position:" + buffer.position()); // 5
        System.out.println("limit:" + buffer.limit()); // 1024
        System.out.println("capacity:" + buffer.capacity()); // 1024

        // 切换到读取数据，将缓冲区的界限设置为当前位置（5），并将当前位置充值为 0（从0开始读取）
        buffer.flip();
        System.out.println("-----< flip >-----");
        System.out.println("position:" + buffer.position()); // 从 0 开始读
        System.out.println("limit:" + buffer.limit()); // 5
        System.out.println("capacity:" + buffer.capacity()); // 1024

        // 这个时候如果存入数据会报错，因为指针在0，界限是5，而存入的数据长度10大于界限。
//        buffer.put("zhangyu123".getBytes());

        // 取数据
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(new String(bytes, 0, bytes.length));
        System.out.println("-----< get >-----");
        System.out.println("position:" + buffer.position()); // 5
        System.out.println("limit:" + buffer.limit()); // 5
        System.out.println("capacity:" + buffer.capacity()); // 1024

        // 重复读取
        buffer.rewind();
        System.out.println("-----< rewind >-----");
        System.out.println("position:" + buffer.position()); // 0
        System.out.println("limit:" + buffer.limit()); // 5
        System.out.println("capacity:" + buffer.capacity()); // 1024

        // 清空缓存区，缓存区的数据依旧存在，只是position到了0，limit和capacity变为最大容量数值，数据处于游离状态。
        buffer.clear();
        System.out.println("-----< clear >-----");
        System.out.println("position:" + buffer.position()); // 0
        System.out.println("limit:" + buffer.limit()); // 1024
        System.out.println("capacity:" + buffer.capacity()); // 1024
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
}
```
非直接缓冲区：通过 allocate() 方法分配缓冲区，将缓冲区建立在 JVM 的内存中。
 
直接缓冲区：通过 allocateDirect() 方法分配直接缓冲区，将缓冲区建立在物理内存中。可以提高效率。弊端：直接在系统内存中建立（分配、销毁）缓存比在JVM中建立缓存内存消耗要大,并且当我们的应用程序将数据写入到系统内存之后数据是不可控的（不归应用程序管了），缓存中的数据什么写入到磁盘由操作系统决定。使用直接缓冲区最好的场景是当我们需要长时间在缓存中操作数据或者大量的数据时，此时的效率会得到很大的提高。

## 通道
 通道（Channel）：由 java.nio.channels 包定义的。Channel 表示 IO 源与目标打开的连接。Channel 类似于传统的“流”。只不过 Channel 本身不能直接访问数据，Channel 只能与Buffer 进行交互。

主要实现类：

    java.nio.channels.Channel 接口：
  		|--FileChannel
  		|--SocketChannel
  		|--ServerSocketChannel
  		|--DatagramChannel

获取通道的几种方式：

1. Java 针对支持通道的类提供了 getChannel() 方法

  		本地 IO：
  		FileInputStream/FileOutputStream
  		RandomAccessFile
  
 		网络IO：
  		Socket
  		ServerSocket
  		DatagramSocket
  		
2. 在 JDK 1.7 中的 NIO.2 针对各个通道提供了静态方法 open()
3. 在 JDK 1.7 中的 NIO.2 的 Files 工具类的 newByteChannel()

# 文件通道(FileChannel)
FileChannel 的常用方法：

|方 法| 描 述|
|-----|-----|
|int read(ByteBuffer dst) |从 Channel 中读取数据到 ByteBuffer|
|long read(ByteBuffer[] dsts)| 将 Channel 中的数据“分散”到 ByteBuffer[]|
|int write(ByteBuffer src) |将 ByteBuffer 中的数据写入到 Channel|
|long write(ByteBuffer[] srcs)| 将 ByteBuffer[] 中的数据“聚集”到 Channel|
|long position()| 返回此通道的文件位置|
|FileChannel position(long p) |设置此通道的文件位置|
|long size()| 返回此通道的文件的当前大小|
|FileChannel truncate(long s)| 将此通道的文件截取为给定大小|
|void force(boolean metaData)| 强制将所有对此通道的文件更新写入到存储设备中|

以文件传输体验通道的数据传输：
```java
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
```
## RandomAccessFile 类

在某些应用场景，我们需要在一个数据流中的末尾插入数据，通常的方式是先把文件全部读取出来，再用插入的数据拼接到末尾，然后重新写入。但是当数据量特别大时，内存占用会特别的高甚至溢出。

> RandomAccessFile是Java中输入，输出流体系中功能最丰富的文件内容访问类，它提供很多方法来操作文件，包括读写支持，与普通的IO流相比，它最大的特别之处就是支持任意访问的方式，程序可以直接跳到任意地方来读写数据。
如果我们只希望访问文件的部分内容，而不是把文件从头读到尾，使用RandomAccessFile将会带来更简洁的代码以及更好的性能。

通过用例练习RandomAccessFile：
```java
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
```
## 分散和聚集
分散读取（Scattering Reads）是指从 Channel 中读取的数据“分散”到多个 Buffer 中（按照缓冲区的顺序，从 Channel 中读取的数据依次将 Buffer 填满）。

![Alt text](/img/2.jpg)

聚集写入（Gathering Writes）是指将多个 Buffer 中的数据“聚集”到 Channel（按照缓冲区的顺序，写入 position 和 limit 之间的数据到 Channel）。

![Alt text](/img/3.jpg)

```java
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
```


# NIO 的非阻塞式网络通信
## 选择器(Selector)
## SocketChannel、ServerSocketChannel、DatagramChannel
# 管道(Pipe)
# Java NIO2 (Path、Paths 与 Files )