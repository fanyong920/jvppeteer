package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import io.reactivex.rxjava3.subjects.SingleSubject;

import java.util.Optional;
import java.util.Timer;

public class Callback {
    private int id;
    private ProtocolException error =  new ProtocolException();
    SingleSubject<JsonNode> subject = SingleSubject.create();
    private Timer timer;
    public String label;

    public Callback(int id, String label) {
        this.id = id;
        this.label = label;
    }
    public void resolve(JsonNode value){
        Optional.ofNullable(this.timer).ifPresent(Timer::cancel);
        this.subject.onSuccess(value);
    }
    public void reject(Exception error){
        Optional.ofNullable(this.timer).ifPresent(Timer::cancel);
        this.subject.onError(error);
    }
    public int id(){
        return this.id;
    }
    public ProtocolException error(){
        return this.error;
    }
    public String label(){
        return this.label;
    }

    public SingleSubject<JsonNode> getSubject() {
        return this.subject;
    }

    public void setError(ProtocolException error) {
        this.error = error;
    }
}
