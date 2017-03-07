package se.dynabyte.jolokia.exporter;

public class JmxMetric {
    private final String name;
    private final String value;
    private final String type;

    public JmxMetric(String name, String metricType, String value) {
        this.name = name;
        this.type = metricType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "JmxMetric2 [name=" + name + ", value=" + value + "]";
    }

}
