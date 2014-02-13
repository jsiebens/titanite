package org.nosceon.titanite;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
public final class MultiPart {

    private FileUpload fileUpload;

    public MultiPart(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public InputStream content() {
        return
            propagate(() -> {
                if (fileUpload.isInMemory()) {
                    return new ByteBufInputStream(fileUpload.content());
                }
                else {
                    return new FileInputStream(fileUpload.getFile());

                }
            });
    }

    public long length() {
        return fileUpload.length();
    }

    public Optional<String> contentType() {
        return ofNullable(fileUpload.getContentType());
    }

    public String filename() {
        return fileUpload.getFilename();
    }

    public boolean renameTo(File dest) {
        return propagate(() -> fileUpload.renameTo(dest));
    }

}
