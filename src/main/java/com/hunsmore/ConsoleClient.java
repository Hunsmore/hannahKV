package com.hunsmore;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author htf
 */
@Slf4j
public class ConsoleClient {
    public static void main(String[] args) throws InterruptedException {
        //connecting to server
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("localhost", 8080).sync();
//            ByteBuf encoded = ctx.alloc().buffer(result.length());
//            encoded.writeBytes(result.getBytes(StandardCharsets.UTF_8));
//            f.channel().writeAndFlush();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static class ClientHandler extends ChannelInboundHandlerAdapter {
        private final int UTF8_BYTES = 2;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            if (in.readableBytes() < UTF8_BYTES) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            do {
                int readableBytes = in.readableBytes();
                byte[] bytes = new byte[readableBytes];
                in.readBytes(bytes);
                stringBuilder.append(new String(bytes, StandardCharsets.UTF_8));
            } while (in.readableBytes() > UTF8_BYTES);

            System.out.println(stringBuilder);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
