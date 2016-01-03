package io.prometheus.client.exporter.common;


import org.junit.Test;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MetricMapperTest {

    private void assertMapTo(String name, List<String> labelNames, List<String> labelValues,
                             String help, MetricMapper.MetricMapping mapping) {
        assertEquals(mapping.getName(), name);
        assertEquals(mapping.getLabelNames(), labelNames);
        assertEquals(mapping.getLabelValues(), labelValues);
        assertEquals(mapping.getHelp(), help);
    }

    @Test
    public void testBasicMapping() {
        String rules = "---\n" +
                "rules:\n" +
                " - name: http_requests_errors\n" +
                "   pattern: ^RequestErrors$\n" +
                " - name: foo_bar_bazz\n" +
                "   pattern: foo_bar_bazz\n" +
                "   attrNameSnakeCase: true\n" +
                " - name: http_requests\n" +
                "   help: This an help message.\n" +
                "   pattern: HttpRequest(.*)\n" +
                "   labels: \n" +
                "     status: $1\n" +
                "     foo: bar\n";
        MetricMapper mapper = MetricMapper.load(rules);
        assertMapTo("http_requests", Arrays.asList("foo", "status"), Arrays.asList("bar", "Buzzy"),
                "This an help message.", mapper.map("HttpRequestBuzzy"));
        assertMapTo("http_requests_errors", new ArrayList<String>(), new ArrayList<String>(), "",
                mapper.map("RequestErrors"));
        assertMapTo("foo_bar_bazz", new ArrayList<String>(), new ArrayList<String>(), "",
                mapper.map("FooBarBazz"));
        assertMapTo("AnotherNonMatchedRequest", new ArrayList<String>(), new ArrayList<String>(), "",
                mapper.map("AnotherNonMatchedRequest"));

    }

    @Test
    public void testLowerCaseMapping() {
        String rules = "---\n" +
                "lowercaseOutputName: true\n" +
                "lowercaseOutputLabelNames: true\n" +
                "rules:\n" +
                " - name: http_requests_errors\n" +
                "   pattern: ^RequestErrors$\n" +
                " - name: http_requests\n" +
                "   pattern: HttpRequest(.*)\n" +
                "   labels: \n" +
                "     Status: $1\n" +
                "     foo: bar\n";
        MetricMapper mapper = MetricMapper.load(rules);
        assertMapTo("http_requests", Arrays.asList("status", "foo"), Arrays.asList("Buzzy", "bar"),
                "", mapper.map("HttpRequestBuzzy"));
        assertMapTo("http_requests_errors", new ArrayList<String>(), new ArrayList<String>(), "",
                mapper.map("RequestErrors"));
        assertMapTo("anothernonmatchedrequest", new ArrayList<String>(), new ArrayList<String>(), "",
                mapper.map("AnotherNonMatchedRequest"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceToEmptyName() {
        String rules = "---\n" +
                "rules:\n" +
                " - name: $1\n" +
                "   pattern: ^RequestErrors(.*)$\n";
        MetricMapper mapper = MetricMapper.load(rules);
        mapper.map("RequestErrors");
    }

    @Test(expected = YAMLException.class)
    public void testLoadInvalidYaml() {
        MetricMapper.load("{invali");
    }

    @Test
    public void testLoadDefaultMapping() {
        MetricMapper mapper = MetricMapper.load();
        assertMapTo("AnotherNonMatchedRequest", new ArrayList<String>(), new ArrayList<String>(), "",
                mapper.map("AnotherNonMatchedRequest"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidRuleWithNoName() {
        String rules = "---\n" +
                "rules:\n" +
                " - name: foobar\n";
        MetricMapper.load(new StringReader(rules));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseHelpWithNoName() {
        String rules = "---\n" +
                "rules:\n" +
                " - help: foobar\n";
        MetricMapper.load(new StringReader(rules));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLabelNamesWithNoName() {
        String rules = "---\n" +
                "rules:\n" +
                " - labels:\n" +
                "     foo: bar\n";
        MetricMapper.load(new StringReader(rules));
    }
}
