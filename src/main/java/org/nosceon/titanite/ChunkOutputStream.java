package org.nosceon.titanite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Johan Siebens
 */
final class ChunkOutputStream extends OutputStream {

    final ByteBuf buffer;

    final ChannelHandlerContext ctx;

    ChunkOutputStream(ChannelHandlerContext ctx, int chunksize) {
        if (chunksize < 1) {
            throw new IllegalArgumentException("Chunk size must be at least 1");
        }
        this.buffer = Unpooled.buffer(0, chunksize);
        this.ctx = ctx;
    }

    @Override
    public void write(int b) throws IOException {
        if (buffer.maxWritableBytes() < 1) {
            flush();
        }
        buffer.writeByte(b);
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int dataLengthLeftToWrite = len;
        int dataToWriteOffset = off;
        int spaceLeftInCurrentChunk;
        while ((spaceLeftInCurrentChunk = buffer.maxWritableBytes()) < dataLengthLeftToWrite) {
            buffer.writeBytes(b, dataToWriteOffset, spaceLeftInCurrentChunk);
            dataToWriteOffset = dataToWriteOffset + spaceLeftInCurrentChunk;
            dataLengthLeftToWrite = dataLengthLeftToWrite - spaceLeftInCurrentChunk;
            flush();
        }
        if (dataLengthLeftToWrite > 0) {
            buffer.writeBytes(b, dataToWriteOffset, dataLengthLeftToWrite);
        }
    }

    @Override
    public void flush() throws IOException {
        ctx.writeAndFlush(new DefaultHttpContent(buffer.copy()));
        buffer.clear();
        super.flush();
    }

}
