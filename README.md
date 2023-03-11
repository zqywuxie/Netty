# Netty

## 介绍

Netty 是一个异步事件驱动的网络应用框架，用于快速开发可维护的高性能服务器和客户端。(Netty 封装了 JDK 的 NIO)

传统IO的缺点和NIO的改进:

- 一个连接一个线程，大量线程读不到线程处于阻塞状态，浪费系统资源

- 线程的切换会带来额外的开销

改：多个连接注册到selector，selector负责轮询；有数据就通知工作线程进行批量处理数据；减少线程切换，提高效率

- 读写面向字节流，读完不可读。

改：NIO提供的buffer可以移动指针随意读取想要的数据。

Netty与NIO的比较

1. NIO操作复杂，概念较多
2. Netty**底层IO模型可以随意切换**，更改参数可以从NIO切换到IO模型
3. Netty**自带拆包解包**(网络通讯知识)，异常检测等机制，使使用者关注业务逻辑
4. Netty解决了JDK的很多包括空轮询在内的Bug
5. Netty 底层对线程，selector 做了很多细小的优化，精心设计的 reactor 线程模型做到非常高效的并发处理
6. 自带各种协议栈让你处理任何一种通用协议都几乎不用亲自动手
7. Netty 已经历各大 RPC 框架，消息中间件，分布式通信中间件线上的广泛验证，健壮性无比强大

### 简单使用

1、准备工作，引入依赖

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.6.Final</version>
</dependency>
```



2、入门案例

> NettyServer.java

```java
public class NettyServer {
public static void main(String[] args) {
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    NioEventLoopGroup boss = new NioEventLoopGroup();
    NioEventLoopGroup worker = new NioEventLoopGroup();
    serverBootstrap
            .group(boss, worker)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                protected void initChannel(NioSocketChannel ch) {
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                            System.out.println(msg);
                        }
                    });
                }
            })
            .bind(8000);
}
}

```

解释：

1. `boss` 用于接受新连接线程，创建新连接
2. `worker` 负责读取数据的线程，读取数据和业务逻辑处理



> NettyClient.java

```java
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                });

        Channel channel = bootstrap.connect("127.0.0.1", 8000).channel();

        while (true) {
            channel.writeAndFlush(new Date() + ": hello world!");
            Thread.sleep(2000);
        }
    }
}

