package org.nosceon.titanite;

/**
 * @author Johan Siebens
 */
public final class NoLogHttpServerException extends RuntimeException {

    private Response response;

    public NoLogHttpServerException(Response response) {
        this.response = response;
    }

    public NoLogHttpServerException(Throwable cause, Response response) {
        super(cause);
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

}
