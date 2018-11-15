package io.prometheus.client.dropwizard.samplebuilder;

import io.prometheus.client.dropwizard.samplebuilder.MapperConfig;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapperConfigTest {
    @Test
    public void setMatch_WHEN_ExpressionMatchesPattern_AllGood() {
        final MapperConfig mapperConfig = new MapperConfig();
        mapperConfig.setMatch("com.company.meter.*");
        assertEquals("com.company.meter.*", mapperConfig.getMatch());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMatch_WHEN_ExpressionDoesnNotMatchPattern_ThrowException() {
        final MapperConfig mapperConfig = new MapperConfig();
        mapperConfig.setMatch("com.company.meter.**.yay");
    }

    @Test
    public void setLabels_WHEN_ExpressionMatchesPattern_AllGood() {
        final MapperConfig mapperConfig = new MapperConfig();
        final Map<String, String> labels = new HashMap<String, String>();
        labels.put("valid", "${0}");
        mapperConfig.setLabels(labels);
        assertEquals(labels, mapperConfig.getLabels());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setLabels_WHEN_ExpressionDoesnNotMatchPattern_ThrowException() {
        final MapperConfig mapperConfig = new MapperConfig();
        final Map<String, String> labels = new HashMap<String, String>();
        labels.put("valid", "${0}");
        labels.put("not valid", "${0}");
        mapperConfig.setLabels(labels);
    }
}