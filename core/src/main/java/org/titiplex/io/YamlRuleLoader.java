package org.titiplex.io;

import org.titiplex.rules.CorrectionRule;
import org.titiplex.rules.PythonStyleYamlRuleLoader;

import java.io.InputStream;
import java.util.List;

public final class YamlRuleLoader {
    public List<CorrectionRule> load(InputStream inputStream) {
        return new PythonStyleYamlRuleLoader().load(inputStream);
    }
}
