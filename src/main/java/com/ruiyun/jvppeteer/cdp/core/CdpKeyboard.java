package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Keyboard;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.KeyDefinition;
import com.ruiyun.jvppeteer.cdp.entities.KeyDescription;
import com.ruiyun.jvppeteer.cdp.entities.KeyDownOptions;
import com.ruiyun.jvppeteer.cdp.entities.KeyPressOptions;
import com.ruiyun.jvppeteer.cdp.entities.KeyboardTypeOptions;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import static com.ruiyun.jvppeteer.util.Helper.justWait;

public class CdpKeyboard extends Keyboard {

    private CDPSession client;

    private int modifiers;

    private final Set<String> pressedKeys = new HashSet<>();


    public CdpKeyboard(CDPSession client) {
        super();
        this.client = client;
    }

    /**
     * 调度 keydown 事件。<p>
     * 如果 key 是单个字符，并且除了 Shift 之外没有按下任何修改键，也会生成 keypress/input 事件。可以指定 text 选项来强制生成输入事件。如果 key 是修饰键、Shift、Meta、Control 或 Alt，则后续按键将在该修饰键处于活动状态时发送。要释放修饰键，请使用 Keyboard.up()。
     * <p>
     * 按下该键一次后，后续调用 Keyboard.down() 会将 repeat 设置为 true。要释放密钥，请使用 Keyboard.up()。
     * <p>
     * 修饰键确实会影响 Keyboard.down()。按住 Shift 将以大写形式键入文本。
     * <p>
     *
     * @param key     要按下的键的名称，例如 ArrowLeft。有关所有键名称的列表，请参阅 {@link Keyboard#keyDefinitions}。
     * @param options 选项。接受文本，如果指定，则使用该文本生成输入事件。接受命令，如果指定，则为键盘快捷键的命令
     */
    public void down(String key, KeyDownOptions options) {
        KeyDescription description = this.keyDescriptionForString(key);
        boolean autoRepeat = this.pressedKeys.contains(description.getCode());
        this.pressedKeys.add(description.getCode());
        this.modifiers |= this.modifierBit(description.getKey());
        if (StringUtil.isEmpty(options.getText())) {
            options.setText(description.getText());
        }
        Map<String, Object> params = ParamsFactory.create();
        if (StringUtil.isNotEmpty(options.getText())) {
            params.put("type", "keyDown");
        } else {
            params.put("type", "rawKeyDown");
        }
        params.put("modifiers", this.modifiers);
        params.put("windowsVirtualKeyCode", description.getKeyCode());
        params.put("code", description.getCode());
        params.put("key", description.getKey());
        params.put("text", options.getText());
        params.put("unmodifiedText", options.getText());
        params.put("autoRepeat", autoRepeat);
        params.put("location", description.getLocation());
        params.put("isKeypad", description.getLocation() == 3);
        params.put("commands", options.getCommands());
        this.client.send("Input.dispatchKeyEvent", params);
    }

    /**
     * 调度 keydown 事件。<p>
     * 如果 key 是单个字符，并且除了 Shift 之外没有按下任何修改键，也会生成 keypress/input 事件。可以指定 text 选项来强制生成输入事件。如果 key 是修饰键、Shift、Meta、Control 或 Alt，则后续按键将在该修饰键处于活动状态时发送。要释放修饰键，请使用 Keyboard.up()。
     * <p>
     * 按下该键一次后，后续调用 Keyboard.down() 会将 repeat 设置为 true。要释放密钥，请使用 Keyboard.up()。
     * <p>
     * 修饰键确实会影响 Keyboard.down()。按住 Shift 将以大写形式键入文本。
     * <p>
     *
     * @param key 要按下的键的名称，例如 ArrowLeft。有关所有键名称的列表，请参阅 {@link CdpKeyboard#keyDefinitions}。
     */
    public void down(String key) {
        this.down(key, new KeyDownOptions());
    }

