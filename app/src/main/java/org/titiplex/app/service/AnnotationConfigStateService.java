package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class AnnotationConfigStateService {
    private AnnotationConfig currentConfig = new AnnotationConfig();
    private Path currentPath;

    public AnnotationConfig getCurrentConfig() {
        return currentConfig;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public void load(Path path) throws IOException {
        currentConfig = new AnnotationConfigLoader().load(path);
        currentPath = path;
    }

    public void reset() {
        currentConfig = new AnnotationConfig();
        currentPath = null;
    }
}