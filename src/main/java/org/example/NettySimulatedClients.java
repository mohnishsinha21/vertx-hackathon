package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class NettySimulatedClients {

    public static void main(String[] args) throws Exception {
        int numberOfClients = 10;  // Number of simulated clients
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                                    String received = ((io.netty.buffer.ByteBuf) msg).toString(CharsetUtil.UTF_8);
                                    System.out.println("Received: " + received);
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // Send data when connection is established
                                    ctx.writeAndFlush(Unpooled.copiedBuffer("Hello from client", CharsetUtil.UTF_8));
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    });

            // Connect multiple clients
            for (int i = 0; i < numberOfClients; i++) {
                final int clientId = i;
                ChannelFuture future = bootstrap.connect("localhost", 1234);
                future.addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        System.out.println("Client " + clientId + " connected!");
                    } else {
                        System.out.println("Client " + clientId + " failed to connect: " + channelFuture.cause().getMessage());
                    }
                });
                future.channel().closeFuture().sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}

