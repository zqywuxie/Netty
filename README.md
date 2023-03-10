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
        socketChannel.pipeline().addLast(new FirstClientHandler());
    }
```

1. `pipeline` 返回与这条连接相关的逻辑处理链，使用到了责任链模式。

```java
ChannelPipeline pipeline();
```

2. `addLast` 添加一个逻辑处理器，自定义



> FirstClientHandler.java 逻辑处理代码

```java
public class FirstClientHandler extends ChannelInboundHandlerAdapter {
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
        channel.pipeline().addLast(new FirstServerHandler());
    }
});
```

> FirstServerHandler.java

```java
public class FirstServerHandler extends ChannelInboundHandlerAdapter {
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

> FirstServerHandler.java

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



> FirstClientHandler.java



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



> FirstClientHandler.java  添加读取响应逻辑

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

> FirstServerHandler.java

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