```



解释：

1. `group` 对应main函数中起的线程



## 使用

### 1、服务端启动流程

```java
public class NettyServer {
public static void main(String[] args) {
    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    NioEventLoopGroup workGroup = new NioEventLoopGroup();
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
            .group(bossGroup,workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                protected void initChannel(NioSocketChannel channel){

                }
            });
    serverBootstrap.bind(8000);
}
}
```

> 解释说明:

- `bossGroup` 前面介绍进行连接新线程，  `workGroup` 进行数据的处理和业务逻辑；打比方：`bossGroup` 老板进行接活，`workGroup` 员工进行干活；

- `serverBootStrap` 引导类，进行服务端的启动工作

- `.group(bossGroup,workGroup)` 配置两大线程组

- `.channel(NioServerSocketChannel.class)` 配置IO模型为`NIO`

- `.childHandler`  创建一个`ChannelInitializer` ，定义每条连接的数据读写，业务处理逻辑。`NioSocketChannel`泛型，对NIO类型连接的抽象，前者`NioServerSocketChannel` 也是对NIO类型连接的抽象。对应BIO编程模型的``ServerSocket`  以及`Socket`

  - 补充：

    - BIO（Blocking IO） 同步阻塞IO

    socket可以使一个应用从网络中读取和写入数据，不同计算机上的两个应用可以通过连接发送和接受[字节流](https://so.csdn.net/so/search?q=字节流&spm=1001.2101.3001.7020)，当发送消息时，你需要知道对方的ip和端口，在java中，socket指的是java.net.Socket类。 

    ServerSocket与Socket不同，ServerSocket是等待客户端的请求，一旦获得一个连接请求，就创建一个Socket示例来与客户端进行通信。 

- `serverBootstrap.bind(8000);` 它是一个异步的方法，调用之后是立即返回的，他的返回值是一个`ChannelFuture`，我们可以给这个`ChannelFuture`添加一个监听器`GenericFutureListener`，然后我们在`GenericFutureListener`的`operationComplete`方法里面，我们可以监听端口是否绑定成功

```java
serverBootstrap.bind(8000).addListener(new GenericFutureListener<Future<? super Void>>() {
    public void operationComplete(Future<? super Void> future) {
        if (future.isSuccess()) {
            System.out.println("端口绑定成功!");
        } else {
            System.err.println("端口绑定失败!");
        }
    }
});
```

> 设置自动递归绑定端口

将绑定端口的内容进行提取

```java
private static void bind(ServerBootstrap serverBootstrap,final int port){
    serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
        @Override
        public void operationComplete(Future<? super Void> future) throws Exception {
            if (future.isSuccess()){
                System.out.println("端口["+port+"]绑定成功");
            } else {
                System.out.println("端口["+port+"]绑定失败");
                
                
                //绑定失败进行 端口+1 继续绑定
                bind(serverBootstrap,port+1);
            }
        }
    });
}
```





> 服务端启动的其他方法



- handler方法

```java
serverBootstrap.handler(new ChannelInitializer<NioServerSocketChannel>() {
    protected void initChannel(NioServerSocketChannel ch) {
        System.out.println("服务端启动中");
    }
})
```

可以与前面的`childHandler` 进行对应；child负责处理数据，handler负责业务逻辑；通常用不到



- attr方法

```java
serverBootstrap.attr(AttributeKey.newInstance("serverName"), "nettyServer")
```

attr是用于设置服务端监听通道（Server channel）的选项，比如TCP参数、超时时间等

- childAttr() 方法

```java
serverBootstrap.childAttr(AttributeKey.newInstance("clientKey"), "clientValue")
```

childAttr是用于设置服务端接受客户端连接后创建的通道（Channel）的选项，比如缓冲区大小、心跳检测等。

- childOption() 方法

```java
serverBootstrap
    .childOption(ChannelOption.SO_KEEPALIVE, true)
    .childOption(ChannelOption.TCP_NODELAY, true)
```

`childOption()`可以给每条连接设置一些TCP底层相关的属性，比如上面，我们设置了两种TCP属性，其中

- `ChannelOption.SO_KEEPALIVE`表示是否开启TCP底层心跳机制，true为开启
- `ChannelOption.TCP_NODELAY`表示是否开启Nagle算法，true表示关闭，false表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启。

- option() 方法

```java
serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024)
```

给服务端channel设置一些属性，最常见的就是so_backlog;

表示系统用于**临时存放已完成三次握手的请求的队列的最大长度**，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数



### 2、客户端启动流程

> NettyClient.java

```java
public class NettyClient {
public static void main(String[] args) {
    NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();

    bootstrap
        	// 1.线程模型
            .group(workerGroup)
        	// 2.指定 IO 类型为 NIO
            .channel(NioSocketChannel.class)
        	// 3.IO 处理逻辑
            .handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {

                }
            });

    // 4. 建立连接
    bootstrap.connect("juejin.cn", 80).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功");
        } else {
            System.out.println("连接失败");
        }
    });
}
}
```

客户端的启动类为`Bootstrap`，服务端的启动类为`ServerBootstrap`

流程解释

​	开始的流程都与服务端一直，只不过最后是进行连接地址，服务端是绑定地址。绑定地址也可以像服务端一直进行递归绑定

```java
private static void connect(Bootstrap bootstrap, String host, int port) {
    bootstrap.connect(host, port).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功!");
        } else {
            System.err.println("连接失败，开始重连");
            connect(bootstrap, host, port);
        }
    });
}
==========
connect(bootstrap, "localhost/127.0.0.1", 8000);
```

按照一般业务，失败后并不是立刻连接，而是进行一个等待，设置一个指数退避的方式，并且到达一定的次数就进行放弃；

```java
private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
    bootstrap.connect(host, port).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功!");
        } else if (retry == 0) {
            System.err.println("重试次数已用完，放弃连接！");
        } else {
            // 第几次重连
            int order = (MAX_RETRY - retry) + 1;
            // 本次重连的间隔
            int delay = 1 << order;
            System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
            bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
        }
    });
}
```

> 方法解读

```java
bootstrap.config().group().schedule
=============config 返回启动项的配置参数
public final BootstrapConfig config() {
    return this.config;
}
=============group 返回绑定的线程模型
public final EventLoopGroup group() {
    return this.bootstrap.group();
}
=============schedule 设置定时任务
ScheduledFuture<?> schedule(Runnable var1, long var2, TimeUnit var4);
```



> 客户端启动其他的方法

1. ### attr() 方法

```java
bootstrap.attr(AttributeKey.newInstance("clientName"), "nettyClient")
```

同服务端一样，为NioServerSocketChannel添加自定义属性。并且通过`channel.attr()`进行读取

2. ### option() 方法

```java
Bootstrap
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
```

- `ChannelOption.CONNECT_TIMEOUT_MILLIS` 表示连接的超时时间，超过这个时间还是建立不上的话则代表连接失败。同我们自定义的connect的超时一样。
- `ChannelOption.SO_KEEPALIVE` 表示是否开启 TCP 底层心跳机制，true 为开启
- `ChannelOption.TCP_NODELAY` 表示是否开始 Nagle 算法，true 表示关闭，false 表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就设置为 true 关闭，如果需要减少发送次数减少网络交互，就设置为 false 开启



### 3.实战Ⅰ(单向通信)

#### 客户端发送数据

```java
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new ClientHandler());
    }
```

1. `pipeline` 返回与这条连接相关的逻辑处理链，使用到了责任链模式。

```java
ChannelPipeline pipeline();
```

2. `addLast` 添加一个逻辑处理器，自定义



> ClientHandler.java 逻辑处理代码

```java
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf byteBuf = getByteBuf(ctx);
        ctx.channel().writeAndFlush(byteBuf);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        byte[] bytes = "你好，我们在学习netty".getBytes(Charset.forName("utf-8"));
        buffer.writeBytes(bytes);
        return buffer;
    }

}
```

1. `channelActive`方法 会在客户端连接建立成功之后被调用

2. 首先获得一个 Netty需要的一个数据格式`ByteBuf(字节缓存区)`，`ctx.alloc.buffer` 获得一个ByteBuf的内存管理器，向里面写入内容。通过 `ctx.channel().writeAndFlush`写入到服务端。

#### 服务端读取客户端数据

```java
.childHandler(new ChannelInitializer<NioSocketChannel>() {
    protected void initChannel(NioSocketChannel channel){
        channel.pipeline().addLast(new ServerHandler());
    }
});
```

> ServerHandler.java

```java
public class ServerHandler extends ChannelInboundHandlerAdapter {
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf byteBuf= (ByteBuf) msg;

    System.out.println(new Date()+"：服务端读取到数据 ->"+byteBuf.toString(StandardCharsets.UTF_8));
}
}
```

流程与客户端一致，添加一个逻辑处理器。但不同的则是处理器进行重写`channelRead` 方法，参数`msg` 是Object类型，进行一个强转为`ByteBuf` ，直接`toString`输入即可。



#### 运行结果

先服务端，再客户端，基本顺序。

![image-20230309192243672](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230309192243672.png)

![](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230309192243672.png)



笔者在运行时，报了一个错

```sh
io.netty.channel.socket.nio.NioServerSocketChannel cannot be cast to io.netty.channel.socket.SocketChannel ttr difference
```

原因是客户端`bootstrap` 绑定模型时出错。

```java
.channel(NioSocketChannel.class)
    
/// 手误添加成 NioServerSocketChannel
```



### 4.数据传输载体`ByteBuf`

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/5/1650817a1455afbb~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

`ByteBuf` 通过读指针`readerIndex`，写指针`writerIndex`，容量`capacity`分割战场。

每读取一个字节`readerIndex` 就自增1，写入一个字节`writerIndex`就自增1。可知当两者相等时，`Byte`就不可读。`capacity`当内容不足时就会进行扩容，但大于`maxCapacity`就报错。



#### 容量API（结合图）

> capacity()

表示 ByteBuf 底层占用了多少字节的内存（包括丢弃的字节、可读字节、可写字节），不同的底层实现机制有不同的计算方式

> maxCapacity()

表示 ByteBuf 底层最大能够占用多少字节的内存

> readableBytes() 与 isReadable()

readableBytes 表示可读的字节数。`writerIndex-readerIndex`

isReadable 表示是否可读`writerIndex==readerIndex`

> writableBytes()、 isWritable() 与 maxWritableBytes()

writableBytes 可写字节数 `capacity-writerIndex`

isWritable 是否可写 `writerIndex==readerIndex`

maxWritableBytes 可写最大字节数。`maxCapacity-writerIndex`



#### 读写API

> readerIndex() 与 readerIndex(int)
>
> writeIndex() 与 writeIndex(int)

返回当前读写指针和设置读写指针

> markReaderIndex() 与 resetReaderIndex()

保存当前读指针和回复当前读指针到上一次保存的状态；

以下两个方法一致，推荐后者

```java
// 代码片段1
int readerIndex = buffer.readerIndex();
buffer.readerIndex(readerIndex);


// 代码片段二
buffer.markReaderIndex();
// 不用自己设置值
buffer.resetReaderIndex();
```



> markWriterIndex() 与 resetWriterIndex()

同读指针



> writeBytes(byte[] src) 与 buffer.readBytes(byte[] dst)

将src内容写入ByteBuf，src大小通常小于等于``writableBytes` 

dst 大小通常等于`readableBytes()`

> writeByte(byte b) 与 buffer.readByte()

读写一个字节，类似还有writeBoolean()、writeChar()、writeShort()、writeInt()等

与读写 API 类似的 API 还有 getBytes、getByte() 与 setBytes()、setByte() 系列，**唯一的区别就是 get/set 不会改变读写指针，而 read/write 会改变读写指针**，这点在解析数据的时候千万要注意



> release() 与 retain()

由于 Netty 使用了**堆外内存，而堆外内存是不被 jvm 直接管理的**，也就是说申请到的内存无法被垃圾回收器直接回收，所以需要我们手动回收。有点类似于c语言里面，申请到的内存必须手工释放，否则会造成内存泄漏。

Netty 的 ByteBuf 是通过引用计数的方式管理的，如果一个 ByteBuf 没有地方被引用到，需要回收底层内存。默认情况下，当创建完一个 ByteBuf，它的引用为1，然后每次调用 `retain() `方法， 它的引用就加一， `release()` 方法原理是将引用计数减一，**减完之后如果发现引用计数为0，则直接回收 ByteBuf 底层的内存**。



> slice()、duplicate()、copy()

1. ```java
   public abstract ByteBuf slice();

返回此缓冲区可读字节的切片。
修改返回缓冲区或此缓冲区的内容会影响彼此的内容，同时它们会保持单独的索引和标记。
此方法与buf.slice（buf.readerIndex（），buf.readableBytes（））相同。
此方法不会修改此缓冲区的readerIndex或writerIndex。 还要注意，此方法不会调用retain（），因此引用计数不会增加。

2. ```java
   public abstract ByteBuf duplicate();
   ```

   返回共享此缓冲区整个区域的缓冲区。

   同上

3. ```java
   public abstract ByteBuf copy();
   ```

   返回此缓冲区可读字节的副本。

   同上

   但是会调用`retain() `



> retainedSlice() 与 retainedDuplicate()

截取内存时，增加内存的引用计数，等同如下

```java
// retainedSlice 等价于
slice().retain();

// retainedDuplicate() 等价于
duplicate().retain()
```



### 5.客户端与服务端通信协议编解码

#### 通信协议

无论是使用 Netty 还是原始的 Socket 编程，基于 TCP 通信的数据包格式均为二进制，协议指的就是客户端与服务端事先商量好的，每一个二进制数据包中每一段字节分别代表什么含义的规则。

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/13/1653028b3b0a2ef0~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

指定一个协议，那么客户端传输的二进制数据，服务端就可以根据该协议精准获取数据。



![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/13/1653028b3e5c1437~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

> 传输流程

1. 客户端将对象转换为二进制数据
2. 通过网络将数据传输到服务端，由TCP/IP 协议负责数据的传输
3. 服务端将二进制数据根据协议转换为对象
4. 服务端通过一系列操作，再通过该流程将响应数据传回客户端。



#### 通信协议设计

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/13/1653028b36ee5d81~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

1. `魔数` 通信双方设置的暗号，接收方会根据这个暗号来判断是不是自己人。有点像`天王盖地虎`，回答上`宝塔镇河妖` 你就是自己人，那么就让你进门。回答不上统统嘎了。
2. 版本号 ，程序会进行更新，那么协议也如此。不同协议对应的解析方法不同，所以生产级项目强烈预留这个字段。
3. 序列化算法，表示如何将对象转换为二进制以及二进制如何转换为对象。如：json，hessian或者java自带的等
4. 指令：针对不同的数据处理逻辑
5. 数据长度和数据。不同指令（操作）不同数据



### 6.自定义协议

#### protocol（协议）

定义通信的Java对象

> Packet.java

```java
@Data
public abstract class Packet {
    /**
     * 协议版本
     */
    private Byte version = 1;

    /**
     * 指令
     */
    public abstract Byte getCommand();
}

```

`@Data`引入lombok依赖，生成setter和getter方法

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.26</version>
</dependency>
```



> command.java

```java
public interface command {
    /**
     * 登录指令
     */
    Byte LOGIN_REQUEST = 1;
}

```

> LoginRequestPacket.java

```java
@Data
public class LoginRequestPacket extends Packet {

    private Integer userId;

    private String username;

    private String password;

    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}

```

> packetCodeC.java

```java
public class PacketCodeC {

    // 魔数
    private static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte,Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer>serializerMap;
	// map管理关系
    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        serializerMap = new HashMap<>();
        Serializer jsonSerializer = new JSONSerializer();
        serializerMap.put(jsonSerializer.getSerializerAlgorithm(),jsonSerializer);

    }

}
```



```java

    /**
     * 编码
     */

    public ByteBuf encode(Packet packet){
        //1.获得ByteBuf 对象
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        //2.序列化对象

        byte[] serialize = Serializer.DEFAULT.serialize(packet);

        //3.进行编码

        //魔数
        byteBuf.writeInt(MAGIC_NUMBER);
        //版本
        byteBuf.writeByte(packet.getVersion());
        //序列化算法
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        //指令
        byteBuf.writeByte(packet.getCommand());
        // 数据长度
        byteBuf.writeInt(serialize.length) ;
        //数据
        byteBuf.writeBytes(serialize);
        return byteBuf;
    }

```

```java
 /**
     * 解码
     */

    public Packet decode(ByteBuf byteBuf){

        //跳过魔数，实际应该取出来然后进行比较，此处省略
        byteBuf.skipBytes(4);

        //跳过版本号,同上
        byteBuf.skipBytes(1);

        byte serializerAlgorithm = byteBuf.readByte();

        byte command = byteBuf.readByte();

        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];

        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializerAlgorithm);
        if (requestType!=null&&serializer!=null){
            return serializer.deserialize(requestType, bytes);
        }
        return null;
    }

    private Serializer getSerializer(byte serializerAlgorithm) {
        return serializerMap.get(serializerAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }
```



#### serialize (数据转换)

> Serializer 转换器

```java
public interface Serializer {

    /**
     * 默认json 序列化
     */
    
    Serializer DEFAULT = new JSONSerializer();

    /**
     * 获得具体的序列化算法标识
     */
    byte getSerializerAlgorithm();
    
    /**
     * java 对象转换成二进制
     */
    byte[] serialize(Object object);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);
}
```



> SerializerAlgorithm 转换标识

```java
public interface SerializerAlgorithm {
    /**
     * json 序列化标识
     */
    byte JSON = 1;
}
```



> JSONSerializer json转换器

```java
public class JSONSerializer implements Serializer {
   
