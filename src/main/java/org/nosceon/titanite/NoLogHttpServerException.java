package org.nosceon.titanite;

/**
 * @author Johan Siebens
 */
public final class NoLogHttpServerException extends RuntimeException {

    private Response response;

    public NoLogHttpServerException(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

}
