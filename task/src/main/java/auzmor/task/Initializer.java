package auzmor.task;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

public class Initializer extends ChannelInitializer<SocketChannel> {

	public Initializer() {
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		p.addLast(new HttpRequestDecoder());
		p.addLast(new HttpResponseEncoder());
		p.addLast(new ServerHandler());
	}

}