    @Override
    public byte getSerializerAlgorithm() {
        
        return SerializerAlgorithm.JSON;
    } 

    @Override
    public byte[] serialize(Object object) {
        
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        
        return JSON.parseObject(bytes, clazz);
    }
}
```



此处使用了alibba的`fastjson`工具类

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.24</version>
</dependency>
```



### 7.实战Ⅱ：实现客户端登录

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/14/16535d7424e02d3a~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

流程如图

1. 构建登录请求对象编码为ByteBuf传输到服务端。
2. 服务端解码后进行登录校验，然后构造登录相应对象，依然经过编码传输回客户端。
3. 客户端拿到登录响应判断是否登录成功



#### protocol

在上面自定义协议中，添加一个响应包类

> LoginResponsePacket.java

```java
@Data
public class LoginResponsePacket extends Packet{


    private String reason;

    private Boolean isSuccess;
    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}

```



> command.java

新增登录响应指令

```java
public interface command {
    /**
     * 登录指令
     */
    Byte LOGIN_REQUEST = 1;

    /**
     * 登录响应指令
     */

    Byte LOGIN_RESPONSE = 2;
}
```



#### server

我们将实战Ⅰ中的客户端和服务端代码进行修改

> ServerHandler.java

```java
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        ByteBuf requestByteBuf= (ByteBuf) msg;
        /**
         * 解码
         */

        Packet packet = PacketCodeC.getInstance().decode(requestByteBuf);

        /**
         * 判断是否为请求数据包
         */
        if (LOGIN_REQUEST.equals(packet.getCommand())) {
            LoginRequestPacket loginRequestPacket = (LoginRequestPacket) packet;

            LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
           
 
            if (valid(loginRequestPacket)){
                System.out.println("鉴权成功");
                loginResponsePacket.setIsSuccess(true);
            } else {
                System.out.println("鉴权失败");
                loginResponsePacket.setReason("密码错误");
                loginResponsePacket.setIsSuccess(false);
            }

            ByteBuf responseByteBuf = PacketCodeC.getInstance().encode(loginResponsePacket);

            ctx.channel().writeAndFlush(responseByteBuf);
        }
   }

    /**
     * 鉴权逻辑
     */
    private boolean valid(LoginRequestPacket loginRequestPacket){
        return true;
    }
```



> ClientHandler.java



```java
    /**
     * 客户端连接便会执行
     */
@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println(new Date()+": 客户端开始登录");

    LoginRequestPacket loginRequestPacket = new LoginRequestPacket();

    loginRequestPacket.setUserId(UUID.randomUUID().toString());
    loginRequestPacket.setUsername("wuxie");
    loginRequestPacket.setPassword("123");

    //使用到单例模式创建
    ByteBuf byteBuf = PacketCodeC.getInstance().encode(loginRequestPacket);
    ctx.channel().writeAndFlush(byteBuf);
}

    /**
     * 获取服务端发回的响应数据
     */

@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf byteBuf = (ByteBuf) msg;

    Packet packet = PacketCodeC.getInstance().decode(byteBuf);

    if(LOGIN_RESPONSE.equals(packet.getCommand())){
        LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;

        if (loginResponsePacket.getIsSuccess()){
            System.out.println(new Date()+": 客户端登录成功");
        } else {
            System.out.println(new Date()+": 客户端登录失败，原因:"+loginResponsePacket.getReason());
        }
    }


}
```

注意：

每个hanler处理器建议重写一个方法，进行一个异常捕获，及时关闭``ChannelHandlerContext`

```java
@Override
public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    Channel channel = ctx.channel();
    if (channel.isActive()){
        ctx.close();
    }
}
```



结果如下：



![image-20230310083345241](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310083345241.png)



![image-20230310083352133](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310083352133.png)



> 注意

关于出现内容泄露的问题:

如果你的channelHandler是继承自ChannelInboundHandlerAdapter，在channelRead 中接受到的入站byteBuffer是需要手工release的，但是如果是SimpleChannelInboundHandler则不需要。



### 8.实战Ⅲ 客户端和服务端收发消息

#### protocol

添加消息请求和响应指令

> command.java

```java
/**
 * 消息发送
 */

Byte MESSAGE_REQUEST = 3;

/**
 * 消息响应指令
 */

Byte MESSAGE_RESPONSE = 4;
```



添加消息请求和响应包

> MessageRequestPacket，MessageResponsePacket.java

```java
@Data
public class MessageResponsePacket extends Packet {

