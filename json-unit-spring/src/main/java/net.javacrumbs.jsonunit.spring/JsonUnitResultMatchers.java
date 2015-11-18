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
import net.javacrumbs.jsonunit.core.internal.Node;
import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeExists;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonUnitResultMatchers {
    private final String path;
    private final Configuration configuration = Configuration.empty();

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
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                Diff diff = createDiff(expected, actual);
                if (!diff.similar()) {
                    failWithMessage(diff.differences());
                }
            }
        };
    }

    public ResultMatcher isNotEqualTo(final String expected) {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                Diff diff = createDiff(expected, actual);
                if (diff.similar()) {
                    failWithMessage("JSON is equal.");
                }
            }
        };
    }

    /**
     * Fails if the node exists.
     *
     * @return
     */
    public ResultMatcher isAbsent() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                if (nodeExists(actual, path)) {
                    failWithMessage("Node \"" + path + "\" is present.");
                }
            }
        };
    }

    /**
     * Fails if the node is missing.
     */
    public ResultMatcher isPresent() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                if (!nodeExists(actual, path)) {
                    failWithMessage("Node \"" + path + "\" is missing.");
                }
            }
        };
    }


    /**
     * Fails if the selected JSON is not an Array or is not present.
     *
     * @return
     */
    public ResultMatcher isArray() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent();
                Node node = getNode(actual, path);
                if (node.getNodeType() != ARRAY) {
                    failOnType(node, "an array");
                }
            }
        };
    }

    /**
     * Fails if the selected JSON is not an Object or is not present.
     */
    public ResultMatcher isObject() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent();
                Node node = getNode(actual, path);
                if (node.getNodeType() != OBJECT) {
                    failOnType(node, "an object");
                }
            }
        };
    }

    /**
     * Fails if the selected JSON is not a String or is not present.
     */
    public ResultMatcher isString() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent();
                Node node = getNode(actual, path);
                if (node.getNodeType() != STRING) {
                    failOnType(node, "a string");
                }
            }
        };
    }


    /**
     * Matches the node using Hamcrest matcher.
     * <p/>
     * <ul>
     * <li>Numbers are mapped to BigDecimal</li>
     * <li>Arrays are mapped to a Collection</li>
     * <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     *
     * @param matcher
     * @return
     */
    public ResultMatcher matches(final Matcher<?> matcher) {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent();
                Node node = getNode(actual, path);
                assertThat("Node \"" + path + "\" does not match.", node.getValue(), (Matcher<? super Object>) matcher);
            }
        };

    }

    private void failOnType(Node node, final String type) {
        failWithMessage("Node \"" + path + "\" is not " + type + ". The actual value is '" + node + "'.");
    }

    private void failWithMessage(String message) {
        throw new AssertionError(message);
    }

    private static abstract class AbstractResultMatcher implements ResultMatcher {
        private final String path;
        private final Configuration configuration;

        protected AbstractResultMatcher(String path, Configuration configuration) {
            this.path = path;
            this.configuration = configuration;
        }

        public void match(MvcResult result) throws Exception {
            Object actual = result.getResponse().getContentAsString();
            doMatch(actual);
        }

        protected Diff createDiff(Object expected, Object actual) {
            return create(expected, actual, "actual", path, configuration);
        }

        protected abstract void doMatch(Object actual);
    }


}
