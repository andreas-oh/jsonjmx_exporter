package se.dynabyte.jolokia.exporter;

import io.prometheus.client.Collector;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Resty;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonCollector extends Collector {

    private final JsonScraper scraper;

    public JsonCollector(File configFile, File metricsFile) throws IOException, JSONException {

        Map<String, Object> configMap = (Map<String, Object>)new Yaml().load(new FileReader(configFile));

        InputStream metricsIs = new FileInputStream(metricsFile);
        JSONArray payload = new JSONArray(IOUtils.toString(metricsIs));

        scraper = new JsonScraper(configMap, new Resty(), payload);
    }

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> metricFamilySamples = new ArrayList<MetricFamilySamples>();

        List<JmxMetric> metrics = scraper.scrape();

        for (JmxMetric metric : metrics) {
            //TODO: Fix labels and such in JmxMetric
            List<String> labelNames = Arrays.asList("tag1", "tag2");
            List<String> labelValues = Arrays.asList("tagValue1", "tagValue2");

            double value = Double.parseDouble(metric.getValue());

            //String name, List<String> labelNames, List<String> labelValues, double value
            MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(metric.getName(), labelNames, labelValues, value);
            List<MetricFamilySamples.Sample> samples = Arrays.asList(sample);

            //String name, Collector.Type type, String help, List<Collector.MetricFamilySamples.Sample> samples
            metricFamilySamples.add(new MetricFamilySamples(sample.name, toPromType(metric.getType()), "Not available", samples));
        }

        return metricFamilySamples;
    }

    private Type toPromType(String type) {
        switch (type) {
            case "guage":
                return Type.GAUGE;
            case "counter":
                return Type.COUNTER;
            case "histogram":
                return Type.HISTOGRAM;
            case "summary":
                return Type.SUMMARY;
            default:
                return Type.UNTYPED;
        }
    }
}