    private String message;



    @Override
    public Byte getCommand() {
        return MESSAGE_RESPONSE;
    }
}

===============
@Data
public class MessageRequestPacket extends Packet {

    private String message;



    @Override
    public Byte getCommand() {
        return MESSAGE_REQUEST;
    }
}
```



> 收发消息，需要判断用户是否登录。所以用到channel.attr的方法，进行添加登录标识
>
> Attributes.java

```java
public interface Attributes {
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");
}
```



> 注意新增了包类型，编码类里面需要添加相应类型
>
> PacketCodeC.java

```java
static {
    packetTypeMap = new HashMap<>();
    packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
    packetTypeMap.put(LOGIN_RESPONSE,LoginResponsePacket.class);
    /**
    * 注意添加
    */
    packetTypeMap.put(MESSAGE_REQUEST,MessageRequestPacket.class);
    packetTypeMap.put(MESSAGE_RESPONSE,MessageResponsePacket.class);
    serializerMap = new HashMap<>();
    Serializer jsonSerializer = new JSONSerializer();
    serializerMap.put(jsonSerializer.getSerializerAlgorithm(),jsonSerializer);
}
```



#### utils

> 新增工具类，进行登录的标记和判断
>
> LoginUtil.java

```java
public class LoginUtil {
    public static void markAsLogin(Channel channel) {
        channel.attr(Attributes.LOGIN).set(true);
    }

    public static boolean hasLogin(Channel channel) {
        Attribute<Boolean> loginAttr = channel.attr(Attributes.LOGIN);

        return loginAttr.get() != null;
    }
}
```



#### client

> 客户端发送消息，新创建一个线程进行消息发送
>
> NettyClient.java

这里判断逻辑有点不对，只是简单展示下，读者可以自行更改。

```java

public static void startConsoleThread(Channel channel) {
    new Thread(() -> {
        while (!Thread.interrupted()) {
            if (LoginUtil.hasLogin(channel)) {
                System.out.println("======请输入消息到服务端");
                Scanner scanner = new Scanner(System.in);
                String message = scanner.nextLine();

                MessageRequestPacket messageRequestPacket = new MessageRequestPacket();
                messageRequestPacket.setMessage(message);
                ByteBuf byteBuf = PacketCodeC.getInstance().encode(messageRequestPacket);
                channel.writeAndFlush(byteBuf);
            } 
        }
    }).start();

}
========== 
private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
    bootstrap.connect(host, port).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功!");
			
            Channel channel = ((ChannelFuture) future).channel();
            startConsoleThread(channel);
        } else if (retry == 0) {
            //
        } else {
            //
        }
}
```



> ClientHandler.java  添加读取响应逻辑

```java
if (LOGIN_RESPONSE.equals(command)) {
        LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;

        if (loginResponsePacket.getIsSuccess()) {
            //登录标记
            LoginUtil.markAsLogin(ctx.channel());
            System.out.println(new Date() + ": 客户端登录成功");
        } else {
            System.out.println(new Date() + ": 客户端登录失败，原因:" + loginResponsePacket.getReason());
        }
    } else if (MESSAGE_RESPONSE.equals(command)){

        MessageResponsePacket messageResponsePacket= (MessageResponsePacket) packet;
        System.out.println(new Date()+": 收到服务端的消息 :" + messageResponsePacket.getMessage());
    }
    byteBuf.release();
```





#### server

> ServerHandler.java

```java
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        ByteBuf requestByteBuf = (ByteBuf) msg;
        /**
         * 解码
         */

        Packet packet = PacketCodeC.getInstance().decode(requestByteBuf);

        Byte command = packet.getCommand();
        /**
         * 判断是否为请求数据包
         */
        if (LOGIN_REQUEST.equals(command)) {
			....
        } else if (MESSAGE_REQUEST.equals(command)) {
            MessageRequestPacket messageRequestPacket = (MessageRequestPacket) packet;
            System.out.println(new Date() + ": 收到客户端消息 :" + messageRequestPacket.getMessage());

            // 收到消息进行响应
            MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
            messageResponsePacket.setMessage("服务端回复【" + messageRequestPacket.getMessage() + "】");

            ByteBuf responseByteBuf = PacketCodeC.getInstance().encode(messageResponsePacket);

            ctx.channel().writeAndFlush(responseByteBuf);

        }

        requestByteBuf.release();
    }
```







![image-20230310092932610](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310092932610.png)

![image-20230310092937474](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310092937474.png)



#### 总结

1. 定义消息请求和响应的包装类
2. 使用channel的`attr`方法，进行添加登录标记
3. 开启新线程进行客户端的消息发送
4. 服务单获得消息并且对客户端做出响应

#### 问题：

当指令越来越多，如何避免`channelRead`中的对指令处理逻辑的`if else`泛滥？



### 9.pipeline 与 channelHandler

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/17/16545510d7b4f970~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

如上图我们可知将所有数据处理都放在了一个类中，客户端的`ClientHandler`和服务端的`ServerHandler`，那么就会导致一个类非常臃肿。并且对于数据的输送我们都必要手动的进行编码，那么我们会想将不同逻辑进行**模块化处理**，不同类处理不同逻辑，比如登录类处理登录校验，编码类处理编码等，然后将其串联起来，形成一个完整的逻辑处理链。



Netty 中的 `pipeline `和 `channelHandler `正是用来解决这个问题的：它通过**责任链设计模式**来组织代码逻辑，并且能够**支持逻辑的动态添加和删除** ，Netty 能够支持各类协议的扩展，比如 HTTP，Websocket，Redis，靠的就是 pipeline 和 channelHandler。



#### 构成

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/17/1654526f0a67bb52~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

由图得所有处理逻辑都在`ChannelPipeline`对象中，并且它是一个双向链表，与channel一对一关系。

`ChannelPipeline` 里面每个节点都是一个 `ChannelHandlerContext` 对象，这个对象能够拿到和 Channel 相关的**所有的上下文信息**，然后这个对象包着一个重要的对象，那就是逻辑处理器 `ChannelHandler`。



#### 分类

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/17/1654526f0a8f2890~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)



- `ChannelInboundHandler` 

**处理读数据的逻辑**，比如，我们在一端读到一段数据，首先要解析这段数据，然后对这些数据做一系列逻辑处理，最终把响应写到对端， 在开始组装响应之前的所有的逻辑，都可以放置在 `ChannelInboundHandler` 里处理，它的一个最重要的方法就是 `channelRead()`。读者可以将 `ChannelInboundHandler` 的逻辑处理过程与 TCP 的七层协议的解析联系起来，收到的数据一层层从物理层上升到我们的应用层。

- `ChannelOutBoundHandler `

**处理写数据的逻辑**，它是定义我们一端在组装完响应之后，把数据写到对端的逻辑，比如，我们封装好一个 response 对象，接下来我们有可能对这个 response 做一些其他的特殊逻辑，然后，再编码成 ByteBuf，最终写到对端，它里面最核心的一个方法就是 `write()`，读者可以将 `ChannelOutBoundHandler` 的逻辑处理过程与 TCP 的七层协议的封装过程联系起来，我们在应用层组装响应之后，通过层层协议的封装，直到最底层的物理层。



上面是两个接口，有对应的默认实现，`ChannelInboundHandlerAdapter`，和 `ChanneloutBoundHandlerAdapter`，它们分别实现了两大接口的所有功能，**默认情况下会把读写事件传播到下一个 handler**。



#### ChannelInboundHandler 的事件传播

基于实战Ⅲ的代码修改

```java
serverBootstrap
        .group(bossGroup, workGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel channel) {
                channel.pipeline().addLast(new FirstServerHandler());
                channel.pipeline().addLast(new InBoundHandlerA());
                channel.pipeline().addLast(new InBoundHandlerB());
                channel.pipeline().addLast(new InBoundHandlerC());
            }
        });


class InBoundHandlerA extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InBoundHandlerA:" + msg);
        super.channelRead(ctx, msg);
    }
}

