package com.ruiyun.jvppeteer.protocol.runtime;

/**
 * Description of an isolated world.
 */
public class ExecutionContextDescription {

    /**
     * Unique id of the execution context. It can be used to specify in which execution context
     script evaluation should be performed.
     */
    private int id;
    /**
     * Execution context origin.
     */
    private String origin;
    /**
     * Human readable name describing given context.
     */
    private String name;
    /**
     * Embedder-specific auxiliary data.
     */
    private AuxData auxData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuxData getAuxData() {
        return auxData;
    }

    public void setAuxData(AuxData auxData) {
        this.auxData = auxData;
    }
}
