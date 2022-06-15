package com.ruiyun.jvppeteer.core.env;

import com.ruiyun.jvppeteer.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Environment implementation suitable for use in 'standard'.
 * <p>
 * search order:
 * <ul>
 *  <li>system properties
 *  <li>system environment variables
 * </ul>
 *
 * <p>For example, a call to {@code getEnv("foo.bar")} will attempt to find a value
 *  for the original property or any 'equivalent' property, returning the first found:
 *  <ul>
 *   <li>{@code foo.bar} - the original name</li>
 *   <li>{@code foo_bar} - with underscores for periods (if any)</li>
 *   <li>{@code FOO.BAR} - original, with upper case</li>
 *   <li>{@code FOO_BAR} - with underscores and upper case</li>
 *  </ul>
 *
 * @author sage.xue
 * @date 2022/6/10 16:15
 */
public class StandardEnvironment implements Environment {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardEnvironment.class);

    private static final Map<String, String> systemPropertiesSourceMap = new HashMap<>();
    private static final Map<String, String> systemEnvSourceMap = new HashMap<>();

    static {
        Properties systemProperties = System.getProperties();
        systemProperties.entrySet().forEach(systemPropertiesEntry -> {
            String key = systemPropertiesEntry.getKey().toString();
            String value = systemPropertiesEntry.getValue() == null ? null : systemPropertiesEntry.getValue().toString();
            systemPropertiesSourceMap.put(key, value);
        });

        systemEnvSourceMap.putAll(System.getenv());
    }

    @Override
    public String getEnv(String name) {
        // order 0: find from system property
        String value = this.getPropertyValue(name, systemPropertiesSourceMap, "JvppeteerSystemProperties");

        // order 1: find from env
        if (value == null) {
            value = this.getPropertyValue(name, systemEnvSourceMap, "JvppeteerSystemEnv");
        }

        // order 3: find from LaunchOptions

        return value;
    }

    private String getPropertyValue(String name, Map<String, String> source, String sourceName) {
        String actualName = resolvePropertyName(name, source);
        if (actualName == null) {
            // retry to uppercase
            actualName = resolvePropertyName(name.toUpperCase(), source);
            if (actualName == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("PropertySource ' " + sourceName + " ' does not contain property '" + name);
                }
                return null;
            }
        }

        if (LOGGER.isDebugEnabled() && !name.equals(actualName)) {
            LOGGER.debug("PropertySource ' " + sourceName + " ' does not contain property '" + name +
                    "', but found equivalent '" + actualName + "'");
        }

        return source.get(actualName);
    }

    private final String resolvePropertyName(String name, Map<String, String> source) {
        if (source.containsKey(name)) {
            return name;
        }

        // check name with just dots replaced
        String noDotName = name.replace(".", "_");
        if (!name.equals(noDotName) && source.containsKey(noDotName)) {
            return noDotName;
        }

        // check name with just hyphens replaced
        String noHyphenName = name.replace("-", "_");
        if (!name.equals(noHyphenName) && source.containsKey(noHyphenName)) {
            return noHyphenName;
        }

        // Check name with dots and hyphens replaced
        String noDotAndHyphenName = noDotName.replace("-", "_");
        if (!name.equals(noDotAndHyphenName) && source.containsKey(noDotAndHyphenName)) {
            return noDotAndHyphenName;
        }

        // give up
        return null;
    }
}
