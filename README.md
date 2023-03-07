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

表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数