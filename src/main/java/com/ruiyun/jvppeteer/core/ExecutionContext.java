package com.ruiyun.jvppeteer.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.LazyArg;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.Binding;
import com.ruiyun.jvppeteer.entities.BindingPayload;
import com.ruiyun.jvppeteer.entities.EvaluateResponse;
import com.ruiyun.jvppeteer.entities.EvaluateType;
import com.ruiyun.jvppeteer.entities.ExceptionDetails;
import com.ruiyun.jvppeteer.entities.ExecutionContextDescription;
import com.ruiyun.jvppeteer.entities.RemoteObject;
import com.ruiyun.jvppeteer.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.ExecutionContextDestroyedEvent;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.ruiyun.jvppeteer.common.Constant.CDP_BINDING_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.INTERNAL_URL;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.SOURCE_URL_REGEX;
import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class ExecutionContext extends EventEmitter<ExecutionContext.ExecutionContextEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);
    private final CDPSession client;
    private String name;
    private final int id;
    private final IsolatedWorld world;
    private final Map<CDPSession.CDPSessionEvent, Consumer<?>> listener = new HashMap<>();
    private final Map<String, Binding> bindings = new HashMap<>();
    private static final AtomicLong pptrFunctionId = new AtomicLong(0);
    private volatile JSHandle puppeteerUtil;
    String source = "\"use strict\";var g=Object.defineProperty;var X=Object.getOwnPropertyDescriptor;var B=Object.getOwnPropertyNames;var Y=Object.prototype.hasOwnProperty;var l=(t,e)=>{for(var r in e)g(t,r,{get:e[r],enumerable:!0})},J=(t,e,r,o)=>{if(e&&typeof e==\"object\"||typeof e==\"function\")for(let n of B(e))!Y.call(t,n)&&n!==r&&g(t,n,{get:()=>e[n],enumerable:!(o=X(e,n))||o.enumerable});return t};var z=t=>J(g({},\"__esModule\",{value:!0}),t);var pe={};l(pe,{default:()=>he});module.exports=z(pe);var N=class extends Error{constructor(e,r){super(e,r),this.name=this.constructor.name}get[Symbol.toStringTag](){return this.constructor.name}},p=class extends N{};var c=class t{static create(e){return new t(e)}static async race(e){let r=new Set;try{let o=e.map(n=>n instanceof t?(n.#n&&r.add(n),n.valueOrThrow()):n);return await Promise.race(o)}finally{for(let o of r)o.reject(new Error(\"Timeout cleared\"))}}#e=!1;#r=!1;#o;#t;#a=new Promise(e=>{this.#t=e});#n;#i;constructor(e){e&&e.timeout>0&&(this.#i=new p(e.message),this.#n=setTimeout(()=>{this.reject(this.#i)},e.timeout))}#l(e){clearTimeout(this.#n),this.#o=e,this.#t()}resolve(e){this.#r||this.#e||(this.#e=!0,this.#l(e))}reject(e){this.#r||this.#e||(this.#r=!0,this.#l(e))}resolved(){return this.#e}finished(){return this.#e||this.#r}value(){return this.#o}#s;valueOrThrow(){return this.#s||(this.#s=(async()=>{if(await this.#a,this.#r)throw this.#o;return this.#o})()),this.#s}};var L=new Map,F=t=>{let e=L.get(t);return e||(e=new Function(`return ${t}`)(),L.set(t,e),e)};var x={};l(x,{ariaQuerySelector:()=>G,ariaQuerySelectorAll:()=>b});var G=(t,e)=>globalThis.__ariaQuerySelector(t,e),b=async function*(t,e){yield*await globalThis.__ariaQuerySelectorAll(t,e)};var E={};l(E,{cssQuerySelector:()=>K,cssQuerySelectorAll:()=>Z});var K=(t,e)=>t.querySelector(e),Z=function(t,e){return t.querySelectorAll(e)};var A={};l(A,{customQuerySelectors:()=>P});var v=class{#e=new Map;register(e,r){if(!r.queryOne&&r.queryAll){let o=r.queryAll;r.queryOne=(n,i)=>{for(let s of o(n,i))return s;return null}}else if(r.queryOne&&!r.queryAll){let o=r.queryOne;r.queryAll=(n,i)=>{let s=o(n,i);return s?[s]:[]}}else if(!r.queryOne||!r.queryAll)throw new Error(\"At least one query method must be defined.\");this.#e.set(e,{querySelector:r.queryOne,querySelectorAll:r.queryAll})}unregister(e){this.#e.delete(e)}get(e){return this.#e.get(e)}clear(){this.#e.clear()}},P=new v;var R={};l(R,{pierceQuerySelector:()=>ee,pierceQuerySelectorAll:()=>te});var ee=(t,e)=>{let r=null,o=n=>{let i=document.createTreeWalker(n,NodeFilter.SHOW_ELEMENT);do{let s=i.currentNode;s.shadowRoot&&o(s.shadowRoot),!(s instanceof ShadowRoot)&&s!==n&&!r&&s.matches(e)&&(r=s)}while(!r&&i.nextNode())};return t instanceof Document&&(t=t.documentElement),o(t),r},te=(t,e)=>{let r=[],o=n=>{let i=document.createTreeWalker(n,NodeFilter.SHOW_ELEMENT);do{let s=i.currentNode;s.shadowRoot&&o(s.shadowRoot),!(s instanceof ShadowRoot)&&s!==n&&s.matches(e)&&r.push(s)}while(i.nextNode())};return t instanceof Document&&(t=t.documentElement),o(t),r};var u=(t,e)=>{if(!t)throw new Error(e)};var y=class{#e;#r;#o;#t;constructor(e,r){this.#e=e,this.#r=r}async start(){let e=this.#t=c.create(),r=await this.#e();if(r){e.resolve(r);return}this.#o=new MutationObserver(async()=>{let o=await this.#e();o&&(e.resolve(o),await this.stop())}),this.#o.observe(this.#r,{childList:!0,subtree:!0,attributes:!0})}async stop(){u(this.#t,\"Polling never started.\"),this.#t.finished()||this.#t.reject(new Error(\"Polling stopped\")),this.#o&&(this.#o.disconnect(),this.#o=void 0)}result(){return u(this.#t,\"Polling never started.\"),this.#t.valueOrThrow()}},w=class{#e;#r;constructor(e){this.#e=e}async start(){let e=this.#r=c.create(),r=await this.#e();if(r){e.resolve(r);return}let o=async()=>{if(e.finished())return;let n=await this.#e();if(!n){window.requestAnimationFrame(o);return}e.resolve(n),await this.stop()};window.requestAnimationFrame(o)}async stop(){u(this.#r,\"Polling never started.\"),this.#r.finished()||this.#r.reject(new Error(\"Polling stopped\"))}result(){return u(this.#r,\"Polling never started.\"),this.#r.valueOrThrow()}},T=class{#e;#r;#o;#t;constructor(e,r){this.#e=e,this.#r=r}async start(){let e=this.#t=c.create(),r=await this.#e();if(r){e.resolve(r);return}this.#o=setInterval(async()=>{let o=await this.#e();o&&(e.resolve(o),await this.stop())},this.#r)}async stop(){u(this.#t,\"Polling never started.\"),this.#t.finished()||this.#t.reject(new Error(\"Polling stopped\")),this.#o&&(clearInterval(this.#o),this.#o=void 0)}result(){return u(this.#t,\"Polling never started.\"),this.#t.valueOrThrow()}};var _={};l(_,{PCombinator:()=>H,pQuerySelector:()=>fe,pQuerySelectorAll:()=>$});var a=class{static async*map(e,r){for await(let o of e)yield await r(o)}static async*flatMap(e,r){for await(let o of e)yield*r(o)}static async collect(e){let r=[];for await(let o of e)r.push(o);return r}static async first(e){for await(let r of e)return r}};var C={};l(C,{textQuerySelectorAll:()=>m});var re=new Set([\"checkbox\",\"image\",\"radio\"]),oe=t=>t instanceof HTMLSelectElement||t instanceof HTMLTextAreaElement||t instanceof HTMLInputElement&&!re.has(t.type),ne=new Set([\"SCRIPT\",\"STYLE\"]),f=t=>!ne.has(t.nodeName)&&!document.head?.contains(t),I=new WeakMap,j=t=>{for(;t;)I.delete(t),t instanceof ShadowRoot?t=t.host:t=t.parentNode},W=new WeakSet,se=new MutationObserver(t=>{for(let e of t)j(e.target)}),d=t=>{let e=I.get(t);if(e||(e={full:\"\",immediate:[]},!f(t)))return e;let r=\"\";if(oe(t))e.full=t.value,e.immediate.push(t.value),t.addEventListener(\"input\",o=>{j(o.target)},{once:!0,capture:!0});else{for(let o=t.firstChild;o;o=o.nextSibling){if(o.nodeType===Node.TEXT_NODE){e.full+=o.nodeValue??\"\",r+=o.nodeValue??\"\";continue}r&&e.immediate.push(r),r=\"\",o.nodeType===Node.ELEMENT_NODE&&(e.full+=d(o).full)}r&&e.immediate.push(r),t instanceof Element&&t.shadowRoot&&(e.full+=d(t.shadowRoot).full),W.has(t)||(se.observe(t,{childList:!0,characterData:!0,subtree:!0}),W.add(t))}return I.set(t,e),e};var m=function*(t,e){let r=!1;for(let o of t.childNodes)if(o instanceof Element&&f(o)){let n;o.shadowRoot?n=m(o.shadowRoot,e):n=m(o,e);for(let i of n)yield i,r=!0}r||t instanceof Element&&f(t)&&d(t).full.includes(e)&&(yield t)};var k={};l(k,{checkVisibility:()=>le,pierce:()=>S,pierceAll:()=>O});var ie=[\"hidden\",\"collapse\"],le=(t,e)=>{if(!t)return e===!1;if(e===void 0)return t;let r=t.nodeType===Node.TEXT_NODE?t.parentElement:t,o=window.getComputedStyle(r),n=o&&!ie.includes(o.visibility)&&!ae(r);return e===n?t:!1};function ae(t){let e=t.getBoundingClientRect();return e.width===0||e.height===0}var ce=t=>\"shadowRoot\"in t&&t.shadowRoot instanceof ShadowRoot;function*S(t){ce(t)?yield t.shadowRoot:yield t}function*O(t){t=S(t).next().value,yield t;let e=[document.createTreeWalker(t,NodeFilter.SHOW_ELEMENT)];for(let r of e){let o;for(;o=r.nextNode();)o.shadowRoot&&(yield o.shadowRoot,e.push(document.createTreeWalker(o.shadowRoot,NodeFilter.SHOW_ELEMENT)))}}var Q={};l(Q,{xpathQuerySelectorAll:()=>q});var q=function*(t,e,r=-1){let n=(t.ownerDocument||document).evaluate(e,t,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE),i=[],s;for(;(s=n.iterateNext())&&(i.push(s),!(r&&i.length===r)););for(let h=0;h<i.length;h++)s=i[h],yield s,delete i[h]};var ue=/[-\\w\\P{ASCII}*]/,H=(r=>(r.Descendent=\">>>\",r.Child=\">>>>\",r))(H||{}),V=t=>\"querySelectorAll\"in t,M=class{#e;#r=[];#o=void 0;elements;constructor(e,r){this.elements=[e],this.#e=r,this.#t()}async run(){if(typeof this.#o==\"string\")switch(this.#o.trimStart()){case\":scope\":this.#t();break}for(;this.#o!==void 0;this.#t()){let e=this.#o;typeof e==\"string\"?e[0]&&ue.test(e[0])?this.elements=a.flatMap(this.elements,async function*(r){V(r)&&(yield*r.querySelectorAll(e))}):this.elements=a.flatMap(this.elements,async function*(r){if(!r.parentElement){if(!V(r))return;yield*r.querySelectorAll(e);return}let o=0;for(let n of r.parentElement.children)if(++o,n===r)break;yield*r.parentElement.querySelectorAll(`:scope>:nth-child(${o})${e}`)}):this.elements=a.flatMap(this.elements,async function*(r){switch(e.name){case\"text\":yield*m(r,e.value);break;case\"xpath\":yield*q(r,e.value);break;case\"aria\":yield*b(r,e.value);break;default:let o=P.get(e.name);if(!o)throw new Error(`Unknown selector type: ${e.name}`);yield*o.querySelectorAll(r,e.value)}})}}#t(){if(this.#r.length!==0){this.#o=this.#r.shift();return}if(this.#e.length===0){this.#o=void 0;return}let e=this.#e.shift();switch(e){case\">>>>\":{this.elements=a.flatMap(this.elements,S),this.#t();break}case\">>>\":{this.elements=a.flatMap(this.elements,O),this.#t();break}default:this.#r=e,this.#t();break}}},D=class{#e=new WeakMap;calculate(e,r=[]){if(e===null)return r;e instanceof ShadowRoot&&(e=e.host);let o=this.#e.get(e);if(o)return[...o,...r];let n=0;for(let s=e.previousSibling;s;s=s.previousSibling)++n;let i=this.calculate(e.parentNode,[n]);return this.#e.set(e,i),[...i,...r]}},U=(t,e)=>{if(t.length+e.length===0)return 0;let[r=-1,...o]=t,[n=-1,...i]=e;return r===n?U(o,i):r<n?-1:1},de=async function*(t){let e=new Set;for await(let o of t)e.add(o);let r=new D;yield*[...e.values()].map(o=>[o,r.calculate(o)]).sort(([,o],[,n])=>U(o,n)).map(([o])=>o)},$=function(t,e){let r=JSON.parse(e);if(r.some(o=>{let n=0;return o.some(i=>(typeof i==\"string\"?++n:n=0,n>1))}))throw new Error(\"Multiple deep combinators found in sequence.\");return de(a.flatMap(r,o=>{let n=new M(t,o);return n.run(),n.elements}))},fe=async function(t,e){for await(let r of $(t,e))return r;return null};var me=Object.freeze({...x,...A,...R,..._,...C,...k,...Q,...E,Deferred:c,createFunction:F,createTextContent:d,IntervalPoller:T,isSuitableNodeForTextMatching:f,MutationPoller:y,RAFPoller:w}),he=me;\n";

    public ExecutionContext(CDPSession client, ExecutionContextDescription contextPayload, IsolatedWorld world) {
        this.client = client;
        this.world = world;
        this.id = contextPayload.getId();
        if (StringUtil.isNotEmpty(contextPayload.getName())) {
            this.name = contextPayload.getName();
        }
        setListener(client);
    }

    private void setListener(CDPSession client) {
        Consumer<BindingCalledEvent> bindingCalled = this::onBindingCalled;
        client.on(CDPSession.CDPSessionEvent.Runtime_bindingCalled, bindingCalled);
        this.listener.put(CDPSession.CDPSessionEvent.Runtime_bindingCalled, bindingCalled);

        Consumer<ExecutionContextDestroyedEvent> executionContextDestroyed = event -> {
            if (event.getExecutionContextId() == this.id) {
                this.dispose();
            }
        };
        client.on(CDPSession.CDPSessionEvent.Runtime_executionContextDestroyed, executionContextDestroyed);
        this.listener.put(CDPSession.CDPSessionEvent.Runtime_executionContextDestroyed, executionContextDestroyed);

        Consumer<Boolean> executionContextsCleared = event -> this.dispose();
        client.on(CDPSession.CDPSessionEvent.Runtime_executionContextsCleared, executionContextsCleared);
        this.listener.put(CDPSession.CDPSessionEvent.Runtime_executionContextsCleared, executionContextsCleared);

        Consumer<ConsoleAPICalledEvent> consoleAPICalled = ExecutionContext.this::onConsoleAPI;
        client.on(CDPSession.CDPSessionEvent.Runtime_consoleAPICalled, consoleAPICalled);
        this.listener.put(CDPSession.CDPSessionEvent.Runtime_consoleAPICalled, consoleAPICalled);

        Consumer<Boolean> disconnected = event -> this.dispose();
        client.on(CDPSession.CDPSessionEvent.CDPSession_Disconnected, disconnected);
        this.listener.put(CDPSession.CDPSessionEvent.CDPSession_Disconnected, disconnected);
    }

    public void addBinding(Binding binding) {
        if (this.bindings.containsKey(binding.name())) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("name", CDP_BINDING_PREFIX + binding.name());
        if (StringUtil.isNotEmpty(this.name)) {
            params.put("executionContextName", this.name);
        } else {
            params.put("executionContextId", this.id);
        }
        synchronized (this) {
            try {
                this.client.send("Runtime.addBinding", params);
                List<Object> args = new ArrayList<>();
                args.add("internal");
                args.add(binding.name());
                args.add(CDP_BINDING_PREFIX);
                this.evaluate(addPageBinding(), args);
                this.bindings.put(binding.name(), binding);
            } catch (Exception e) {
                if (e.getMessage().contains("Execution context was destroyed")) {
                    return;
                }
                if (e.getMessage().contains("Cannot find context with specified id'")) {
                    return;
                }
                LOGGER.error("addBinding error:", e);
            }
        }
    }

    private String addPageBinding() {
        return "function addPageBinding(type,name,prefix) {\n" +
                "  // Depending on the frame loading state either Runtime.evaluate or\n" +
                "  // Page.addScriptToEvaluateOnNewDocument might succeed. Let's check that we\n" +
                "  // don't re-wrap Puppeteer's binding.\n" +
                "  // @ts-expect-error: In a different context.\n" +
                "  if (globalThis[name]) {\n" +
                "    return;\n" +
                "  }\n" +
                "\n" +
                "  // We replace the CDP binding with a Puppeteer binding.\n" +
                "  Object.assign(globalThis, {\n" +
                "    [name](...args) {\n" +
                "      // This is the Puppeteer binding.\n" +
                "      // @ts-expect-error: In a different context.\n" +
                "      const callPuppeteer = globalThis[name];\n" +
                "      callPuppeteer.args ??= new Map();\n" +
                "      callPuppeteer.callbacks ??= new Map();\n" +
                "\n" +
                "      const seq = (callPuppeteer.lastSeq ?? 0) + 1;\n" +
                "      callPuppeteer.lastSeq = seq;\n" +
                "      callPuppeteer.args.set(seq, args);\n" +
                "\n" +
                "      // @ts-expect-error: In a different context.\n" +
                "      // Needs to be the same as CDP_BINDING_PREFIX.\n" +
                "      globalThis[prefix + name](\n" +
                "        JSON.stringify({\n" +
                "          type,\n" +
                "          name,\n" +
                "          seq,\n" +
                "          args,\n" +
                "          isTrivial: !args.some(value => {\n" +
                "            return value instanceof Node;\n" +
                "          }),\n" +
                "        })\n" +
                "      );\n" +
                "\n" +
                "      return new Promise((resolve, reject) => {\n" +
                "        callPuppeteer.callbacks.set(seq, {\n" +
                "          resolve(value) {\n" +
                "            callPuppeteer.args.delete(seq);\n" +
                "            resolve(value);\n" +
                "          },\n" +
                "          reject(value) {\n" +
                "            callPuppeteer.args.delete(seq);\n" +
                "            reject(value);\n" +
                "          },\n" +
                "        });\n" +
                "      });\n" +
                "    },\n" +
                "  });\n" +
                "}";
    }

    private void onBindingCalled(BindingCalledEvent event) {
        if (event.getExecutionContextId() != this.id) {
            return;
        }
        String payloadStr = event.getPayload();
        BindingPayload payload;
        try {
            payload = OBJECTMAPPER.readValue(payloadStr, BindingPayload.class);
        } catch (JsonProcessingException e) {
            return;
        }
        if (!"internal".equals(payload.getType())) {
            this.emit(ExecutionContextEvent.Bindingcalled, event);
            return;
        }
        if (!this.bindings.containsKey(payload.getName())) {
            this.emit(ExecutionContextEvent.Bindingcalled, event);
            return;
        }
        Binding binding = this.bindings.get(payload.getName());
        try {
            if (binding != null) {
                binding.run(this, payload.getSeq(), payload.getArgs(), payload.getIsTrivial());
            }
        } catch (Exception e) {
            LOGGER.error("onBindingCalled error", e);
        }
    }

    public int getId() {
        return id;
    }

    private void onConsoleAPI(ConsoleAPICalledEvent event) {
        if (event.getExecutionContextId() != this.id) {
            return;
        }
        this.emit(ExecutionContextEvent.Consoleapicalled, event);
    }

    public JSHandle evaluateHandle(String pageFunction, EvaluateType type, List<Object> args) throws JsonProcessingException, EvaluateException {
        Object handle = this.evaluateInternal(false, pageFunction, type, args);
        if (handle == null) {
            return null;
        }
        return (JSHandle) handle;
    }

    public JSHandle evaluateHandle(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        Object handle = this.evaluateInternal(false, pageFunction, Helper.isFunction(pageFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING, args);
        if (handle == null) {
            return null;
        }
        return (JSHandle) handle;
    }

    public Object evaluate(String pageFunction, EvaluateType type, List<Object> args) throws JsonProcessingException, EvaluateException {
        if (type == null) {
            type = Helper.isFunction(pageFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING;
        }
        return this.evaluateInternal(true, pageFunction, type, args);
    }

    public Object evaluate(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        return this.evaluate(pageFunction, null, args);
    }

    private String setSourceUrlComment(String pageFunction) {
        if (SOURCE_URL_REGEX.matcher(pageFunction).find()) {
            return pageFunction;
        } else {
            return pageFunction + "\n" + "//# sourceURL=" + INTERNAL_URL + pptrFunctionId.incrementAndGet() + "\n";
        }
    }

    private EvaluateResponse rewriteError(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("Object reference chain is too long")) {
            RemoteObject remoteObject = new RemoteObject();
            remoteObject.setType("undefined");
            EvaluateResponse response = new EvaluateResponse();
            response.setResult(remoteObject);
            return response;
        }
        if (e.getMessage() != null && e.getMessage().contains("Object couldn't be returned by value")) {
            RemoteObject remoteObject = new RemoteObject();
            remoteObject.setType("undefined");
            EvaluateResponse response = new EvaluateResponse();
            response.setResult(remoteObject);
            return response;
        }

        if (e.getMessage() != null && (e.getMessage().endsWith("Cannot find context with specified id") || e.getMessage().endsWith("Inspected target navigated or closed"))) {
            throw new JvppeteerException("Execution context was destroyed, most likely because of a navigation.");
        }
        throwError(e);
        return null;
    }

    /**
     * 这里的EvaluateType有时候是明确指定为String的，不指定的情况下，会自动判断是字符串还是函数
     * <p>
     * {@link Frame#addExposedFunctionBinding(Binding)}就指定了是String
     */
    private Object evaluateInternal(boolean returnByValue, String pageFunction, EvaluateType type, List<?> args) throws JsonProcessingException, EvaluateException {
        pageFunction = setSourceUrlComment(pageFunction);
        if (EvaluateType.STRING.equals(type)) {
            Map<String, Object> params = new HashMap<>();
            params.put("expression", pageFunction);
            params.put("contextId", this.id);
            params.put("returnByValue", returnByValue);
            params.put("awaitPromise", true);
            params.put("userGesture", true);
            EvaluateResponse result;
            try {
                result = OBJECTMAPPER.treeToValue(this.client.send("Runtime.evaluate", params), EvaluateResponse.class);
            } catch (Exception e) {
                result = rewriteError(e);
            }
            ExceptionDetails exceptionDetails = result.getExceptionDetails();
            if (exceptionDetails != null) {
                Object evaluationError = Helper.createEvaluationError(exceptionDetails);
                if (evaluationError instanceof EvaluateException) {
                    throw (EvaluateException) evaluationError;
                } else {
                    throw new EvaluateException(OBJECTMAPPER.writeValueAsString(evaluationError));
                }
            }
            RemoteObject remoteObject = result.getResult();
            return returnByValue ? Helper.valueFromRemoteObject(remoteObject) : this.world.createJSHandle(remoteObject);
        }
        Map<String, Object> params = new HashMap<>();
        List<JsonNode> argList = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof LazyArg) {
                    initPuppeteerUtil();
                } else {
                    argList.add(convertArgument(this, arg));
                }
            }
        }
        params.put("functionDeclaration", pageFunction);
        params.put("executionContextId", this.id);
        params.put("arguments", argList);
        params.put("returnByValue", returnByValue);
        params.put("awaitPromise", true);
        params.put("userGesture", true);
        EvaluateResponse callFunctionOnPromise;
        try {
            try {//第一个try用来添加message,第二个try是重写错误，返回结果
                callFunctionOnPromise = OBJECTMAPPER.treeToValue(this.client.send("Runtime.callFunctionOn", params), EvaluateResponse.class);
            } catch (Exception e) {
                if (e.getMessage().startsWith("Converting circular structure to JSON"))
                    throw new JvppeteerException(e.getMessage() + " Recursive objects are not allowed.");
                else
                    throw e;
            }
        } catch (Exception e) {
            callFunctionOnPromise = rewriteError(e);
        }
        if (callFunctionOnPromise == null) {
            return null;
        }
        ExceptionDetails exceptionDetails = callFunctionOnPromise.getExceptionDetails();
        if (exceptionDetails != null) {
            Object evaluationError = Helper.createEvaluationError(exceptionDetails);
            if (evaluationError instanceof EvaluateException) {
                throw (EvaluateException) evaluationError;
            } else {
                throw new EvaluateException(OBJECTMAPPER.writeValueAsString(evaluationError));
            }
        }
        RemoteObject remoteObject = callFunctionOnPromise.getResult();
        return returnByValue ? Helper.valueFromRemoteObject(remoteObject) : this.world.createJSHandle(remoteObject);
    }

    private void initPuppeteerUtil() throws JsonProcessingException {
        if (puppeteerUtil == null) {
            synchronized (this) {
                if (puppeteerUtil == null) {
//                    BindingFunction queryOneFunction = (args) -> {
//                        ElementHandle element = (ElementHandle) args.get(0);
//                        String selector = (String) args.get(2);
//                        ARIAQueryHandler ariaQueryHandler = new ARIAQueryHandler(element, selector);
//                        try {
//                            return ariaQueryHandler.queryOne();
//                        } catch (JsonProcessingException e) {
//                            return null;
//                        }
//                    };
//                    Binding ariaQuerySelectorBinding = new Binding("__ariaQuerySelector", queryOneFunction, "");
//                    BindingFunction queryAllFunction = (args) -> {
//                        ElementHandle element = (ElementHandle) args.get(0);
//                        String selector = (String) args.get(2);
//                        ARIAQueryHandler ariaQueryHandler = new ARIAQueryHandler(element, selector);
//                        try {
//                            return ariaQueryHandler.queryAll();
//                        } catch (JsonProcessingException e) {
//                            return null;
//                        }
//                    };
//                    Binding ariaQuerySelectorAllBinding = new Binding("__ariaQuerySelector", queryAllFunction, "");
//                    this.addBinding(ariaQuerySelectorBinding);
//                    this.addBinding(ariaQuerySelectorAllBinding);
                    String pageFunction = "(() => {\n      const module = {};\n      \"use strict\";var g=Object.defineProperty;var X=Object.getOwnPropertyDescriptor;var B=Object.getOwnPropertyNames;var Y=Object.prototype.hasOwnProperty;var l=(t,e)=>{for(var r in e)g(t,r,{get:e[r],enumerable:!0})},J=(t,e,r,o)=>{if(e&&typeof e==\"object\"||typeof e==\"function\")for(let n of B(e))!Y.call(t,n)&&n!==r&&g(t,n,{get:()=>e[n],enumerable:!(o=X(e,n))||o.enumerable});return t};var z=t=>J(g({},\"__esModule\",{value:!0}),t);var pe={};l(pe,{default:()=>he});module.exports=z(pe);var N=class extends Error{constructor(e,r){super(e,r),this.name=this.constructor.name}get[Symbol.toStringTag](){return this.constructor.name}},p=class extends N{};var c=class t{static create(e){return new t(e)}static async race(e){let r=new Set;try{let o=e.map(n=>n instanceof t?(n.#n&&r.add(n),n.valueOrThrow()):n);return await Promise.race(o)}finally{for(let o of r)o.reject(new Error(\"Timeout cleared\"))}}#e=!1;#r=!1;#o;#t;#a=new Promise(e=>{this.#t=e});#n;#i;constructor(e){e&&e.timeout>0&&(this.#i=new p(e.message),this.#n=setTimeout(()=>{this.reject(this.#i)},e.timeout))}#l(e){clearTimeout(this.#n),this.#o=e,this.#t()}resolve(e){this.#r||this.#e||(this.#e=!0,this.#l(e))}reject(e){this.#r||this.#e||(this.#r=!0,this.#l(e))}resolved(){return this.#e}finished(){return this.#e||this.#r}value(){return this.#o}#s;valueOrThrow(){return this.#s||(this.#s=(async()=>{if(await this.#a,this.#r)throw this.#o;return this.#o})()),this.#s}};var L=new Map,F=t=>{let e=L.get(t);return e||(e=new Function(`return ${t}`)(),L.set(t,e),e)};var x={};l(x,{ariaQuerySelector:()=>G,ariaQuerySelectorAll:()=>b});var G=(t,e)=>globalThis.__ariaQuerySelector(t,e),b=async function*(t,e){yield*await globalThis.__ariaQuerySelectorAll(t,e)};var E={};l(E,{cssQuerySelector:()=>K,cssQuerySelectorAll:()=>Z});var K=(t,e)=>t.querySelector(e),Z=function(t,e){return t.querySelectorAll(e)};var A={};l(A,{customQuerySelectors:()=>P});var v=class{#e=new Map;register(e,r){if(!r.queryOne&&r.queryAll){let o=r.queryAll;r.queryOne=(n,i)=>{for(let s of o(n,i))return s;return null}}else if(r.queryOne&&!r.queryAll){let o=r.queryOne;r.queryAll=(n,i)=>{let s=o(n,i);return s?[s]:[]}}else if(!r.queryOne||!r.queryAll)throw new Error(\"At least one query method must be defined.\");this.#e.set(e,{querySelector:r.queryOne,querySelectorAll:r.queryAll})}unregister(e){this.#e.delete(e)}get(e){return this.#e.get(e)}clear(){this.#e.clear()}},P=new v;var R={};l(R,{pierceQuerySelector:()=>ee,pierceQuerySelectorAll:()=>te});var ee=(t,e)=>{let r=null,o=n=>{let i=document.createTreeWalker(n,NodeFilter.SHOW_ELEMENT);do{let s=i.currentNode;s.shadowRoot&&o(s.shadowRoot),!(s instanceof ShadowRoot)&&s!==n&&!r&&s.matches(e)&&(r=s)}while(!r&&i.nextNode())};return t instanceof Document&&(t=t.documentElement),o(t),r},te=(t,e)=>{let r=[],o=n=>{let i=document.createTreeWalker(n,NodeFilter.SHOW_ELEMENT);do{let s=i.currentNode;s.shadowRoot&&o(s.shadowRoot),!(s instanceof ShadowRoot)&&s!==n&&s.matches(e)&&r.push(s)}while(i.nextNode())};return t instanceof Document&&(t=t.documentElement),o(t),r};var u=(t,e)=>{if(!t)throw new Error(e)};var y=class{#e;#r;#o;#t;constructor(e,r){this.#e=e,this.#r=r}async start(){let e=this.#t=c.create(),r=await this.#e();if(r){e.resolve(r);return}this.#o=new MutationObserver(async()=>{let o=await this.#e();o&&(e.resolve(o),await this.stop())}),this.#o.observe(this.#r,{childList:!0,subtree:!0,attributes:!0})}async stop(){u(this.#t,\"Polling never started.\"),this.#t.finished()||this.#t.reject(new Error(\"Polling stopped\")),this.#o&&(this.#o.disconnect(),this.#o=void 0)}result(){return u(this.#t,\"Polling never started.\"),this.#t.valueOrThrow()}},w=class{#e;#r;constructor(e){this.#e=e}async start(){let e=this.#r=c.create(),r=await this.#e();if(r){e.resolve(r);return}let o=async()=>{if(e.finished())return;let n=await this.#e();if(!n){window.requestAnimationFrame(o);return}e.resolve(n),await this.stop()};window.requestAnimationFrame(o)}async stop(){u(this.#r,\"Polling never started.\"),this.#r.finished()||this.#r.reject(new Error(\"Polling stopped\"))}result(){return u(this.#r,\"Polling never started.\"),this.#r.valueOrThrow()}},T=class{#e;#r;#o;#t;constructor(e,r){this.#e=e,this.#r=r}async start(){let e=this.#t=c.create(),r=await this.#e();if(r){e.resolve(r);return}this.#o=setInterval(async()=>{let o=await this.#e();o&&(e.resolve(o),await this.stop())},this.#r)}async stop(){u(this.#t,\"Polling never started.\"),this.#t.finished()||this.#t.reject(new Error(\"Polling stopped\")),this.#o&&(clearInterval(this.#o),this.#o=void 0)}result(){return u(this.#t,\"Polling never started.\"),this.#t.valueOrThrow()}};var _={};l(_,{PCombinator:()=>H,pQuerySelector:()=>fe,pQuerySelectorAll:()=>$});var a=class{static async*map(e,r){for await(let o of e)yield await r(o)}static async*flatMap(e,r){for await(let o of e)yield*r(o)}static async collect(e){let r=[];for await(let o of e)r.push(o);return r}static async first(e){for await(let r of e)return r}};var C={};l(C,{textQuerySelectorAll:()=>m});var re=new Set([\"checkbox\",\"image\",\"radio\"]),oe=t=>t instanceof HTMLSelectElement||t instanceof HTMLTextAreaElement||t instanceof HTMLInputElement&&!re.has(t.type),ne=new Set([\"SCRIPT\",\"STYLE\"]),f=t=>!ne.has(t.nodeName)&&!document.head?.contains(t),I=new WeakMap,j=t=>{for(;t;)I.delete(t),t instanceof ShadowRoot?t=t.host:t=t.parentNode},W=new WeakSet,se=new MutationObserver(t=>{for(let e of t)j(e.target)}),d=t=>{let e=I.get(t);if(e||(e={full:\"\",immediate:[]},!f(t)))return e;let r=\"\";if(oe(t))e.full=t.value,e.immediate.push(t.value),t.addEventListener(\"input\",o=>{j(o.target)},{once:!0,capture:!0});else{for(let o=t.firstChild;o;o=o.nextSibling){if(o.nodeType===Node.TEXT_NODE){e.full+=o.nodeValue??\"\",r+=o.nodeValue??\"\";continue}r&&e.immediate.push(r),r=\"\",o.nodeType===Node.ELEMENT_NODE&&(e.full+=d(o).full)}r&&e.immediate.push(r),t instanceof Element&&t.shadowRoot&&(e.full+=d(t.shadowRoot).full),W.has(t)||(se.observe(t,{childList:!0,characterData:!0,subtree:!0}),W.add(t))}return I.set(t,e),e};var m=function*(t,e){let r=!1;for(let o of t.childNodes)if(o instanceof Element&&f(o)){let n;o.shadowRoot?n=m(o.shadowRoot,e):n=m(o,e);for(let i of n)yield i,r=!0}r||t instanceof Element&&f(t)&&d(t).full.includes(e)&&(yield t)};var k={};l(k,{checkVisibility:()=>le,pierce:()=>S,pierceAll:()=>O});var ie=[\"hidden\",\"collapse\"],le=(t,e)=>{if(!t)return e===!1;if(e===void 0)return t;let r=t.nodeType===Node.TEXT_NODE?t.parentElement:t,o=window.getComputedStyle(r),n=o&&!ie.includes(o.visibility)&&!ae(r);return e===n?t:!1};function ae(t){let e=t.getBoundingClientRect();return e.width===0||e.height===0}var ce=t=>\"shadowRoot\"in t&&t.shadowRoot instanceof ShadowRoot;function*S(t){ce(t)?yield t.shadowRoot:yield t}function*O(t){t=S(t).next().value,yield t;let e=[document.createTreeWalker(t,NodeFilter.SHOW_ELEMENT)];for(let r of e){let o;for(;o=r.nextNode();)o.shadowRoot&&(yield o.shadowRoot,e.push(document.createTreeWalker(o.shadowRoot,NodeFilter.SHOW_ELEMENT)))}}var Q={};l(Q,{xpathQuerySelectorAll:()=>q});var q=function*(t,e,r=-1){let n=(t.ownerDocument||document).evaluate(e,t,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE),i=[],s;for(;(s=n.iterateNext())&&(i.push(s),!(r&&i.length===r)););for(let h=0;h<i.length;h++)s=i[h],yield s,delete i[h]};var ue=/[-\\\\w\\\\P{ASCII}*]/,H=(r=>(r.Descendent=\">>>\",r.Child=\">>>>\",r))(H||{}),V=t=>\"querySelectorAll\"in t,M=class{#e;#r=[];#o=void 0;elements;constructor(e,r){this.elements=[e],this.#e=r,this.#t()}async run(){if(typeof this.#o==\"string\")switch(this.#o.trimStart()){case\":scope\":this.#t();break}for(;this.#o!==void 0;this.#t()){let e=this.#o;typeof e==\"string\"?e[0]&&ue.test(e[0])?this.elements=a.flatMap(this.elements,async function*(r){V(r)&&(yield*r.querySelectorAll(e))}):this.elements=a.flatMap(this.elements,async function*(r){if(!r.parentElement){if(!V(r))return;yield*r.querySelectorAll(e);return}let o=0;for(let n of r.parentElement.children)if(++o,n===r)break;yield*r.parentElement.querySelectorAll(`:scope>:nth-child(${o})${e}`)}):this.elements=a.flatMap(this.elements,async function*(r){switch(e.name){case\"text\":yield*m(r,e.value);break;case\"xpath\":yield*q(r,e.value);break;case\"aria\":yield*b(r,e.value);break;default:let o=P.get(e.name);if(!o)throw new Error(`Unknown selector type: ${e.name}`);yield*o.querySelectorAll(r,e.value)}})}}#t(){if(this.#r.length!==0){this.#o=this.#r.shift();return}if(this.#e.length===0){this.#o=void 0;return}let e=this.#e.shift();switch(e){case\">>>>\":{this.elements=a.flatMap(this.elements,S),this.#t();break}case\">>>\":{this.elements=a.flatMap(this.elements,O),this.#t();break}default:this.#r=e,this.#t();break}}},D=class{#e=new WeakMap;calculate(e,r=[]){if(e===null)return r;e instanceof ShadowRoot&&(e=e.host);let o=this.#e.get(e);if(o)return[...o,...r];let n=0;for(let s=e.previousSibling;s;s=s.previousSibling)++n;let i=this.calculate(e.parentNode,[n]);return this.#e.set(e,i),[...i,...r]}},U=(t,e)=>{if(t.length+e.length===0)return 0;let[r=-1,...o]=t,[n=-1,...i]=e;return r===n?U(o,i):r<n?-1:1},de=async function*(t){let e=new Set;for await(let o of t)e.add(o);let r=new D;yield*[...e.values()].map(o=>[o,r.calculate(o)]).sort(([,o],[,n])=>U(o,n)).map(([o])=>o)},$=function(t,e){let r=JSON.parse(e);if(r.some(o=>{let n=0;return o.some(i=>(typeof i==\"string\"?++n:n=0,n>1))}))throw new Error(\"Multiple deep combinators found in sequence.\");return de(a.flatMap(r,o=>{let n=new M(t,o);return n.run(),n.elements}))},fe=async function(t,e){for await(let r of $(t,e))return r;return null};var me=Object.freeze({...x,...A,...R,..._,...C,...k,...Q,...E,Deferred:c,createFunction:F,createTextContent:d,IntervalPoller:T,isSuitableNodeForTextMatching:f,MutationPoller:y,RAFPoller:w}),he=me;\n\n      \n      return module.exports.default;\n    })()";
                    this.puppeteerUtil = this.evaluateHandle(pageFunction, EvaluateType.STRING, null);
                }
            }
        }
    }

    private JsonNode convertArgument(ExecutionContext context, Object arg) {
        ObjectNode objectNode = Constant.OBJECTMAPPER.createObjectNode();
        if (arg == null) {
            objectNode.put("value", "");
            return objectNode;
        }
        if (arg instanceof BigInteger) { // eslint-disable-line valid-typeof
            objectNode.put("unserializableValue", arg + "n");
            return objectNode;
        }
        if ("-0".equals(arg)) {
            objectNode.put("unserializableValue", "-0");
            return objectNode;
        }
        if ("Infinity".equals(arg)) {
            objectNode.put("unserializableValue", "Infinity");
            return objectNode;
        }
        if ("-Infinity".equals(arg)) {
            objectNode.put("unserializableValue", "-Infinity");
            return objectNode;
        }
        if ("NaN".equals(arg)) {
            objectNode.put("unserializableValue", "NaN");
            return objectNode;
        }
        JSHandle objectHandle = arg instanceof JSHandle ? (JSHandle) arg : null;
        if (objectHandle != null) {
            if (objectHandle.realm() != context.world()) {
                throw new JvppeteerException("JSHandles can be evaluated only in the context they were created!");
            }
            if (objectHandle.disposed()) {
                throw new JvppeteerException("JSHandle is disposed!" + objectHandle.getRemoteObject().getObjectId());

            }
            if (objectHandle.getRemoteObject().getUnserializableValue() != null) {
                objectNode.put("unserializableValue", objectHandle.getRemoteObject().getUnserializableValue());
                return objectNode;
            }
            if (StringUtil.isEmpty(objectHandle.getRemoteObject().getObjectId())) {
                return objectNode.putPOJO("value", objectHandle.getRemoteObject().getValue());
            }
            return objectNode.put("objectId", objectHandle.getRemoteObject().getObjectId());
        }
        return objectNode.putPOJO("value", arg);
    }

    private IsolatedWorld world() {
        return this.world;
    }


    public void dispose() {
        this.listener.forEach(this.client::off);
        this.emit(ExecutionContextEvent.Disposed, true);
    }

    public enum ExecutionContextEvent {
        Disposed("disposed"),
        Consoleapicalled("consoleapicalled"),
        Bindingcalled("bindingcalled");
        private String eventType;

        ExecutionContextEvent(String eventType) {

        }

        public String getEventType() {
            return eventType;
        }
    }
}
