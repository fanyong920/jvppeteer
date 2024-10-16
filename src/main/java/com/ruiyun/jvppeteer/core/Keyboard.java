package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.KeyDefinition;
import com.ruiyun.jvppeteer.entities.KeyDescription;
import com.ruiyun.jvppeteer.entities.KeyDownOptions;
import com.ruiyun.jvppeteer.entities.KeyPressOptions;
import com.ruiyun.jvppeteer.entities.KeyboardTypeOptions;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.ruiyun.jvppeteer.util.Helper.justWait;

public class Keyboard {

    private CDPSession client;

    private int modifiers;

    private final Set<String> pressedKeys = new HashSet<>();

    private static final Map<String, KeyDefinition> keyDefinitions = new HashMap<>();

    static {
        keyDefinitions.put("0", new KeyDefinition(48, "0", "Digit0"));
        keyDefinitions.put("1", new KeyDefinition(49, "1", "Digit1"));
        keyDefinitions.put("2", new KeyDefinition(50, "2", "Digit2"));
        keyDefinitions.put("3", new KeyDefinition(51, "3", "Digit3"));
        keyDefinitions.put("4", new KeyDefinition(52, "4", "Digit4"));
        keyDefinitions.put("5", new KeyDefinition(53, "5", "Digit5"));
        keyDefinitions.put("6", new KeyDefinition(54, "6", "Digit6"));
        keyDefinitions.put("7", new KeyDefinition(55, "7", "Digit7"));
        keyDefinitions.put("8", new KeyDefinition(56, "8", "Digit8"));
        keyDefinitions.put("9", new KeyDefinition(57, "9", "Digit9"));
        keyDefinitions.put("Power", new KeyDefinition("Power", "Power"));
        keyDefinitions.put("Eject", new KeyDefinition("Eject", "Eject"));
        keyDefinitions.put("Abort", new KeyDefinition(3, "Cancel", "Abort"));
        keyDefinitions.put("Help", new KeyDefinition(6, "Help", "Help"));
        keyDefinitions.put("Backspace", new KeyDefinition(8, "Backspace", "Backspace"));
        keyDefinitions.put("Tab", new KeyDefinition(9, "Tab", "Tab"));
        keyDefinitions.put("Numpad5", new KeyDefinition(12, 101, "Clear", "Numpad5", "5", 3));
        keyDefinitions.put("NumpadEnter", new KeyDefinition(13, "NumpadEnter", "Enter", "\r", 3));
        keyDefinitions.put("Enter", new KeyDefinition(13, "Enter", "Enter", "\r"));
        keyDefinitions.put("\r", new KeyDefinition(13, "Enter", "Enter", "\r"));
        keyDefinitions.put("\n", new KeyDefinition(13, "Enter", "Enter", "\r"));
        keyDefinitions.put("ShiftLeft", new KeyDefinition(16, "Shift", "ShiftLeft", 1));
        keyDefinitions.put("ShiftRight", new KeyDefinition(16, "Shift", "ShiftRight", 2));
        keyDefinitions.put("ControlLeft", new KeyDefinition(17, "Control", "ControlLeft", 1));
        keyDefinitions.put("ControlRight", new KeyDefinition(17, "Control", "ControlRight", 2));
        keyDefinitions.put("AltLeft", new KeyDefinition(18, "Alt", "AltLeft", 1));
        keyDefinitions.put("AltRight", new KeyDefinition(18, "Alt", "AltRight", 2));
        keyDefinitions.put("Pause", new KeyDefinition(19, "Pause", "Pause"));
        keyDefinitions.put("CapsLock", new KeyDefinition(20, "CapsLock", "CapsLock"));
        keyDefinitions.put("Escape", new KeyDefinition(27, "Escape", "Escape"));
        keyDefinitions.put("Convert", new KeyDefinition(28, "Convert", "Convert"));
        keyDefinitions.put("NonConvert", new KeyDefinition(29, "NonConvert", "NonConvert"));
        keyDefinitions.put("Space", new KeyDefinition(32, " ", "Space"));
        keyDefinitions.put("Numpad9", new KeyDefinition(33, 105, "PageUp", "Numpad9", "9", 3));
        keyDefinitions.put("PageUp", new KeyDefinition(33, "PageUp", "PageUp"));
        keyDefinitions.put("Numpad3", new KeyDefinition(34, 99, "PageDown", "Numpad3", "3", 3));
        keyDefinitions.put("PageDown", new KeyDefinition(34, "PageDown", "PageDown"));
        keyDefinitions.put("End", new KeyDefinition(35, "End", "End"));
        keyDefinitions.put("Numpad1", new KeyDefinition(35, 97, "End", "Numpad1", "1", 3));
        keyDefinitions.put("Home", new KeyDefinition(36, "Home", "Home"));
        keyDefinitions.put("Numpad7", new KeyDefinition(36, 103, "Home", "Numpad7", "7", 3));
        keyDefinitions.put("ArrowLeft", new KeyDefinition(37, "ArrowLeft", "ArrowLeft"));
        keyDefinitions.put("Numpad4", new KeyDefinition(37, 100, "ArrowLeft", "Numpad4", "4", 3));
        keyDefinitions.put("Numpad8", new KeyDefinition(38, 104, "ArrowUp", "Numpad8", "8", 3));
        keyDefinitions.put("ArrowUp", new KeyDefinition(38, "ArrowUp", "ArrowUp"));
        keyDefinitions.put("ArrowRight", new KeyDefinition(39, "ArrowRight", "ArrowRight"));
        keyDefinitions.put("Numpad6", new KeyDefinition(39, 102, "ArrowRight", "Numpad6", "6", 3));
        keyDefinitions.put("Numpad2", new KeyDefinition(40, 98, "ArrowDown", "Numpad2", "2", 3));
        keyDefinitions.put("ArrowDown", new KeyDefinition(40, "ArrowDown", "ArrowDown"));
        keyDefinitions.put("Select", new KeyDefinition(41, "Select", "Select"));
        keyDefinitions.put("Open", new KeyDefinition(43, "Execute", "Open"));
        keyDefinitions.put("PrintScreen", new KeyDefinition(44, "PrintScreen", "PrintScreen"));
        keyDefinitions.put("Insert", new KeyDefinition(45, "Insert", "Insert"));
        keyDefinitions.put("Numpad0", new KeyDefinition(45, 96, "Insert", "Numpad0", "0", 3));
        keyDefinitions.put("Delete", new KeyDefinition(46, "Delete", "Delete"));
        keyDefinitions.put("NumpadDecimal", new KeyDefinition(46, 110, "NumpadDecimal", "\u0000", ".", 3));
        keyDefinitions.put("Digit0", new KeyDefinition(48, "Digit0", ")", "0"));
        keyDefinitions.put("Digit1", new KeyDefinition(49, "Digit1", "!", "1"));
        keyDefinitions.put("Digit2", new KeyDefinition(50, "Digit2", "@", "2"));
        keyDefinitions.put("Digit3", new KeyDefinition(51, "Digit3", "#", "3"));
        keyDefinitions.put("Digit4", new KeyDefinition(52, "Digit4", "$", "4"));
        keyDefinitions.put("Digit5", new KeyDefinition(53, "Digit5", "%", "5"));
        keyDefinitions.put("Digit6", new KeyDefinition(54, "Digit6", "^", "6"));
        keyDefinitions.put("Digit7", new KeyDefinition(55, "Digit7", "&", "7"));
        keyDefinitions.put("Digit8", new KeyDefinition(56, "Digit8", "*", "8"));
        keyDefinitions.put("Digit9", new KeyDefinition(57, "Digit9", "(", "9"));
        keyDefinitions.put("KeyA", new KeyDefinition(65, "KeyA", "A", "a"));
        keyDefinitions.put("KeyB", new KeyDefinition(66, "KeyB", "B", "b"));
        keyDefinitions.put("KeyC", new KeyDefinition(67, "KeyC", "C", "c"));
        keyDefinitions.put("KeyD", new KeyDefinition(68, "KeyD", "D", "d"));
        keyDefinitions.put("KeyE", new KeyDefinition(69, "KeyE", "E", "e"));
        keyDefinitions.put("KeyF", new KeyDefinition(70, "KeyF", "F", "f"));
        keyDefinitions.put("KeyG", new KeyDefinition(71, "KeyG", "G", "g"));
        keyDefinitions.put("KeyH", new KeyDefinition(72, "KeyH", "H", "h"));
        keyDefinitions.put("KeyI", new KeyDefinition(73, "KeyI", "I", "i"));
        keyDefinitions.put("KeyJ", new KeyDefinition(74, "KeyJ", "J", "j"));
        keyDefinitions.put("KeyK", new KeyDefinition(75, "KeyK", "K", "k"));
        keyDefinitions.put("KeyL", new KeyDefinition(76, "KeyL", "L", "l"));
        keyDefinitions.put("KeyM", new KeyDefinition(77, "KeyM", "M", "m"));
        keyDefinitions.put("KeyN", new KeyDefinition(78, "KeyN", "N", "n"));
        keyDefinitions.put("KeyO", new KeyDefinition(79, "KeyO", "O", "o"));
        keyDefinitions.put("KeyP", new KeyDefinition(80, "KeyP", "P", "p"));
        keyDefinitions.put("KeyQ", new KeyDefinition(81, "KeyQ", "Q", "q"));
        keyDefinitions.put("KeyR", new KeyDefinition(82, "KeyR", "R", "r"));
        keyDefinitions.put("KeyS", new KeyDefinition(83, "KeyS", "S", "s"));
        keyDefinitions.put("KeyT", new KeyDefinition(84, "KeyT", "T", "t"));
        keyDefinitions.put("KeyU", new KeyDefinition(85, "KeyU", "U", "u"));
        keyDefinitions.put("KeyV", new KeyDefinition(86, "KeyV", "V", "v"));
        keyDefinitions.put("KeyW", new KeyDefinition(87, "KeyW", "W", "w"));
        keyDefinitions.put("KeyX", new KeyDefinition(88, "KeyX", "X", "x"));
        keyDefinitions.put("KeyY", new KeyDefinition(89, "KeyY", "Y", "y"));
        keyDefinitions.put("KeyZ", new KeyDefinition(90, "KeyZ", "Z", "z"));
        keyDefinitions.put("MetaLeft", new KeyDefinition(91, "Meta", "MetaLeft", 1));
        keyDefinitions.put("MetaRight", new KeyDefinition(92, "Meta", "MetaRight", 2));
        keyDefinitions.put("ContextMenu", new KeyDefinition(93, "ContextMenu", "ContextMenu"));
        keyDefinitions.put("NumpadMultiply", new KeyDefinition(106, "*", "NumpadMultiply", 3));
        keyDefinitions.put("NumpadAdd", new KeyDefinition(107, "+", "NumpadAdd", 3));
        keyDefinitions.put("NumpadSubtract", new KeyDefinition(109, "-", "NumpadSubtract", 3));
        keyDefinitions.put("NumpadDivide", new KeyDefinition(111, "/", "NumpadDivide", 3));
        keyDefinitions.put("F1", new KeyDefinition(112, "F1", "F1"));
        keyDefinitions.put("F2", new KeyDefinition(113, "F2", "F2"));
        keyDefinitions.put("F3", new KeyDefinition(114, "F3", "F3"));
        keyDefinitions.put("F4", new KeyDefinition(115, "F4", "F4"));
        keyDefinitions.put("F5", new KeyDefinition(116, "F5", "F5"));
        keyDefinitions.put("F6", new KeyDefinition(117, "F6", "F6"));
        keyDefinitions.put("F7", new KeyDefinition(118, "F7", "F7"));
        keyDefinitions.put("F8", new KeyDefinition(119, "F8", "F8"));
        keyDefinitions.put("F9", new KeyDefinition(120, "F9", "F9"));
        keyDefinitions.put("F10", new KeyDefinition(121, "F10", "F10"));
        keyDefinitions.put("F11", new KeyDefinition(122, "F11", "F11"));
        keyDefinitions.put("F12", new KeyDefinition(123, "F12", "F12"));
        keyDefinitions.put("F13", new KeyDefinition(124, "F13", "F13"));
        keyDefinitions.put("F14", new KeyDefinition(125, "F14", "F14"));
        keyDefinitions.put("F15", new KeyDefinition(126, "F15", "F15"));
        keyDefinitions.put("F16", new KeyDefinition(127, "F16", "F16"));
        keyDefinitions.put("F17", new KeyDefinition(128, "F17", "F17"));
        keyDefinitions.put("F18", new KeyDefinition(129, "F18", "F18"));
        keyDefinitions.put("F19", new KeyDefinition(130, "F19", "F19"));
        keyDefinitions.put("F20", new KeyDefinition(131, "F20", "F20"));
        keyDefinitions.put("F21", new KeyDefinition(132, "F21", "F21"));
        keyDefinitions.put("F22", new KeyDefinition(133, "F22", "F22"));
        keyDefinitions.put("F23", new KeyDefinition(134, "F23", "F23"));
        keyDefinitions.put("F24", new KeyDefinition(135, "F24", "F24"));
        keyDefinitions.put("NumLock", new KeyDefinition(144, "NumLock", "NumLock"));
        keyDefinitions.put("ScrollLock", new KeyDefinition(145, "ScrollLock", "ScrollLock"));
        keyDefinitions.put("AudioVolumeMute", new KeyDefinition(173, "AudioVolumeMute", "AudioVolumeMute"));
        keyDefinitions.put("AudioVolumeDown", new KeyDefinition(174, "AudioVolumeDown", "AudioVolumeDown"));
        keyDefinitions.put("AudioVolumeUp", new KeyDefinition(175, "AudioVolumeUp", "AudioVolumeUp"));
        keyDefinitions.put("MediaTrackNext", new KeyDefinition(176, "MediaTrackNext", "MediaTrackNext"));
        keyDefinitions.put("MediaTrackPrevious", new KeyDefinition(177, "MediaTrackPrevious", "MediaTrackPrevious"));
        keyDefinitions.put("MediaStop", new KeyDefinition(178, "MediaStop", "MediaStop"));
        keyDefinitions.put("MediaPlayPause", new KeyDefinition(179, "MediaPlayPause", "MediaPlayPause"));
        keyDefinitions.put("Semicolon", new KeyDefinition(186, "Semicolon", ":", ";"));
        keyDefinitions.put("Equal", new KeyDefinition(187, "Equal", "+", "="));
        keyDefinitions.put("NumpadEqual", new KeyDefinition(187, "=", "NumpadEqual", 3));
        keyDefinitions.put("Comma", new KeyDefinition(188, "Comma", "<", ","));
        keyDefinitions.put("Minus", new KeyDefinition(189, "Minus", "_", "-"));
        keyDefinitions.put("Period", new KeyDefinition(190, "Period", ">", "."));
        keyDefinitions.put("Slash", new KeyDefinition(191, "Slash", "?", "/"));
        keyDefinitions.put("Backquote", new KeyDefinition(192, "Backquote", "~", "`"));
        keyDefinitions.put("BracketLeft", new KeyDefinition(219, "BracketLeft", "{", "["));
        keyDefinitions.put("Backslash", new KeyDefinition(220, "Backslash", "|", "\\"));
        keyDefinitions.put("BracketRight", new KeyDefinition(221, "BracketRight", "}", "]"));
        keyDefinitions.put("Quote", new KeyDefinition(222, "Quote", "\"", "'"));
        keyDefinitions.put("AltGraph", new KeyDefinition(225, "AltGraph", "AltGraph"));
        keyDefinitions.put("Props", new KeyDefinition(247, "CrSel", "Props"));
        keyDefinitions.put("Cancel", new KeyDefinition(3, "Cancel", "Abort"));
        keyDefinitions.put("Clear", new KeyDefinition(12, "Clear", "Numpad5", 3));
        keyDefinitions.put("Shift", new KeyDefinition(16, "Shift", "ShiftLeft", 1));
        keyDefinitions.put("Control", new KeyDefinition(17, "Control", "ControlLeft", 1));
        keyDefinitions.put("Alt", new KeyDefinition(18, "Alt", "AltLeft", 1));
        keyDefinitions.put("Accept", new KeyDefinition(30, "Accept"));
        keyDefinitions.put("ModeChange", new KeyDefinition(31, "ModeChange"));
        keyDefinitions.put(" ", new KeyDefinition(32, " ", "Space"));
        keyDefinitions.put("Print", new KeyDefinition(42, "Print"));
        keyDefinitions.put("Execute", new KeyDefinition(43, "Execute", "Open"));
        keyDefinitions.put("\u0000", new KeyDefinition(46, "\u0000", "NumpadDecimal", 3));
        keyDefinitions.put("d", new KeyDefinition(68, "d", "KeyD"));
        keyDefinitions.put("e", new KeyDefinition(69, "e", "KeyE"));
        keyDefinitions.put("f", new KeyDefinition(70, "f", "KeyF"));
        keyDefinitions.put("g", new KeyDefinition(71, "g", "KeyG"));
        keyDefinitions.put("h", new KeyDefinition(72, "h", "KeyH"));
        keyDefinitions.put("i", new KeyDefinition(73, "i", "KeyI"));
        keyDefinitions.put("j", new KeyDefinition(74, "j", "KeyJ"));
        keyDefinitions.put("k", new KeyDefinition(75, "k", "KeyK"));
        keyDefinitions.put("l", new KeyDefinition(76, "l", "KeyL"));
        keyDefinitions.put("m", new KeyDefinition(77, "m", "KeyM"));
        keyDefinitions.put("n", new KeyDefinition(78, "n", "KeyN"));
        keyDefinitions.put("o", new KeyDefinition(79, "o", "KeyO"));
        keyDefinitions.put("p", new KeyDefinition(80, "p", "KeyP"));
        keyDefinitions.put("q", new KeyDefinition(81, "q", "KeyQ"));
        keyDefinitions.put("r", new KeyDefinition(82, "r", "KeyR"));
        keyDefinitions.put("s", new KeyDefinition(83, "s", "KeyS"));
        keyDefinitions.put("t", new KeyDefinition(84, "t", "KeyT"));
        keyDefinitions.put("u", new KeyDefinition(85, "u", "KeyU"));
        keyDefinitions.put("v", new KeyDefinition(86, "v", "KeyV"));
        keyDefinitions.put("w", new KeyDefinition(87, "w", "KeyW"));
        keyDefinitions.put("x", new KeyDefinition(88, "x", "KeyX"));
        keyDefinitions.put("y", new KeyDefinition(89, "y", "KeyY"));
        keyDefinitions.put("z", new KeyDefinition(90, "z", "KeyZ"));
        keyDefinitions.put("Meta", new KeyDefinition(91, "Meta", "MetaLeft", 1));
        keyDefinitions.put("*", new KeyDefinition(106, "*", "NumpadMultiply", 3));
        keyDefinitions.put("+", new KeyDefinition(107, "+", "NumpadAdd", 3));
        keyDefinitions.put("-", new KeyDefinition(109, "-", "NumpadSubtract", 3));
        keyDefinitions.put("/", new KeyDefinition(111, "/", "NumpadDivide", 3));
        keyDefinitions.put(";", new KeyDefinition(186, ";", "Semicolon"));
        keyDefinitions.put("=", new KeyDefinition(187, "=", "Equal"));
        keyDefinitions.put(",", new KeyDefinition(188, ",", "Comma"));
        keyDefinitions.put(".", new KeyDefinition(190, ".", "Period"));
        keyDefinitions.put("`", new KeyDefinition(192, "`", "Backquote"));
        keyDefinitions.put("[", new KeyDefinition(219, "[", "BracketLeft"));
        keyDefinitions.put("\\", new KeyDefinition(220, "\\", "Backslash"));
        keyDefinitions.put("]", new KeyDefinition(221, "]", "BracketRight"));
        keyDefinitions.put("'", new KeyDefinition(222, "'", "Quote"));
        keyDefinitions.put("Attn", new KeyDefinition(246, "Attn"));
        keyDefinitions.put("CrSel", new KeyDefinition(247, "CrSel", "Props"));
        keyDefinitions.put("ExSel", new KeyDefinition(248, "ExSel"));
        keyDefinitions.put("EraseEof", new KeyDefinition(249, "EraseEof"));
        keyDefinitions.put("Play", new KeyDefinition(250, "Play"));
        keyDefinitions.put("ZoomOut", new KeyDefinition(251, "ZoomOut"));
        keyDefinitions.put(")", new KeyDefinition(48, ")", "Digit0"));
        keyDefinitions.put("!", new KeyDefinition(49, "!", "Digit1"));
        keyDefinitions.put("@", new KeyDefinition(50, "@", "Digit2"));
        keyDefinitions.put("#", new KeyDefinition(51, "#", "Digit3"));
        keyDefinitions.put("$", new KeyDefinition(52, "$", "Digit4"));
        keyDefinitions.put("%", new KeyDefinition(53, "%", "Digit5"));
        keyDefinitions.put("^", new KeyDefinition(54, "^", "Digit6"));
        keyDefinitions.put("&", new KeyDefinition(55, "&", "Digit7"));
        keyDefinitions.put("(", new KeyDefinition(57, "(", "Digit9"));
        keyDefinitions.put("A", new KeyDefinition(65, "A", "KeyA"));
        keyDefinitions.put("B", new KeyDefinition(66, "B", "KeyB"));
        keyDefinitions.put("C", new KeyDefinition(67, "C", "KeyC"));
        keyDefinitions.put("D", new KeyDefinition(68, "D", "KeyD"));
        keyDefinitions.put("E", new KeyDefinition(69, "E", "KeyE"));
        keyDefinitions.put("F", new KeyDefinition(70, "F", "KeyF"));
        keyDefinitions.put("G", new KeyDefinition(71, "G", "KeyG"));
        keyDefinitions.put("H", new KeyDefinition(72, "H", "KeyH"));
        keyDefinitions.put("I", new KeyDefinition(73, "I", "KeyI"));
        keyDefinitions.put("J", new KeyDefinition(74, "J", "KeyJ"));
        keyDefinitions.put("K", new KeyDefinition(75, "K", "KeyK"));
        keyDefinitions.put("L", new KeyDefinition(76, "L", "KeyL"));
        keyDefinitions.put("M", new KeyDefinition(77, "M", "KeyM"));
        keyDefinitions.put("N", new KeyDefinition(78, "N", "KeyN"));
        keyDefinitions.put("O", new KeyDefinition(79, "O", "KeyO"));
        keyDefinitions.put("P", new KeyDefinition(80, "P", "KeyP"));
        keyDefinitions.put("Q", new KeyDefinition(81, "Q", "KeyQ"));
        keyDefinitions.put("R", new KeyDefinition(82, "R", "KeyR"));
        keyDefinitions.put("S", new KeyDefinition(83, "S", "KeyS"));
        keyDefinitions.put("T", new KeyDefinition(84, "T", "KeyT"));
        keyDefinitions.put("U", new KeyDefinition(85, "U", "KeyU"));
        keyDefinitions.put("V", new KeyDefinition(86, "V", "KeyV"));
        keyDefinitions.put("W", new KeyDefinition(87, "W", "KeyW"));
        keyDefinitions.put("X", new KeyDefinition(88, "X", "KeyX"));
        keyDefinitions.put("Y", new KeyDefinition(89, "Y", "KeyY"));
        keyDefinitions.put("Z", new KeyDefinition(90, "Z", "KeyZ"));
        keyDefinitions.put(":", new KeyDefinition(186, ":", "Semicolon"));
        keyDefinitions.put("<", new KeyDefinition(188, "<", "Comma"));
        keyDefinitions.put("_", new KeyDefinition(189, "_", "Minus"));
        keyDefinitions.put(">", new KeyDefinition(190, ">", "Period"));
        keyDefinitions.put("?", new KeyDefinition(191, "?", "Slash"));
        keyDefinitions.put("~", new KeyDefinition(192, "~", "Backquote"));
        keyDefinitions.put("{", new KeyDefinition(219, "{", "BracketLeft"));
        keyDefinitions.put("|", new KeyDefinition(220, "|", "Backslash"));
        keyDefinitions.put("}", new KeyDefinition(221, "}", "BracketRight"));
        keyDefinitions.put("\"", new KeyDefinition(222, "\"", "Quote"));
        keyDefinitions.put("SoftLeft", new KeyDefinition("SoftLeft", "SoftLeft", 4));
        keyDefinitions.put("SoftRight", new KeyDefinition("SoftRight", "SoftRight", 4));
        keyDefinitions.put("Camera", new KeyDefinition(44, "Camera", "Camera", 4));
        keyDefinitions.put("Call", new KeyDefinition("Call", "Call", 4));
        keyDefinitions.put("EndCall", new KeyDefinition(95, "EndCall", "EndCall", 4));
        keyDefinitions.put("VolumeDown", new KeyDefinition(182, "VolumeDown", "VolumeDown", 4));
        keyDefinitions.put("VolumeUp", new KeyDefinition(183, "VolumeUp", "VolumeUp", 4));
    }

    public Keyboard(CDPSession client) {
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
     * @param key 要按下的键的名称，例如 ArrowLeft。有关所有键名称的列表，请参阅 {@link Keyboard#keyDefinitions}。
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
     * @param text 要输入的文本。
     */
    public void type(String text) {
        this.type(text, new KeyboardTypeOptions());
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
     * @param key 要按下的键的名称，例如 ArrowLeft。有关所有键名称的列表，请参阅 {@link Keyboard#keyDefinitions}。
     */
    public void press(String key) {
        this.press(key, new KeyPressOptions());
    }

    /**
     * Keyboard.down() 和 Keyboard.up() 的快捷方式。
     *
     * @param key     要按下的键的名称，例如 ArrowLeft。有关所有键名称的列表，请参阅 {@link Keyboard#keyDefinitions}。
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

    void updateClient(CDPSession client) {
        this.client = client;
    }
}