......
```



`childHandler` 是要客户端连接才会执行，并且`server`向外输送数据，所以都需要启动才会看到效果。

可见顺序是`A->B->C`



![image-20230310161310428](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310161310428.png)



#### ChannelOutboundHandler 的事件传播

```java
    .group(bossGroup, workGroup)
    .channel(NioServerSocketChannel.class)
    .childHandler(new ChannelInitializer<NioSocketChannel>() {
        protected void initChannel(NioSocketChannel channel) {
            channel.pipeline().addLast(new FirstServerHandler());
            channel.pipeline().addLast(new InBoundHandlerA());
            channel.pipeline().addLast(new InBoundHandlerB());
            channel.pipeline().addLast(new InBoundHandlerC());

            // outBound，处理写数据的逻辑链
            channel.pipeline().addLast(new OutBoundHandlerA());
            channel.pipeline().addLast(new OutBoundHandlerB());
            channel.pipeline().addLast(new OutBoundHandlerC());
        }
    });


class OutBoundHandlerA extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutBoundHandlerA:"+msg);
        super.write(ctx, msg, promise);
    }
}
.....
```



`ChannelOutboundHandlerAdapter` 需要`client`向`server` 发送数据，server处理写数据

这里没有调用`InBoundHandlerABC` ，因为前者`FirstServerHandler` 进行处理并没有向下进行传递

需要使用到`fireChannelRead` 才能进行向下传递，所以`InBoundHandlerABC` 就没有执行

```java
super.channelRead(ctx, msg);
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ctx.fireChannelRead(msg);
}
```



同理如果删除某个`OutBoundHandlerC`的`super.write(ctx, msg, promise);` ，一样不会向下执行。







![image-20230310163252543](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310163252543.png)



可见顺序与添加顺序相反是`C->B->A`



> pipeline 的结构

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/17/1654526f0a73d8c3~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

ChannelHandlerContext

> 实际存储在Pipeline中的并非是ChannelHandler，而是上下文对象。将Handler包裹在上下文对象中，通过上下文对象与它所属的ChannelPipeline交互，向上或向下传递事件或者修改pipeline都是通过上下文对象。

那么如何维护Pipeline中的handler呢

> ChannelPipeline是线程安全的，ChannelHandler可以在任何时候添加或者删除。例如你可以在即将交换敏感信息时插入加密处理程序，并在交换后删除它。一般操作，初始化的时候增加进去，较少删除。下面是Pipeline中管理的API





> pipeline的执行顺序

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/17/1654526f4f032dbb~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)



虽然两种类型的 handler 在一个双向链表里，但是这两类 handler 的分工是不一样的，inBoundHandler 的事件通常只会传播到下一个 inBoundHandler，outBoundHandler 的事件通常只会传播到下一个 outBoundHandler，**两者相互不受干扰**。



#### 思考

1. 参考本文的例子，如果我们往 pipeline 里面添加 handler 的顺序不变， 要在控制台打印出 inboundA -> inboundC -> outboundB -> outboundA，该如何实现？
2. 如何在每个 handler 里面打印上一个 handler 处理结束的时间点？





### 10. 实战Ⅳ：构建客户端与服务端 pipeline

通过Netty内置的ChannelHandler进行构建

**ChannelInboundHandlerAdapter ，ChannelOutboundHandlerAdapter**

---



上文已经介绍过，我们主要关心如下方法

> ChannelInboundHandlerAdapter.java

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ctx.fireChannelRead(msg);
}
```

接收上一个handler输出，其中`msg`就是上一个handler的输出。默认情况下 adapter 会通过 `fireChannelRead()` 方法直接把上一个 handler 的输出结果传递到下一个 handler。

> ChannelOutboundHandlerAdapter.java

```java
@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    ctx.write(msg, promise);
}
```

同上，但是传播顺序与上方相反



