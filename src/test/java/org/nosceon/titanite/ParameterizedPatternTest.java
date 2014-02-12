package org.nosceon.titanite;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Johan Siebens
 */
public class ParameterizedPatternTest {

    @Test
    public void testParameterizedPattern() {
        ParameterizedPattern pattern = new ParameterizedPattern("/hello/{name}/id/{id}");
        ParameterizedPattern.Matcher matcher = pattern.matcher("/hello/world/id/123");
        assertThat(matcher.matches(), is(true));
        assertThat(matcher.parameters().size(), equalTo(2));
        assertThat(matcher.parameters().get("name"), equalTo("world"));
        assertThat(matcher.parameters().get("id"), equalTo("123"));

        assertThat(pattern.matcher("/lorem/ipsum").matches(), is(false));
    }

    @Test
    public void testSimplePattern() {
        ParameterizedPattern pattern = new ParameterizedPattern("/hello/world");
        ParameterizedPattern.Matcher matcher = pattern.matcher("/hello/world");
        assertThat(matcher.matches(), is(true));
        assertThat(matcher.parameters().size(), equalTo(0));

        assertThat(pattern.matcher("/lorem/ipsum").matches(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIdentifierOccursMoreThanOnce() {
        new ParameterizedPattern("/hello/{name}/id/{name}");
    }

}
