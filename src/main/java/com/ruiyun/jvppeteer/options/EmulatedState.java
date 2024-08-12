package com.ruiyun.jvppeteer.options;

public class EmulatedState<T extends ActiveProperty> {

    public T state;

    public  ClientProvider clientProvider;

    public Updater<T> updater;

    public EmulatedState(T initialState, ClientProvider clientProvider, Updater<T> updater){
        this.state = initialState;
        this.clientProvider = clientProvider;
        this.updater = updater;
        this.clientProvider.registerState(this);
    }

    public void setState(T state){
        this.state = state;
        this.send();
    }

    public T getState(){
        return this.state;
    }

    public void send() {
        this.clientProvider.clients().forEach(client -> this.updater.update(client,this.state));
    }
}
