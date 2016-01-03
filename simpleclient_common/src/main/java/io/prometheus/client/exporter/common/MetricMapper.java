package io.prometheus.client.exporter.common;

import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Map origin names with a target name, label names and values according a specified configuration.
 */
public class MetricMapper {

    private static final Pattern snakeCasePattern = Pattern.compile("([a-z0-9])([A-Z])");
    private final boolean lowerCaseOutputNames;
    private final boolean lowerCaseOutputLabelNames;
    private final Pattern unsafeChars = Pattern.compile("[^a-zA-Z0-9:_]");
    private final Pattern multipleUnderscores = Pattern.compile("__+");
    Map<String, MetricMapping> mappingCache;
    private ArrayList<Rule> rules = new ArrayList<Rule>();

    /**
     * @param rules                    a list of mapping rules to apply.
     * @param lowerCaseOutputNames     lowercase metric names.
     * @param lowerCaseOutpuLabelNames lowercase metric labels names.
     */
    public MetricMapper(ArrayList<Rule> rules, boolean lowerCaseOutputNames, boolean lowerCaseOutpuLabelNames) {
        this.rules = rules;
        this.mappingCache = new HashMap<String, MetricMapping>();
        this.lowerCaseOutputNames = lowerCaseOutputNames;
        this.lowerCaseOutputLabelNames = lowerCaseOutpuLabelNames;
    }

    /**
     * Replace invalid chars to underscore and replace multiple underscores with one underscore.
     *
     * @param s a metric name or label name.
     * @return a sanitized name.
     */
    private String safeName(String s) {
        // Change invalid chars to underscore, and merge underscores.
        return multipleUnderscores.matcher(unsafeChars.matcher(s).replaceAll("_")).replaceAll("_");
    }

    /**
     * Map a metric to target mapping.
     *
     * @param metricName
     * @return a mapping for the specified metric name.
     */
    public MetricMapping map(String metricName) {
        if (!mappingCache.containsKey(metricName)) {
            mappingCache.put(metricName, process(metricName));
        }
        return mappingCache.get(metricName);
    }

