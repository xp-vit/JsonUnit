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

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;

public class JsonUnitResultMatchers {
    private final String path;

    private JsonUnitResultMatchers(String path) {
        this.path = path;
    }

    public static JsonUnitResultMatchers json() {
        return new JsonUnitResultMatchers("");
    }

    public JsonUnitResultMatchers node(String path) {
        return new JsonUnitResultMatchers(path);
    }

    public ResultMatcher isEqualTo(final Object expected) {
        return new ResultMatcher() {
            public void match(MvcResult result) throws Exception {
                Diff diff =  create(expected, result.getResponse().getContentAsString(), "actual", path, Configuration.empty());
                if (!diff.similar()) {
                        failWithMessage(diff.differences());
                }
            }
        };
    }

    private void failWithMessage(String message) {
        throw new AssertionError(message);
    }
}