自定义自己的pipeline，考虑数据的处理逻辑，首先服务端会受到请求，通过`channelRead`，`msg`就是`ByteBuf`，所以对其进行解码。

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf requestByteBuf = (ByteBuf) msg;
        // 解码
        Packet packet = PacketCodeC.INSTANCE.decode(requestByteBuf);
        // 解码后的对象传递到下一个 handler 处理
        ctx.fireChannelRead(packet)
}
```

解码前需要对数据进行强转，Netty对此进行了优化，提供了一个解码的父类



**ByteToMessageDecoder**

---

```java
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) {
        out.add(PacketCodeC.INSTANCE.decode(in));
    }
}
```

实现它的`decode` 方法，并且这里的数据已经是`ByteBuf` ，所以不需要强转了，并且这里使用到了一个`List`存储数据，就自动实现了向下一个handler的传递。

另外，值得注意的一点，对于 Netty 里面的 ByteBuf，我们使用 `4.1.6.Final` 版本，默认情况下用的是堆外内存，在前面提到过，**堆外内存我们需要自行释放**，在我们前面小节的解码的例子中，其实我们已经漏掉了这个操作，这一点是非常致命的，随着程序运行越来越久，内存泄露的问题就慢慢暴露出来了， 而这里我们使用 `ByteToMessageDecoder`，Netty 会自动进行内存的释放，我们不用操心太多的内存管理方面的逻辑，关于如何自动释放内存大家有兴趣可以参考一下 [ByteToMessageDecoder的实现原理(8-2)](https://link.juejin.cn/?target=https%3A%2F%2Fcoding.imooc.com%2Fclass%2Fchapter%2F230.html%23Anchor)。



**SimpleChannelInboundHandler**

---

回顾之前的数据处理逻辑

```java
if (LOGIN_REQUEST.equals(command)) {

} else if (MESSAGE_REQUEST.equals(command)) {

}
```

我们将不同数据处理化分不同的Handler处理

```java
if (LOGIN_REQUEST.equals(command)) {
	//
} else {
    ctx.fireChannelRead(packet); 
}
```

这样可以使得每添加一个指令处理器，逻辑处理框架都一致；但会多出一个`else`进行传递无法处理的对象给下一个指令，重复度较高。Netty针对这种抽象出了`SimpleChannelInboundHandler`对象，将类型判断和对象传递都自当实现了，我们只需要关注处理逻辑即可。

> LoginRequestHandler.java

```java
public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginRequestPacket) {
        // 登录逻辑
    }
}
```

这样对对象的判断和传递都交给了父类`SimpleChannelInboundHandler`

同理，编写其他处理器只需要更改泛型类型即可。

> MessageRequestHandler.java

```java
public class MessageRequestHandler extends SimpleChannelInboundHandler<MessageRequestPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRequestPacket messageRequestPacket) {

    }
}
```





----

上面介绍了Netty内部自带的解码类，同样Netty也自带了编码类，帮助我们对数据的编码进行优化；

**MessageToByteEncoder**

**处理逻辑**

```java
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        PacketCodeC.INSTANCE.encode(out, packet);
    }
}
```

这样就不需要自己创建`ByteBuf`对象，并且通过不同的handler也不需要对数据再一次进行编码。

这里读者看出，使用这个类，就需要更改我们自己的`encode`的方法。

**原本的代码**

```java
public ByteBuf encode(Packet packet) {
    // 1. 创建 ByteBuf 对象
    ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
    // 2. 序列化 java 对象

    // 3. 实际编码过程

    return byteBuf;
}
```

**优化后**

```java
// 更改后的定义
public void encode(ByteBuf byteBuf, Packet packet) {
    // 1. 序列化 java 对象

    // 2. 实际编码过程
}
```



现在进行总结，我们自定义pipeline对什么进行了优化

1. 编解码的优化（`ByteToMessageDecoder`,`MessageToByteEncoder`
2. 将对某个指令的逻辑处理单独提出，并且对handler数据的传递进行优化  `SimpleChannelInboundHandler`

#### 构建客户端和服务端的pipeline

![img](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/10/14/1666fd9cc2b9c089~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)



代码较多，请读者自行去笔者仓库查看源码。[github]([Netty/src/main/java/com/wuxie/netty at master · zqywuxie/Netty (github.com)](https://github.com/zqywuxie/Netty/tree/master/src/main/java/com/wuxie/netty/Demo6))



### 11.实战Ⅴ：拆包和粘包

---



拿双向通信的代码进行更改，仓库`Demo2`



> ClientHandler.java

```java
public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 1000; i++) {
            ByteBuf buffer = getByteBuf(ctx);
            ctx.channel().writeAndFlush(buffer);
        }
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        byte[] bytes = "你好，我们在学习netty".getBytes(Charset.forName("utf-8"));
        buffer.writeBytes(bytes);
        return buffer;
    }

}
```

向服务端连续发送1000条数据，我们希望得到的结果是连续的1000条完整的数据。但是实际如下，出现了三种数据

1. 正常数据
2. 多个字符连在一起，定义这种`ByteBuf` 为粘包
3. 字符串被拆开了，这也是为什么会出现不完整的字符串和乱码(那一段的字节不完整，字自然也不完整)。定义这种`ByteBuf`为半包



![image-20230310202348592](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310202348592.png)



#### 出现原因

---

我们需要知道，尽管我们在应用层面使用了 Netty，但是对于操作系统来说，只认 TCP 协议，尽管我们的应用层是按照 ByteBuf 为 单位来发送数据，但是到了**底层操作系统仍然是按照字节流发送数据**，因此，数据到了服务端，也是按照字节流的方式读入，然后到了 Netty 应用层面，重新拼装成 ByteBuf，**而这里的 ByteBuf 与客户端按顺序发送的 ByteBuf 可能是不对等的**。因此，我们需要在客户端根据自定义协议来组装我们应用层的数据包，然后在服务端根据我们的应用层的协议来组装数据包，这个过程通常在服务端称为拆包，而在客户端称为粘包。

拆包和粘包是相对的，一端粘了包，另外一端就需要将粘过的包拆开，举个栗子，发送端将三个数据包粘成两个 TCP 数据包发送到接收端，接收端就需要根据应用协议将两个数据包重新组装成三个数据包。



#### 拆包原理

---

在没有 Netty 的情况下，用户如果自己需要拆包，基本原理就是不断从 TCP 缓冲区中读取数据，每次读取完都需要判断是否是一个完整的数据包。如果不是完整的包就继续读取数据，如果是则拼接上次读取的数据，构成一个**完整**的业务数据传递到业务逻辑中，多余的数据仍然保留与下次读取到的数据进行拼接，如此重复操作。

现在这种操作很麻烦，需要自定义协议和异常等实现。但Netty显然自带一些开箱即用的拆包器



#### Netty的拆包器

---

##### 1. 固定长度的拆包器 FixedLengthFrameDecoder

如果你的应用层协议非常简单，每个数据包的长度都是固定的，比如 100，那么只需要把这个拆包器加到 pipeline 中，Netty 会把一个个长度为 100 的数据包 (ByteBuf) 传递到下一个 channelHandler。

##### 2. 行拆包器 LineBasedFrameDecoder

从字面意思来看，发送端发送数据包的时候，每个数据包之间以换行符作为分隔，接收端通过 LineBasedFrameDecoder 将粘过的 ByteBuf 拆分成一个个完整的应用层数据包。

##### 3. 分隔符拆包器 DelimiterBasedFrameDecoder

DelimiterBasedFrameDecoder 是行拆包器的通用版本，只不过我们可以**自定义分隔符**。

##### 4. 基于长度域拆包器 LengthFieldBasedFrameDecoder

最后一种拆包器是最通用的一种拆包器，只要你的**自定义协议中包含长度域字段**，均可以使用这个拆包器来实现应用层拆包。由于上面三种拆包器比较简单，读者可以自行写出 demo，接下来，我们就结合我们小册的自定义协议，来学习一下如何使用基于长度域的拆包器来拆解我们的数据包。



#### 使用LengthFieldBasedFrameDecoder

---

![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/13/1653028b36ee5d81~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

如下图源码，

第一个参数为数据包的最大长度
第二个参数为长度域的偏移量，也就上图协议中到达数据长度的长度，也就是4+1+1+1=7个字节

第三个参数为数据的长度 4个字节

```java
public LengthFieldBasedFrameDecoder(
        int maxFrameLength,
        int lengthFieldOffset, int lengthFieldLength) {
    this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
}
```

```java
new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 7, 4);
```



这里我们使用实战IV中代码进行测试 `Demo6`

> NettyClient.java

连续发送1000条数据，会报错

```java
public static void startConsoleThread(Channel channel) {
new Thread(() -> {
while (!Thread.interrupted()) {
    if (LoginUtil.hasLogin(channel)) {
        for (int i = 0; i < 1000; i++) {
            System.out.println("======请输入消息到服务端");
//                        Scanner scanner = new Scanner(System.in);
//                        String message = scanner.nextLine();
            String  message = "我们在学习netty";

            channel.writeAndFlush(new MessageRequestPacket(message));
        }

    }
}
}).start();

}
```



> io.netty.[handler](https://so.csdn.net/so/search?q=handler&spm=1001.2101.3001.7020).codec.DecoderException: java.lang.IndexOutOfBoundsException:
> readerIndex(11) + length(565) exceeds writerIndex(512): PooledUnsafeDirectByteBuf(ridx: 11, widx: 512, cap: 512)

数据包的长度为565，而`ByteToMessageDecoder`只处理到了512。我并没有找到控制`ByteToMessageDecoder`最大读写的方法。
**但是，因为解码器继承`ChannelInboundHandlerAdapter`类，而我们可以使用多个处理器一起处理数据**。



所以这里我们加上拆包器(服务端和客户端都加上)，然后进行测试

```java
bootstrap
.group(workerGroup)
.channel(NioSocketChannel.class)
.handler(new ChannelInitializer<NioSocketChannel>() {

@Override
protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
    nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,7,4));
    nioSocketChannel.pipeline().addLast(new PacketDecoder());
    nioSocketChannel.pipeline().addLast(new LoginResponseHandler());
    nioSocketChannel.pipeline().addLast(new MessageResponseHandler());
    nioSocketChannel.pipeline().addLast(new PacketEncoder());
}
});
```



![image-20230310205345470](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310205345470.png)

可以看到数据没有任何问题，也没有任何报错传输成功。



#### 拒绝非本协议的连接

---

最一开始我们设计协议的时候，说到一个`魔数`，是用来判断是否是本协议的数据。而这个判断肯定是要获得数据后马上进行判断，而我们一开始添加的handler是`LengthFieldBasedFrameDecoder`，那么我们就可以对它进行优化。

```java
public class Spliter extends LengthFieldBasedFrameDecoder {

    private static final int LENGTH_FIELD_OFFSET = 7;

    public static final int LENGTH_FIELD_LENGTH = 4;
    public Spliter (){
        //调用父类构造器  LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,LENGTH_FIELD_OFFSET,LENGTH_FIELD_LENGTH)
        super(Integer.MAX_VALUE,LENGTH_FIELD_OFFSET,LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //in 缓存区的开头，获得读指;然后getint读取一个整数(魔数)
        if (in.getInt(in.readerIndex()) != PacketCodeC.MAGIC_NUMBER){
            ctx.channel().close();
            return null;
        }

        return super.decode(ctx, in);
    }
}

```



重写deocde()方法，每次从channel中读取到数据时，都会进行调用。



使用windows中的`talnet 127.0.0.1 8000 `,进行测试，并且发送非自定义协议的内容。

可见直接拦截了，断开了连接。

![image-20230310214701769](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230310214701769.png)



#### 服务端和客户端的 pipeline 结构

---





![image.png](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/28/1657e014321e00b0~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)



### 12.channelHandler的生命周期

---



> 本节针对读数据的相关逻辑，讨论`ChannelInboundHandler`

基于`ChannelInboundHandler`自定义一个handler

> LifeCyCleTestHandler.java

```java
public class LifeCyCleTestHandler extends ChannelInboundHandlerAdapter {
@Override
public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    System.out.println("逻辑处理器被添加：handlerAdded()");
    super.handlerAdded(ctx);
}

@Override
public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channel 绑定到线程(NioEventLoop)：channelRegistered()");
    super.channelRegistered(ctx);
}

@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channel 准备就绪：channelActive()");
    super.channelActive(ctx);
}

@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    System.out.println("channel 有数据可读：channelRead()");
    super.channelRead(ctx, msg);
}

@Override
public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channel 某次数据读完：channelReadComplete()");
    super.channelReadComplete(ctx);
}

