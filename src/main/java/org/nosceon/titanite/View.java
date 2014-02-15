package org.nosceon.titanite;

/**
 * @author Johan Siebens
 */
public abstract class View {

    public final String template;

    protected View(String template) {
        this.template = template;
    }

}