    /**
     * Process rules for the specified metric name and map it with label names, values and
     * help according to the first matched rule.
     *
     * @param metricName
     * @return a MetricMapping associated with a metricName.
     */
    public MetricMapping process(String metricName) {
        String targetMetricName;
        final String targetHelp;
        final String snakeCaseMetricName = snakeCasePattern.matcher(metricName).replaceAll("$1_$2").toLowerCase();

        for (Rule rule : rules) {
            Matcher matcher = null;
            String matchName = (rule.attrNameSnakeCase ? snakeCaseMetricName : metricName);
            if (rule.pattern != null) {
                matcher = rule.pattern.matcher(matchName);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // Replace matches in help if a help was specified
            targetHelp = (rule.help != null) ? matcher.replaceAll(rule.help) : "";
            // Replace matches in metric rule name if specified. Sanitize name
            targetMetricName = safeName((rule.name == null) ? metricName : matcher.replaceAll(rule.name));
            if (targetMetricName.isEmpty()) {
                throw new IllegalArgumentException("Empty metric name. Original metric name: " + metricName);
            }
            if (this.lowerCaseOutputNames) {
                targetMetricName = targetMetricName.toLowerCase();
            }
            ArrayList<String> labelNames = new ArrayList<String>();
            ArrayList<String> labelValues = new ArrayList<String>();
            if (rule.labelNames != null) {
                for (int i = 0; i < rule.labelNames.size(); i++) {
                    final String unsafeLabelName = rule.labelNames.get(i);
                    final String labelValReplacement = rule.labelValues.get(i);
                    try {
                        String labelName = safeName(matcher.replaceAll(unsafeLabelName));
                        String labelValue = matcher.replaceAll(labelValReplacement);
                        if (this.lowerCaseOutputLabelNames) {
                            labelName = labelName.toLowerCase();
                        }
                        if (!labelName.isEmpty() && !labelValue.isEmpty()) {
                            labelNames.add(labelName);
                            labelValues.add(labelValue);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(
                                format("Matcher '%s' unable to use: '%s' value: '%s'", matcher, unsafeLabelName, labelValReplacement), e);
                    }
                }
            }
            return new MetricMapping(targetMetricName, labelNames, labelValues, targetHelp);
        }
        return MetricMapping.defaultMapping((lowerCaseOutputNames ? metricName.toLowerCase() : metricName));
    }


    public static MetricMapper load(String yamlConfig) {
        return load((Map<String, Object>) new Yaml().load(yamlConfig));
    }

    public static MetricMapper load(Reader reader) {
        return load((Map<String, Object>) new Yaml().load(reader));
    }

    public static MetricMapper load() {
        return load((Map<String, Object>) null);
    }

    public static MetricMapper load(Map<String, Object> config) {
        boolean lowercaseOutputName = false;
        boolean lowercaseOutputLabelNames = false;
        ArrayList<Rule> rules = new ArrayList<Rule>();

        if (config == null) {  //Yaml config empty, set config to empty map.
            config = new HashMap<String, Object>();
        }
        if (config.containsKey("lowercaseOutputName")) {
            lowercaseOutputName = (Boolean) config.get("lowercaseOutputName");
        }
        if (config.containsKey("lowercaseOutputLabelNames")) {
            lowercaseOutputLabelNames = (Boolean) config.get("lowercaseOutputLabelNames");
        }

        if (config.containsKey("rules")) {
            List<Map<String, Object>> configRules = (List<Map<String, Object>>) config.get("rules");
            for (Map<String, Object> ruleObject : configRules) {
                Map<String, Object> yamlRule = ruleObject;
                Rule rule = new Rule();
                if (yamlRule.containsKey("pattern")) {
                    rule.pattern = Pattern.compile("^.*" + (String) yamlRule.get("pattern") + ".*$");
                }
                if (yamlRule.containsKey("name")) {
                    rule.name = (String) yamlRule.get("name");
                }
                if (yamlRule.containsKey("attrNameSnakeCase")) {
                    rule.attrNameSnakeCase = (Boolean) yamlRule.get("attrNameSnakeCase");
                }
                if (yamlRule.containsKey("help")) {
                    rule.help = (String) yamlRule.get("help");
                }
                if (yamlRule.containsKey("labels")) {
                    TreeMap labels = new TreeMap((Map<String, Object>) yamlRule.get("labels"));
                    rule.labelNames = new ArrayList<String>();
                    rule.labelValues = new ArrayList<String>();
                    for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) labels.entrySet()) {
                        rule.labelNames.add(entry.getKey());
                        rule.labelValues.add((String) entry.getValue());
                    }
                }

                // Validation.
                if ((rule.labelNames != null || rule.help != null) && rule.name == null) {
                    throw new IllegalArgumentException("Must provide name, if help or labels are given: " + yamlRule);
                }
                if (rule.name != null && rule.pattern == null) {
                    throw new IllegalArgumentException("Must provide pattern, if name is given: " + yamlRule);
                }
                rules.add(rule);
            }
        } else {
            // Default to a single default rule.
            rules.add(new Rule());
        }

        return new MetricMapper(rules, lowercaseOutputName, lowercaseOutputLabelNames);
    }

    /**
     * A mapping rule.
     */
    private static class Rule {
        Pattern pattern;
        String name;
        String help;
        boolean attrNameSnakeCase;
        ArrayList<String> labelNames;
        ArrayList<String> labelValues;
    }

    /**
     * Contains prometheus metric name, label names and values for a source metric name
     */
    public static class MetricMapping {

        private ArrayList<String> labelNames;
        private ArrayList<String> labelValues;
        private String name;
        private String help;

        public MetricMapping(String name, ArrayList<String> labelNames, ArrayList<String> labelValues, String help) {
            this.labelNames = labelNames;
            this.name = name;
            this.labelValues = labelValues;
            this.help = help;
        }

        public ArrayList<String> getLabelNames() {
            return labelNames;
        }

        public ArrayList<String> getLabelValues() {
            return labelValues;
        }

        public String getName() {
            return name;
        }

        public String getHelp() {
            return help;
        }

        /**
         * Return a default mapping for a metric.
         * This mapping contains unmodified metric name, empty label names, values and help.
         *
         * @param name
         * @return the default MetricMapping associated to this metricName.
         */
        public static MetricMapping defaultMapping(String name) {
            return new MetricMapping(name, new ArrayList<String>(), new ArrayList<String>(), "");
        }
    }
}
