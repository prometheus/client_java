package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LabelsTest {

    @Test
    public void testSort() {
        List<Labels> labels = Arrays.asList(
                Labels.of("env", "prod", "status", "200"),
                Labels.of("env", "dev", "status", "200"),
                Labels.of("env", "prod", "status", "200", "exception", "none"),
                Labels.of("env", "prod", "status", "500", "exception", "IOException"),
                Labels.of("env", "prod", "status", "500", "exception", "IOException", "x", "1")
        );
        Collections.sort(labels);
        // Sort by label name first.
        // If all label names are equal, sort by label values.
        int i = 0;
        Assert.assertEquals("{env=\"prod\",exception=\"IOException\",status=\"500\"}", labels.get(i++).toString());
        Assert.assertEquals("{env=\"prod\",exception=\"none\",status=\"200\"}", labels.get(i++).toString());
        Assert.assertEquals("{env=\"prod\",exception=\"IOException\",status=\"500\",x=\"1\"}", labels.get(i++).toString());
        Assert.assertEquals("{env=\"dev\",status=\"200\"}", labels.get(i++).toString());
        Assert.assertEquals("{env=\"prod\",status=\"200\"}", labels.get(i++).toString());
    }
}
