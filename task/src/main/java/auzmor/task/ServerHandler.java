package auzmor.task;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

public class ServerHandler extends SimpleChannelInboundHandler<Object> {

	private HttpRequest request;

	private final StringBuilder buf = new StringBuilder();

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(msg.getClass().getName());
		if (msg instanceof DefaultHttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;

			buf.setLength(0);
			buf.append("===================================\r\n");

			buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
			buf.append("HOSTNAME: ").append(HttpHeaders.getHost(request, "unknown")).append("\r\n");
			buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");

			HttpHeaders headers = request.headers();
			if (!headers.isEmpty()) {
				for (Map.Entry<String, String> h : headers) {
					String key = h.getKey();
					String value = h.getValue();
					buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
				}
				buf.append("\r\n");
			}

			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
			Map<String, List<String>> params = queryStringDecoder.parameters();
			if (!params.isEmpty()) {
				for (Map.Entry<String, List<String>> p : params.entrySet()) {
					String key = p.getKey();
					List<String> vals = p.getValue();
					for (String val : vals) {
						buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
					}
				}
				buf.append("\r\n");
			}

			appendDecoderResult(buf, request);
		}

		if (msg instanceof DefaultHttpContent) {
			System.out.println("-------------");
			HttpContent httpContent = (HttpContent) msg;

			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				buf.append("CONTENT: ");
				buf.append(content.toString(CharsetUtil.UTF_8));

				if (isJson(content.toString(CharsetUtil.UTF_8))) {
					buf.append("Valid JSON");
					
					
					try {
						JSONParser jParse = new JSONParser();
						JSONObject data = (JSONObject)jParse.parse(content.toString(CharsetUtil.UTF_8));
						HandleProtoBuf adder = new HandleProtoBuf();
						System.out.println(data.toString());
						adder.addToProtobuf(content.toString(CharsetUtil.UTF_8));
						
					} catch (ParseException e) {
						System.out.println("xoxoxoxoxoxo");
					}
					

					
				} else
					buf.append("Usage ::  <expects json>");
				
				buf.append("\r\n");
				appendDecoderResult(buf, request);
			}

			if (msg instanceof DefaultLastHttpContent) {
				buf.append("END OF CONTENT\r\n");

				LastHttpContent trailer = (LastHttpContent) msg;
				if (!trailer.trailingHeaders().isEmpty()) {
					buf.append("\r\n");
					for (String name : trailer.trailingHeaders().names()) {
						for (String value : trailer.trailingHeaders().getAll(name)) {
							buf.append("TRAILING HEADER: ");
							buf.append(name).append(" = ").append(value).append("\r\n");
						}
					}
					buf.append("\r\n");
				}

				if (!writeResponse(trailer, ctx)) {
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}

		// System.out.println(buf.toString());
	}

	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.getDecoderResult();
		if (result.isSuccess()) {
			return;
		}

		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		boolean keepAlive = HttpHeaders.isKeepAlive(request);
		
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				currentObj.getDecoderResult().isSuccess() ? OK : BAD_REQUEST,
				Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		ctx.write(response);

		return keepAlive;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	
	private boolean isJson(String sResponse) {
		try {
			JSONParser jParse = new JSONParser();
			jParse.parse(sResponse);
			
		} catch (ParseException e) {
			return false;
		}
		
		return true;
	}
}