    /**
     * 调度 keyup 事件。
     *
     * @param key 要释放的密钥的名称，
     */
    public void up(String key) {
        KeyDescription description = this.keyDescriptionForString(key);
        this.modifiers &= ~this.modifierBit(description.getKey());
        this.pressedKeys.remove(description.getCode());
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "keyUp");
        params.put("modifiers", this.modifiers);
        params.put("key", description.getKey());
        params.put("windowsVirtualKeyCode", description.getKeyCode());
        params.put("code", description.getCode());
        params.put("location", description.getLocation());
        this.client.send("Input.dispatchKeyEvent", params);
    }

    /**
     * 调度 keypress 和 input 事件。这不会发送 keydown 或 keyup 事件。
     * <p>
     * 修改键不会影响 Keyboard.sendCharacter。按住 Shift 将不会键入大写文本。
     *
     * @param cha 要发送到页面的字符。
     */
    public void sendCharacter(String cha) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("text", cha);
        this.client.send("Input.insertText", params);
    }

    private boolean charIsKey(String c) {
        return keyDefinitions.containsKey(c);
    }

    /**
     * 为文本中的每个字符发送 keydown、keypress/input 和 keyup 事件。
     *
     * @param text    要输入的文本。
     * @param options 选项
     */
    public void type(String text, KeyboardTypeOptions options) {
        for (int i = 0; i < text.length(); i++) {
            String c = String.valueOf(text.charAt(i));
            if (this.charIsKey(c)) {
                KeyPressOptions pressOptions = new KeyPressOptions();
                pressOptions.setDelay(options.getDelay());
                this.press(c, pressOptions);
            } else {
                if (options.getDelay() > 0) {
                    justWait(options.getDelay());
                }
                this.sendCharacter(c);
            }
        }
    }


    /**
     * Keyboard.down() 和 Keyboard.up() 的快捷方式。
     *
     * @param key     要按下的键的名称，例如 ArrowLeft。有关所有键名称的列表，请参阅 {@link CdpKeyboard#keyDefinitions}。
     * @param options 选项
     */
    public void press(String key, KeyPressOptions options) {
        KeyDownOptions downOptions = new KeyDownOptions();
        downOptions.setText(options.getText());
        downOptions.setCommands(options.getCommands());
        this.down(key, downOptions);
        if (options.getDelay() > 0)
            justWait(options.getDelay());
        this.up(key);
    }

    private int modifierBit(String key) {
        if ("Alt".equals(key))
            return 1;
        if ("Control".equals(key))
            return 2;
        if ("Meta".equals(key))
            return 4;
        if ("Shift".equals(key))
            return 8;
        return 0;
    }

    private KeyDescription keyDescriptionForString(String keyString) {

        int shift = this.modifiers & 8;
        KeyDescription description = new KeyDescription("", 0, "", "", 0);
        KeyDefinition definition = keyDefinitions.get(keyString);
        Objects.requireNonNull(definition, "Unknown key: " + keyString);

        if (StringUtil.isNotEmpty(definition.getKey()))
            description.setKey(definition.getKey());
        if (shift != 0 && StringUtil.isNotEmpty(definition.getShiftKey()))
            description.setKey(definition.getShiftKey());

        if (definition.getKeyCode() != 0)
            description.setKeyCode(definition.getKeyCode());
        if (shift != 0 && definition.getShiftKeyCode() != 0)
            description.setKeyCode(definition.getShiftKeyCode());

        if (StringUtil.isNotEmpty(definition.getCode()))
            description.setCode(definition.getCode());
        if (definition.getLocation() != 0)
            description.setLocation(definition.getLocation());

        if (description.getKey().length() == 1)
            description.setText(description.getKey());

        if (StringUtil.isNotEmpty(definition.getText()))
            description.setText(definition.getText());
        if (shift != 0 && StringUtil.isNotEmpty(definition.getShiftText()))
            description.setText(definition.getShiftText());

        // if any modifiers besides shift are pressed, no text should be sent
        if ((this.modifiers & ~8) != 0)
            description.setText("");
        return description;
    }

    int getModifiers() {
        return modifiers;
    }

    void updateClient(CdpCDPSession client) {
        this.client = client;
    }
}
