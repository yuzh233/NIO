> Java Non blocking IO 学习笔记

<!-- TOC -->

- [Java NIO 简介](#java-nio-简介)
- [Java NIO 与 IO 的主要区别](#java-nio-与-io-的主要区别)
- [缓冲区(Buffer)和通道(Channel)](#缓冲区buffer和通道channel)
    - [缓冲区](#缓冲区)
    - [通道](#通道)
- [文件通道(FileChannel)](#文件通道filechannel)
    - [RandomAccessFile 类](#randomaccessfile-类)
    - [分散和聚集](#分散和聚集)
    - [字符集 Charset](#字符集-charset)
- [NIO 的非阻塞式网络通信](#nio-的非阻塞式网络通信)
    - [选择器(Selector)](#选择器selector)
    - [SocketChannel、ServerSocketChannel、DatagramChannel](#socketchannelserversocketchanneldatagramchannel)
- [管道(Pipe)](#管道pipe)
- [Java NIO2 (Path、Paths 与 Files )](#java-nio2-pathpaths-与-files-)
- [自动资源管理](#自动资源管理)

<!-- /TOC -->

# Java NIO 简介
 Java NIO（Non blocking IO）是从Java 1.4版本开始引入的一个新的IO API，可以替代标准的Java IO API。NIO与原来的IO有同样的作用和目的，但是使用的方式完全不同，NIO支持面向缓冲区的、基于通道的IO操作。NIO将以更加高效的方式进行文件的读写操作。

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

## 字符集 Charset
```java
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
```
# NIO 的非阻塞式网络通信
> 传统的 IO 流都是阻塞式的。也就是说，当一个线程调用 read() 或 write() 时，该线程被阻塞，直到有一些数据被读取或写入，该线程在此期间不能执行其他任务。因此，在完成网络通信进行 IO 操作时，由于线程会阻塞，所以服务器端必须为每个客户端都提供一个独立的线程进行处理，当服务器端需要处理大量客户端时，性能急剧下降。<br>
Java NIO 是非阻塞模式的。当线程从某通道进行读写数据时，若没有数据可用时，该线程可以进行其他任务。线程通常将非阻塞 IO 的空闲时间用于在其他通道上执行 IO 操作，所以单独的线程可以管理多个输入和输出通道。因此，NIO 可以让服务器端使用一个或有限几个线程来同时处理连接到服务器端的所有客户端。

通过用例代码验证阻塞式网络通信：
```java
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
```
## 选择器(Selector)
选择器（Selector） 是 SelectableChannle 对象的多路复用器，Selector 可以同时监控多个 SelectableChannel 的 IO 状况，也就是说，利用 Selector 可使一个单独的线程管理多个Channel。Selector 是非阻塞 IO 的核心。

    java.nio.channels.Channel 接口：
  			|--SelectableChannel
  				|--SocketChannel
  				|--ServerSocketChannel
  				|--DatagramChannel
  
  				|--Pipe.SinkChannel
  				|--Pipe.SourceChannel

非阻塞式IO及选择器的用例说明：
```java
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
```

## SocketChannel、ServerSocketChannel、DatagramChannel
Java NIO中的SocketChannel是一个连接到TCP网络套接字的通道。操作步骤：

- 打开 SocketChannel
- 读写数据
- 关闭 SocketChannel

Java NIO中的 ServerSocketChannel 是一个可以监听新进来的TCP连接的通道，就像标准IO中的ServerSocket一样。

Java NIO中的DatagramChannel是一个能收发UDP包的通道。操作步骤：

- 打开 DatagramChannel
- 接收/发送数据

# 管道(Pipe)
Java NIO 管道是2个线程之间的单向数据连接。Pipe有一个source通道和一个sink通道。数据会被写到sink通道，从source通道读取。

```java
public class PipTest {
    @Test
    public void test1() throws IOException {
        Pipe pipe = Pipe.open(); // 获取管道
        ByteBuffer buf = ByteBuffer.allocate(1024); // 将缓冲区中的数据写入管道

        Pipe.SinkChannel sinkChannel = pipe.sink();
        buf.put("通过单向管道发送数据".getBytes());
        buf.flip();
        sinkChannel.write(buf);

        Pipe.SourceChannel sourceChannel = pipe.source(); // 读取缓冲区中的数据
        buf.flip();
        int len = sourceChannel.read(buf);
        System.out.println(new String(buf.array(), 0, len));

        sourceChannel.close();
        sinkChannel.close();
    }
}
```

# Java NIO2 (Path、Paths 与 Files )
java.nio.file.Path 接口代表一个平台无关的平台路径，描述了目录结构中文件的位置。

Paths 提供的 get() 方法用来获取 Path 对象：

    Path get(String first, String … more) : 用于将多个字符串串连成路径。

|方法|描述|
|-----|-----|
|boolean endsWith(String path)| 判断是否以 path 路径结束|
|boolean startsWith(String path)| 判断是否以 path 路径开始|
|boolean isAbsolute()| 判断是否是绝对路径|
|Path getFileName()| 返回与调用 Path 对象关联的文件名|
|Path getName(int idx) | 返回的指定索引位置 idx 的路径名称|
|int getNameCount() | 返回Path 根目录后面元素的数量|
|Path getParent() |返回Path对象包含整个路径，不包含 Path 对象指定的文件路径|
|Path getRoot() |返回调用 Path 对象的根路径|
|Path resolve(Path p) |将相对路径解析为绝对路径|
|Path toAbsolutePath() | 作为绝对路径返回调用 Path 对象|
|String toString() | 返回调用 Path 对象的字符串表示形式|

java.nio.file.Files 用于操作文件或目录的工具类。

|方法|描述|
|-----|-----|
|Path copy(Path src, Path dest, CopyOption … how) | 文件的复制|
|Path createDirectory(Path path, FileAttribute<?> … attr) | 创建一个目录|
|Path createFile(Path path, FileAttribute<?> … arr) | 创建一个文件|
|void delete(Path path) | 删除一个文件|
|Path move(Path src, Path dest, CopyOption…how) | 将 src 移动到 dest 位置|
|long size(Path path) | 返回 path 指定文件的大小|
|boolean exists(Path path, LinkOption … opts) |判断文件是否存在|
|boolean isDirectory(Path path, LinkOption … opts) |判断是否是目录|
|boolean isExecutable(Path path)|判断是否是可执行文件|
|boolean isHidden(Path path) | 判断是否是隐藏文件|
|boolean isReadable(Path path) | 判断文件是否可读|
|boolean isWritable(Path path) | 判断文件是否可写|
|boolean notExists(Path path, LinkOption … opts) | 判断文件是否不存在|
|SeekableByteChannel newByteChannel(Path path, OpenOption…how) | 获取与指定文件的连接|
| DirectoryStream newDirectoryStream(Path path) | 打开 path 指定的目录|
|InputStream newInputStream(Path path, OpenOption…how)|获取 InputStream 对象|
|OutputStream newOutputStream(Path path, OpenOption…how) | 获取 OutputStream 对象|

# 自动资源管理
> Java 7 增加了一个新特性，该特性提供了另外一种管理资源的方式，这种方式能自动关闭文件。这个特性有时被称为自动资源管理(Automatic Resource Management, ARM)， 该特性以 try 语句的扩展版为基础。自动资源管理主要用于，当不再需要文件（或其他资源）时，可以防止无意中忘记释放它们。

语法：

    try( 需要关闭的资源声明 ){
        //可能发生异常的语句
    }catch( 异常类型 变量名 ){
        //异常的处理语句
    }
    ……
    finally{
        //一定执行的语句
    }

当 try 代码块结束时，自动释放资源。因此不需要显示的调用 close() 方法。该形式也称为“带资源的 try 语句”。注意：

- try 语句中声明的资源被隐式声明为 final ，资源的作用局限于带资源的 try 语句
- 可以在一条 try 语句中管理多个资源，每个资源以“;” 隔开即可。
- 需要关闭的资源，必须实现了 AutoCloseable 接口或其自接口 Closeable

```java
public class TestNIO_2 {
	//自动资源管理：自动关闭实现 AutoCloseable 接口的资源
	@Test
	public void test(){
		try(FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
				FileChannel outChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)){
			
			ByteBuffer buf = ByteBuffer.allocate(1024);
			inChannel.read(buf);
			
		}catch(IOException e){}
	}
}
```