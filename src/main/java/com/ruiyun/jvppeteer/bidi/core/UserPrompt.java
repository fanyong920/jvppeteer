package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptClosedParameters;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptHandlerType;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptOpenedParameters;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptResult;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DisposableStack;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPrompt extends EventEmitter<UserPrompt.UserPromptEvents> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPrompt.class);
    private volatile UserPromptResult result;
    private final UserPromptOpenedParameters info;
    private final BrowsingContext browsingContext;
    protected final List<DisposableStack<?>> disposables = new ArrayList<>();
    private String reason;

    private UserPrompt(BrowsingContext browsingContext, UserPromptOpenedParameters info) {
        super();
        this.browsingContext = browsingContext;
        this.info = info;
    }

    public static UserPrompt from(BrowsingContext browsingContext, UserPromptOpenedParameters info) {
        UserPrompt userPrompt = new UserPrompt(browsingContext, info);
        userPrompt.initialize();
        return userPrompt;
    }

    private void initialize() {
        Consumer<ClosedEvent> closeConsumer = event -> {
            this.dispose("User prompt already closed: " + event.getReason());
        };
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.closed, closeConsumer);
        this.disposables.add(new DisposableStack<>(this.browsingContext, BrowsingContext.BrowsingContextEvents.closed, closeConsumer));

        Consumer<UserPromptClosedParameters> userPromptClosedConsumer = parameters -> {
            if (!Objects.equals(parameters.getContext(), this.browsingContext.id())) {
                return;
            }
            this.result = Constant.OBJECTMAPPER.convertValue(parameters, UserPromptResult.class);
            this.emit(UserPromptEvents.handled, parameters);
            this.dispose("User prompt already handled.");
        };
        this.session().on(ConnectionEvents.browsingContext_userPromptClosed, userPromptClosedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_userPromptClosed, userPromptClosedConsumer));
    }

    public Session session() {
        return this.browsingContext.userContext.browser.session();
    }

    public boolean close() {
        return Objects.nonNull(this.reason);
    }

    public boolean disposed() {
        return this.close();
    }

    public boolean handled() {
        if (this.info.getHandler() == UserPromptHandlerType.Accept || this.info.getHandler() == UserPromptHandlerType.Dismiss) {
            return true;
        }
        return Objects.nonNull(this.result);
    }

    private void dispose(String reason) {
        this.reason = reason;
        this.disposeSymbol();
    }

    public UserPromptResult handle(boolean accept, String text) {
        ValidateUtil.assertArg(Objects.isNull(this.reason), "Attempted to use disposed UserPrompt");
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("accept", accept);
            params.put("userText", text);
            params.put("context", this.info.getContext());
            this.session().send("browsingContext.handleUserPrompt", params);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
            return null;
        }
        return result;
    }

    @Override
    public void disposeSymbol() {
        if (Objects.isNull(this.reason)) {
            this.reason = "User prompt already closed, probably because the associated browsing context was destroyed.";
            this.emit(UserPromptEvents.closed, this.reason);
            for (DisposableStack stack : this.disposables) {
                stack.getEmitter().off(stack.getType(), stack.getConsumer());
            }
            super.disposeSymbol();
        }
    }

    public UserPromptOpenedParameters info() {
        return this.info;
    }


    public enum UserPromptEvents {
        /**
         * UserPromptResult.class
         */
        handled,
        /**
         * string
         */
        closed
    }
}
