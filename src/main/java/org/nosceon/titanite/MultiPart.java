/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.nosceon.titanite.HttpServerException.call;

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
            call(() -> {
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

    public String contentType() {
        return fileUpload.getContentType();
    }

    public String filename() {
        return fileUpload.getFilename();
    }

    public boolean renameTo(File dest) {
        return call(() -> fileUpload.renameTo(dest));
    }

}
