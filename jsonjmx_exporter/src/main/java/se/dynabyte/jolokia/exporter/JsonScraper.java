package se.dynabyte.jolokia.exporter;

import org.yaml.snakeyaml.Yaml;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Content;
import us.monoid.web.Resty;
import us.monoid.web.TextResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 3/5/17.
 */
public class JsonScraper {
    private final Map<String, Object> config;
    private final Resty httpClient;
    private JSONArray metrics;

    private final String target;

    public JsonScraper(Map<String, Object> config, Resty httpClient, JSONArray metrics) {
        this.config = config;
        this.httpClient = httpClient;
        this.metrics = metrics;

        if (config == null) {
            throw new IllegalStateException("Configuration for component can not be null");
        }
        target = (String) config.get("target_url");
        httpClient.authenticate(target, (String) config.get("user"), ((String) config.get("pass")).toCharArray());
    }


    public List<JmxMetric> scrape() {
        List<JmxMetric> metrics2 = new ArrayList<JmxMetric>();

        try {
            for (int i=0; i<metrics.length(); i++) {
                JSONObject metric = (JSONObject) metrics.get(i);
                String metricName = metric.getString("metric-name");
                String metricType = metric.getString("value-type");

                JSONObject jmxMetric = metric.getJSONObject("jmx-property");

                Content c = new Content("application/json", jmxMetric.toString().getBytes());
                TextResource json = httpClient.text(target, c);

                String value = "";
                JSONObject response = new JSONObject(json.toString());
                if (response.has("stacktrace")) {
                    System.out.println("Did not find jmx metrics for " + metricName);
                    continue;
                }

                if ("".equals(metric.getString("value-name"))) {
                    value = response.getString("value");
                } else {
                    value = response.getJSONObject("value").getString(metric.getString("value-name"));
                }

                metrics2.add(new JmxMetric(metricName, metricType, value));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return metrics2;
    }
}
