package org.nosceon.titanite;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Johan Siebens
 */
public abstract class View {

    @JsonIgnore
    public final String template;

    protected View(String template) {
        this.template = template;
    }

}
