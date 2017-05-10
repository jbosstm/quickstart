package demo.util;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProgArgs {
    private Map<String, String> options;

    public ProgArgs(String[] args) {
        options = new HashMap<>();

        Arrays.stream(args).forEach(this::addOption);
    }

    private void addOption(String opt) {
        if (opt != null && opt.contains("=")) {
            String [] pair = opt.split("=");

            options.put(pair[0], pair.length == 1 ? "" : pair[1]);
        }
    }

    public int getIntOption(String optionName, int defaultValue) {
        return options.containsKey(optionName) ?
                Integer.parseInt(options.get(optionName)) :
                defaultValue;
    }

    public Boolean getBooleanOption(String optionName, boolean defaultValue) {
        return options.containsKey(optionName) ?
                Boolean.parseBoolean(options.get(optionName)) :
                defaultValue;
    }

    public String getStringOption(String optionName, String defaultValue) {
        return options.containsKey(optionName) ?
                options.get(optionName) :
                defaultValue;
    }
}
