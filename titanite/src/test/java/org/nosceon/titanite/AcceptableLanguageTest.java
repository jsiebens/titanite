/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.nosceon.titanite.AcceptableLanguage.bestCandidate;

/**
 * @author Johan Siebens
 */
public class AcceptableLanguageTest {

    @Test
    public void test() {
        AcceptableLanguage ac = AcceptableLanguage.valueOf("en-US");
        assertThat(ac.getPrimaryTag(), equalTo("en"));
        assertThat(ac.getSubTags(), equalTo("US"));
        assertThat(ac.getQuality(), equalTo(1d));
    }

    @Test
    public void testWildcard() {
        AcceptableLanguage ac = AcceptableLanguage.valueOf("*");
        assertThat(ac.getPrimaryTag(), equalTo("*"));
        assertThat(ac.getSubTags(), nullValue());
    }

    @Test
    public void testWithQualityFactor() {
        AcceptableLanguage ac = AcceptableLanguage.valueOf("en-US; q=0.7");
        assertThat(ac.getPrimaryTag(), equalTo("en"));
        assertThat(ac.getSubTags(), equalTo("US"));
        assertThat(ac.getQuality(), equalTo(0.7d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidQualityFactory() {
        AcceptableLanguage.valueOf("en-US; q=1.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithMultipleParameters() {
        AcceptableLanguage.valueOf("en-US; q=0.7; a=b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardWithSubTag() {
        AcceptableLanguage.valueOf("*-en");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardWithEmptySubTag() {
        AcceptableLanguage.valueOf("*-");
    }

    @Test
    public void testWithoutSubTag() {
        AcceptableLanguage ac = AcceptableLanguage.valueOf("en");
        assertThat(ac.getPrimaryTag(), equalTo("en"));
        assertThat(ac.getSubTags(), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEmptySubTag() {
        AcceptableLanguage.valueOf("en-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrimaryTagWithInvalidChars() {
        AcceptableLanguage.valueOf("en12");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrimayTagWithInvalidLength() {
        AcceptableLanguage.valueOf("azertyuio");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubTagWithInvalidChars() {
        AcceptableLanguage.valueOf("en-US12");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubTagWithInvalidLength() {
        AcceptableLanguage.valueOf("en-azertyuio");
    }

    @Test
    public void testAccepts() {
        List<AcceptableLanguage> languages = AcceptableLanguage.valuesOf("da, en-gb;q=0.8, en;q=0.7");
        assertThat(AcceptableLanguage.accepts(languages, new Locale("en")), is(true));
        assertThat(AcceptableLanguage.accepts(languages, new Locale("da")), is(true));
        assertThat(AcceptableLanguage.accepts(languages, new Locale("en", "US")), is(true));
    }

    @Test
    public void testBestCandidate() {
        List<AcceptableLanguage> languages = AcceptableLanguage.valuesOf("da, en-gb;q=0.8, en;q=0.7");

        assertThat(bestCandidate(languages, Arrays.asList(new Locale("en", "US"), new Locale("en", "GB"))), equalTo(new Locale("en", "GB")));
        assertThat(bestCandidate(languages, Arrays.asList(new Locale("en", "US"), new Locale("da"))), equalTo(new Locale("da")));
        assertThat(bestCandidate(languages, Arrays.asList(new Locale("en", "US"), new Locale("en", "BE"))), equalTo(new Locale("en", "US")));
        assertThat(bestCandidate(languages, Arrays.asList(new Locale("nl", "BE"), new Locale("fr", "BE"))), nullValue());
    }

}
