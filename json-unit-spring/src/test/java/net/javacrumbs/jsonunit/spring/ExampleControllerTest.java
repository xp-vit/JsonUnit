/**
 * Copyright 2009-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.spring;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static net.javacrumbs.jsonunit.spring.JsonUnitResultMatchers.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
public class ExampleControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void shouldPassIfEquals() throws Exception {
        exec()
            .andExpect(json().isEqualTo("{\"result\":{\"string\":\"stringValue\"}}"));
    }

    @Test
    public void shouldFailIfDoesNotEqual() throws Exception {
        try {
            exec()
                .andExpect(json().isEqualTo("{\"result\":{\"string\":\"stringValue2\"}}"));
            doFail();
        } catch (AssertionError e) {
            assertEquals(e.getMessage(),
                "JSON documents are different:\n" +
                "Different value found in node \"result.string\". Expected \"stringValue2\", got \"stringValue\".\n");
        }
    }

    @Test
    public void shouldFailIfNodeDoesNotEqual() throws Exception {
        try {
            exec()
                .andExpect(json().node("result").isEqualTo("{\"string\":\"stringValue2\"}"));
            doFail();
        } catch (AssertionError e) {
            assertEquals(e.getMessage(),
                "JSON documents are different:\n" +
                "Different value found in node \"result.string\". Expected \"stringValue2\", got \"stringValue\".\n");
        }
    }

    private ResultActions exec() throws Exception {
        return this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON));
    }

    private void doFail() {
        fail("Exception expected");
    }

}