@Override
public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channel 被关闭：channelInactive()");
    super.channelInactive(ctx);
}

@Override
public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channel 取消线程(NioEventLoop) 的绑定: channelUnregistered()");
    super.channelUnregistered(ctx);
}

@Override
public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    System.out.println("逻辑处理器被移除：handlerRemoved()");
    super.handlerRemoved(ctx);
}
}
```



添加到pipeline中，然后客户端发送消息，服务端的展示

![image-20230311170030220](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311170030220.png)



由上可以得到回调顺序:

`handlerAdded() -> channelRegistered() -> channelActive() -> channelRead() -> channelReadComplete()`



1. `handlerAdded()` ：指的是当检测到新连接之后，调用 `ch.pipeline().addLast(new LifeCyCleTestHandler());` 之后的回调，表示在当前的 channel 中，已经成功添加了一个 handler 处理器。
2. `channelRegistered()`：这个回调方法，表示当前的 channel 的所有的逻辑处理已经**和某个 NIO 线程建立了绑定关系**
3. `channelActive() `：当 channel 的所有的业务逻辑链准备完毕（也就是说 channel 的 pipeline 中已经添加完所有的 handler）以及绑定好一个 NIO 线程之后，这条连接算是真正激活了，接下来就会回调到此方法。
4. `channelRead()`：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读。
5. `channelReadComplete()`：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕。

---



关闭客户端后，即channel被关闭了

![image-20230311170309266](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311170309266.png)



回调顺序为：

`channelInactive() -> channelUnregistered() -> handlerRemoved()`

1. `channelInactive()`: 表面这条连接已经被关闭了，这条连接在 TCP 层面已经不再是 ESTABLISH(建立) 状态了
2. `channelUnregistered()`: 既然连接已经被关闭，那么与这条连接绑定的线程就不需要对这条连接负责了，这个回调就表明与这条连接对应的 NIO **线程移除掉对这条连接的处理**
3. `handlerRemoved()`：最后，我们给这条连接上添加的所有的业务逻辑处理器都给移除掉。



![img](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/10/14/1666fdc2bdcf3f9e~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)





#### 用法举例

---



##### 1.ChannelInitializer 实现原理

我们添加一个处理器，都是在`ChannelInitializer`里面的`initChannel`方法中，拿到channel对应的pipeline，然后向里面添加handler。查看源码



> ChannelInitializer.java

```java
protected abstract void initChannel(C ch) throws Exception;

public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            // Normally this method will never be called as handlerAdded(...) should call initChannel(...) and remove
        // the handler.
        if (initChannel(ctx)) {
            ctx.pipeline().fireChannelRegistered();
        } else {
            ctx.fireChannelRegistered();
        }
}

public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    // ...
    if (ctx.channel().isRegistered()) {
        initChannel(ctx);
    }
    // ...
}

private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
    if (initMap.putIfAbsent(ctx, Boolean.TRUE) == null) {
        initChannel((C) ctx.channel());
        // ...
        return true;
    }
    return false;
}
```



1. `ChannelInitializer` 定义了一个抽象的方法 `initChannel()`，这个抽象方法由我们自行实现，我们在服务端启动的流程里面的实现逻辑就是往 pipeline 里面塞我们的 handler 链
2. `handlerAdded()` 和 `channelRegistered()` 方法，都会尝试去调用 `initChannel()` 方法，`initChannel()` 使用 `putIfAbsent()` 来防止 `initChannel()` 被调用多次
3. 读者可以看`channelRegistered` 中的英文，可知这个方法一般都不会调用

> 通常，这个方法永远不会被调用，因为handlerAdded（…）应该调用initChannel（…）并移除处理程序。
>
> 执行了handlerAdded()方法，逻辑最终会把ChannelInitializer从pipeline中移除掉，最后有个remove方法。所以在后续的register事件中，自然就调不到ChannelInitializer中的channelRegistered()方法了。



##### 2.handlerAdded() 与 handlerRemoved()

这两个方法通常可以用在一些资源的申请和释放

##### 3. channelActive() 与 channelInActive()

1. 对我们的应用程序来说，这两个方法表明的含义是 TCP 连接的建立与释放，通常我们在这两个回调里面统计单机的连接数，`channelActive()` 被调用，连接数加一，`channelInActive()` 被调用，连接数减一
2. 另外，我们也可以在 `channelActive()` 方法中，实现对客户端连接 ip 黑白名单的过滤，具体这里就不展开了

##### 4. channelRead()

前面实战Ⅴ拆包和粘包，得知服务端根据自定义协议来进行拆包，其实就是在这个方法里面，每次读到一定的数据，都会累加到一个容器里面，然后判断是否能够拆出来一个完整的数据包，如果够的话就拆了之后，往下进行传递



##### 5. channelReadComplete()

前面小节中，我们在每次向客户端写数据的时候，都通过 `writeAndFlush()` 的方法写并刷新到底层，其实这种方式不是特别高效，我们可以在之前调用 `writeAndFlush()` 的地方都调用 `write()` 方法，然后在这个方面里面调用 `ctx.channel().flush()` 方法，相当于一个批量刷新的机制，当然，如果你对性能要求没那么高，`writeAndFlush()` 足矣。



### 13.实战Ⅵ:使用 channelHandler 的热插拔实现客户端身份校验

`Demo8`

前面实战中，说过登录校验代码有点问题。即使没有进行登录校验，服务端收到消息后还是会对消息进行处理。所以对此进行优化

#### 1.身份检验

---

之前的登录标记是在客户端接收到登录成功消息后，这次我们在服务端鉴权成功后进行添加

> LoginRequestHandler.java

```java
if (valid(loginRequestPacket)) {

    // 鉴权后 添加标志
    LoginUtil.markAsLogin(ctx.channel());
	//
} else {
	//
}
```



并且之前客户端发送消息之前需要进行判断是否登录

```java
public static void startConsoleThread(Channel channel) {
    new Thread(() -> {
        while (!Thread.interrupted()) {
            if (LoginUtil.hasLogin(channel)) {
                //
            }
        }
    }).start();

}
```

但我们学了pipeline后，完全可以将这件事交给它做，将其封装为一个用户认证的handler

> AuthHandler.java

```java
public class AuthHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!LoginUtil.hasLogin(ctx.channel())){
            ctx.channel().close();
        } else {
            super.channelRead(ctx, msg);
        }
    }
}

```

然后在`MessageRequestHandler` 添加该handler，那么服务端接收到消息请求时就会进行认证

```java
    .childHandler(new ChannelInitializer<NioSocketChannel>() {
        protected void initChannel(NioSocketChannel channel) {
            channel.pipeline().addLast(new Spliter());
            channel.pipeline().addLast(new PacketDecoder());
            channel.pipeline().addLast(new LoginRequestHandler());
            //先进行认证
            channel.pipeline().addLast(new AuthHandler());
            channel.pipeline().addLast(new MessageRequestHandler());
            channel.pipeline().addLast(new PacketEncoder());
        }
    });
```

这样每次发送前都不需要自己去关注身份的问题，但是每次发送都会校验一遍，明显有些资源的浪费。但这里只是做了一个简单的校验，实际生产中会更加复杂全面，暂且不关注这些问题。



#### 2.移除校验

上面说了每次发送消息都会调用校验逻辑使得资源浪费，所以我们在连接没有中断，只需要校验一次，之后都不用需要校验了。

> AuthHandler.java

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!LoginUtil.hasLogin(ctx.channel())){
        ctx.channel().close();
    } else {
        // 移除逻辑
        ctx.pipeline().remove(this);
        super.channelRead(ctx, msg);
    }
}

@Override
public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (LoginUtil.hasLogin(ctx.channel())) {
        System.out.println("当前连接登录校验完毕，无需再次验证，AuthHandler移除");
    } else {
        System.out.println("无登录验证，强制关闭连接");
    }
}
```



#### 3.身份校验演示

![image-20230311191036068](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311191036068.png)



##### 3.2无身份认证的演示

不发起登录请求

> LoginResponseHandler.java

```java
public class LoginResponseHandler extends SimpleChannelInboundHandler<LoginResponsePacket> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        //使用到单例模式创建
//        ctx.channel().writeAndFlush(loginRequestPacket);
    }

```



