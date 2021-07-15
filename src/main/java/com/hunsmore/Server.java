package com.hunsmore;

import com.google.common.base.Utf8;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author htf
 */
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new Server(port).run(new CommandDispatcher(new DumbKeyValueServiceImpl()));
    }

    public void run(final CommandDispatcher commandDispatcher) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Decoder(), new RequestHandler(commandDispatcher));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * Handles a server-side channel.
     */
    public static class RequestHandler extends ChannelInboundHandlerAdapter {
        private final CommandDispatcher commandDispatcher;

        public RequestHandler(CommandDispatcher commandDispatcher) {
            this.commandDispatcher = commandDispatcher;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws ExecutionException, InterruptedException {
            String command = (String) msg;
            Future<Object> future = commandDispatcher.dispatchAndRun(command);
            String result = future.get().toString();
            System.out.println(result);
            ByteBuf encoded = ctx.alloc().buffer(result.length());
            encoded.writeBytes(result.getBytes(StandardCharsets.UTF_8));
            ctx.write(encoded);
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }

    public static class Decoder extends ByteToMessageDecoder {

        public static final int UTF8_BYTES = 2;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
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

            out.add(stringBuilder.toString());
        }
    }
}
