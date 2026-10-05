package com.github.thedumbledodo.blueprint.config.yaml;

import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.format.ConfigFormat;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.representer.Representer;

import java.io.StringWriter;
import java.util.*;

public final class YamlFormat implements ConfigFormat {

    private static final List<String> EXTENSIONS = List.of("yml", "yaml");
    private static final int INLINE_LIST_WIDTH = 60;

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public Map<String, Object> read(String content) {
        if (content == null || content.isBlank()) {
            return new LinkedHashMap<>();
        }

        final Object loaded;

        try {
            loaded = createYaml().load(content);

        } catch (YAMLException exception) {
            throw new ConfigException("invalid YAML: " + exception.getMessage(), exception);
        }

        if (loaded == null) {
            return new LinkedHashMap<>();
        }

        if (!(loaded instanceof Map<?, ?> map)) {
            throw new ConfigException("expected a section at the top of the file but got " + loaded.getClass().getSimpleName());
        }
        return (Map<String, Object>) ConfigSection.toPlain(map);
    }

    @Override
    public String write(ConfigSection section) {
        final Yaml yaml = createYaml();
        final Node node = yaml.represent(section.toMap());

        if (node instanceof MappingNode mapping) {
            inlineShortLists(mapping);
            addComments(mapping, section);
            addBlankLines(mapping);
            addHeader(mapping, section.getHeader());
        }

        final StringWriter writer = new StringWriter();

        yaml.serialize(node, writer);
        return writer.toString();
    }

    private Yaml createYaml() {
        final LoaderOptions loaderOptions = new LoaderOptions();
        final DumperOptions dumperOptions = new DumperOptions();

        dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setProcessComments(true);
        dumperOptions.setSplitLines(false);

        return new Yaml(new SafeConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions);
    }

    private void addComments(MappingNode mapping, ConfigSection section) {
        for (NodeTuple tuple : mapping.getValue()) {
            if (!(tuple.getKeyNode() instanceof ScalarNode keyNode)) {
                continue;
            }

            final String key = keyNode.getValue();
            final List<String> comments = section.getComments(key);

            if (!comments.isEmpty()) {
                keyNode.setBlockComments(toCommentLines(comments));
            }

            if (tuple.getValueNode() instanceof MappingNode child && section.get(key) instanceof ConfigSection childSection) {
                addComments(child, childSection);
            }
        }
    }

    private void inlineShortLists(Node node) {
        if (node instanceof MappingNode mapping) {
            for (NodeTuple tuple : mapping.getValue()) {
                inlineShortLists(tuple.getValueNode());
            }
            return;
        }

        if (!(node instanceof SequenceNode sequence)) {
            return;
        }

        int width = 2;
        boolean scalars = true;

        for (Node item : sequence.getValue()) {
            inlineShortLists(item);

            if (!(item instanceof ScalarNode scalar) || scalar.getValue().contains("\n")) {
                scalars = false;
                continue;
            }
            width += scalar.getValue().length() + 2;
        }

        if (scalars && width <= INLINE_LIST_WIDTH) {
            sequence.setFlowStyle(FlowStyle.FLOW);
        }
    }

    private void addBlankLines(Node node) {
        if (node instanceof SequenceNode sequence) {
            for (Node item : sequence.getValue()) {
                addBlankLines(item);
            }
            return;
        }

        if (!(node instanceof MappingNode mapping)) {
            return;
        }

        boolean previousIsBlock = false;

        for (int i = 0; i < mapping.getValue().size(); i++) {
            final NodeTuple tuple = mapping.getValue().get(i);
            final boolean block = isBlock(tuple.getValueNode());

            if (i > 0 && (block || previousIsBlock)) {
                final Node key = tuple.getKeyNode();
                final List<CommentLine> lines = new ArrayList<>();

                lines.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));

                if (key.getBlockComments() != null) {
                    lines.addAll(key.getBlockComments());
                }
                key.setBlockComments(lines);
            }

            addBlankLines(tuple.getValueNode());
            previousIsBlock = block;
        }
    }

    private boolean isBlock(Node node) {
        if (node instanceof MappingNode mapping) {
            return !mapping.getValue().isEmpty();
        }
        return node instanceof SequenceNode sequence && sequence.getFlowStyle() == FlowStyle.BLOCK && !sequence.getValue().isEmpty();
    }

    private void addHeader(MappingNode mapping, List<String> header) {
        if (header.isEmpty() || mapping.getValue().isEmpty()) {
            return;
        }

        final Node firstKey = mapping.getValue().getFirst().getKeyNode();
        final List<CommentLine> lines = new ArrayList<>(toCommentLines(header));

        lines.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));

        if (firstKey.getBlockComments() != null) {
            lines.addAll(firstKey.getBlockComments());
        }
        firstKey.setBlockComments(lines);
    }

    private List<CommentLine> toCommentLines(List<String> comments) {
        final List<CommentLine> lines = new ArrayList<>(comments.size());

        for (String comment : comments) {
            final String value = comment.isEmpty() ? "" : " " + comment;

            lines.add(new CommentLine(null, null, value, CommentType.BLOCK));
        }
        return lines;
    }
}