![image-20230311191924101](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311191924101.png)



这时候连接关闭了，我们需要进行一个判断客户端连接是否被关闭

```java
@Override
public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("连接被关闭");
    super.channelInactive(ctx);
}
```



![image-20230311192329772](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311192329772.png)



#### 总结：

1. 对于很多重复的业务逻辑，我们可以单独提出handler交给pipeline进行处理
2. 对于某一个逻辑不需要执行很多次时，可以进行动态删除，如上登录逻辑，执行一次后就进行删除，提高程序性能





### 14.实战Ⅶ:客户端互聊原理和实现

#### 原理

---

![单聊流程](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/9/1651c08e91cdd8e6~tplv-t2oaga2asx-zoom-in-crop-mark:3024:0:0:0.awebp)

1.A要与B聊天，那么A与B就需要与服务器进行一个连接，然后进行一次登录鉴权的流程，**服务器保存用户标识和TCP连接的映射关系**

2.A与B聊天，就要**发送一个B的标识**到服务器，然后服务器拿到B的标识，就找到B的TCP连接，将信息发送给B；



#### 实现

---
#####  用户登录状态与 channel 的绑定

我们先创建一个session对象，储存客户端相关数据

> Session.java

这里进行简答构造，实际构造的数据肯定不止如此

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {
    
    // 用户唯一标识
    private String userId;
    
    private String username;
}
```



创建session相关工具类，进行TCP连接和session的连接等操作

> SessionUtil.java

```java
public class SessionUtil {

    // map 存储映射

    public static final Map<String , Channel> userIdChannelMap = new ConcurrentHashMap<>();
    /**
     * 绑定
     */
    public static void bindSession (Session session , Channel channel){
        userIdChannelMap.put(session.getUserId(),channel);
        channel.attr(Attributes.SESSION).set(session);
    }
    /**
     * 去除绑定
     */    
    public static void unBindSession(Channel channel){
        if (hasLogin(channel)){
            userIdChannelMap.remove(getLogin(channel).getUserId());
            channel.attr(Attributes.SESSION).set(null);
        }
    }

    /**
     * 另一层面判断是否登录
     */
    
    public static boolean hasLogin (Channel channel){
        
        return  channel.hasAttr(Attributes.SESSION);
    }

    /**
     * 获得登录数据
     */
    
    public static Session getLogin (Channel channel) {
        return channel.attr(Attributes.SESSION).get();
    }

    /**
     *  获得连接
     */
    public static Channel  getChannel (String userId) {
        return userIdChannelMap.get(userId);
    }
}
```

1. 这里的`SessionUtil` 就是前面`LoginUtil`的一个重构。判断登录的同时也可以多携带一些相关数据



> LoginResponsePacket.java

对登录响应包添加新的字段name和id，让client也进行一个绑定

```java
@Data
public class LoginResponsePacket extends Packet {

    private String userId;

    private String username;
    private String reason;

    private Boolean isSuccess;
    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}

```



> LoginRequestHandler.java

```java
@Override
protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginRequestPacket) throws Exception {

    System.out.println(new Date()+": 收到客户端登录请求....");

    LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
    loginResponsePacket.setVersion(loginRequestPacket.getVersion());
    if (valid(loginRequestPacket)) {

        String userId =UUID.randomUUID().toString();

        loginResponsePacket.setUserId(userId);
        loginResponsePacket.setUsername(loginRequestPacket.getUsername());
	
        // 进行绑定
        SessionUtil.bindSession(new Session(userId, loginResponsePacket.getUsername()),ctx.channel());
        System.out.println(new Date()+": " + userId +"用户登录成功");
        loginResponsePacket.setIsSuccess(true);
    } else {
        System.out.println(new Date()+": 用户登录失败");
        loginResponsePacket.setReason("密码错误");
        loginResponsePacket.setIsSuccess(false);
    }

    ctx.channel().writeAndFlush(loginResponsePacket);
}
```





> LoginResponseHandler.java



```java
@Override
protected void channelRead0(ChannelHandlerContext ctx, LoginResponsePacket loginResponsePacket) throws Exception {
    String username = loginResponsePacket.getUsername();
    String userId = loginResponsePacket.getUserId();
    if (loginResponsePacket.getIsSuccess()) {
        
        ///绑定
        SessionUtil.bindSession(new Session(userId,username),ctx.channel());
        System.out.println(new Date() + ": 登录成功,用户ID"+userId);
    } else {
        System.out.println(new Date() + ": 客户端登录失败，原因:" + loginResponsePacket.getReason());
    }
}


// 断线后进行处理
@Override
public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    SessionUtil.unBindSession(ctx.channel());
    super.channelInactive(ctx);
}
```



##### 服务端接收消息并转发

---



重新定义下信息包内容

> MessageRequestPacket.java

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestPacket extends Packet {

    private String message;

    // 发送消息的客户端ID
    private String toUserId;

    @Override
    public Byte getCommand() {
        return MESSAGE_REQUEST;
    }
}
```



> MessageResponsePacket.java

```java
@Data
public class MessageResponsePacket extends Packet {

    private String message;

    private String fromUserId;

    private String fromUserName;


    @Override
    public Byte getCommand() {
        return MESSAGE_RESPONSE;
    }
}

```



> MessageRequestHandler.java

```java
@Override
protected void channelRead0(ChannelHandlerContext ctx, MessageRequestPacket messageRequestPacket) throws Exception {


    //1.拿到session
    Session login = SessionUtil.getLogin(ctx.channel());

    // 2. 通过发起方的信息构造发送的消息
    MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
    messageResponsePacket.setFromUserId(login.getUserId());
    messageResponsePacket.setFromUserName(login.getUsername());
    messageResponsePacket.setMessage(messageRequestPacket.getMessage());

    //3.拿到消息方的channel

    Channel toUserChannel = SessionUtil.getChannel(messageRequestPacket.getToUserId());

    //4.消息发送给接收方

    if (toUserChannel != null  && SessionUtil.hasLogin(toUserChannel))  {

        toUserChannel.writeAndFlush(messageResponsePacket);
    } else {

        System.err.println("[" + messageRequestPacket.getToUserId() +"] 不在线,发送失败");
    }

}
```



##### 客户端接收消息的处理

---



> MessageResponseHandler.java

```java
public class MessageResponseHandler extends SimpleChannelInboundHandler<MessageResponsePacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageResponsePacket messageResponsePacket) throws Exception {

        String fromUserId = messageResponsePacket.getFromUserId();
        String fromUserName = messageResponsePacket.getFromUserName();
        String message = messageResponsePacket.getMessage();

        System.out.println(fromUserId + ":" + fromUserName + "->" +message);
    }
}
```



##### 客户端控制台登录和发送消息

---



> NettyClient.java

```Java
public static void startConsoleThread(Channel channel) {
Scanner scanner = new Scanner(System.in);
LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
new Thread(() -> {
    while (!Thread.interrupted()) {

        // 判断是否登录 , 是就发送消息 , 否则就登录
            if (!SessionUtil.hasLogin(channel)) {
                System.out.println("输入用户名登录:");
                String username = scanner.nextLine();
                loginRequestPacket.setUsername(username);

                // 默认密码
                loginRequestPacket.setPassword("123");
                channel.writeAndFlush(loginRequestPacket);
                waitForLoginResponse();
            } else {

                System.out.println("====输入你要发送的对象ID");
                String toUserId = scanner.next();
                System.out.println("====输入你要发送的消息");
                String message = scanner.next();
                channel.writeAndFlush(new MessageRequestPacket(message,toUserId));
            }
        }
}).start();

}

// 模拟一个登录延迟
public static void waitForLoginResponse(){
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {

    }
}
```



全部代码展示  `Demo8`



##### 演示

设置可以多开实例

![image-20230311220950616](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311220950616.png)



`server`

---



![image-20230311221046553](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311221046553.png)

`client1`

---



![image-20230311221056203](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311221056203.png)

`client2`

---



![image-20230311221107580](https://wuxie-image.oss-cn-chengdu.aliyuncs.com/image-20230311221107580.png)
