package com.example.jsonviewer;

import com.fasterxml.jackson.databind.JsonNode;

/** Holds node data for rendering and search. All comments are in English. */
public class NodeInfo {
    public enum Kind { OBJECT, ARRAY, VALUE }

    private final String key;
    private final JsonNode node;
    private final Kind kind;

    public NodeInfo(String key, JsonNode node) {
        this.key = key;
        this.node = node;
        if (node.isObject()) this.kind = Kind.OBJECT;
        else if (node.isArray()) this.kind = Kind.ARRAY;
        else this.kind = Kind.VALUE;
    }

    public String getKey() { return key; }
    public JsonNode getNode() { return node; }
    public Kind getKind() { return kind; }

    /** String label used in search and default text. */
    @Override
    public String toString() {
        if (kind == Kind.OBJECT) return key;
        if (kind == Kind.ARRAY)  return key;
        if (node.isTextual())    return key + " : " + node.asText();
        if (node.isNumber())     return key + " : " + node.numberValue();
        if (node.isBoolean())    return key + " : " + node.booleanValue();
        if (node.isNull())       return key + " : null";
        return key + " : " + node;
    }
}
