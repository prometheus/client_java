package io.prometheus.client.filter;

public class SlashLimitingPathMapper implements PathToLabelMapper {
    private int pathComponents = 1;

    SlashLimitingPathMapper(int pathComponents) {
        this.pathComponents = pathComponents;
    }

    @Override
    public String getLabel(String path) {
        if (path == null || pathComponents < 1) {
            return path;
        }
        int count = 0;
        int i = -1;
        do {
            i = path.indexOf("/", i + 1);
            if (i < 0) {
                // Path is longer than specified pathComponents.
                return path;
            }
            count++;
        } while (count <= pathComponents);

        return path.substring(0, i);
    }
}
