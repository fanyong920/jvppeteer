// launch-browser-pipe.js
import {spawn} from "node:child_process";


// 从命令行参数读取 Chrome 路径和启动参数（JSON 格式）
const argsJson = process.argv[2]; // 第一个参数是 JSON 字符串
if (!argsJson) {
    console.error('Usage: node launch-browser-pipe.js "<json>"');
    process.exit(1);
}
let config;
try {
    config = JSON.parse(Buffer.from(process.argv[2], 'base64').toString('utf-8'));
} catch (e) {
    console.error('Invalid JSON:', e.message);
    process.exit(1);
}

const {executablePath, args} = config;

if (!executablePath || !Array.isArray(args)) {
    console.error('Missing executablePath or args');
    process.exit(1);
}

const chrome = spawn(executablePath, args, {
    env: process.env,
    stdio: ['pipe', 'pipe', 'pipe', 'pipe', 'pipe'] // 关键：透传 stdin/stdout
});
let pendingMessage = [];
const {3: pipeWrite, 4: pipeRead} = chrome.stdio;
//发送给java
pipeRead.on('data', (data) => {
    process.stdout.write(data);
})
//从java接收
process.stdin.on('data', (data) => {
    pendingMessage.push(data);
    if (data.indexOf('\0') === -1) {
        return;
    }
    const concatBuffer = Buffer.concat(pendingMessage);
    let start = 0;
    let end = concatBuffer.indexOf('\0');
    while (end !== -1) {
        const message = concatBuffer.toString(undefined, start, end);
        setImmediate(() => {
            pipeWrite.write(message);
            pipeWrite.write('\0');
        });
        start = end + 1;
        end = concatBuffer.indexOf('\0', start);
    }
    if (start >= concatBuffer.length) {
        pendingMessage = [];
    } else {
        pendingMessage = [concatBuffer.subarray(start)];
    }
})

// 可选：监听退出
pipeWrite.on('close', (code) => {
    const closeMessage = {"method":"Browser.close","id":25}
    process.stdout.write(JSON.stringify(closeMessage));
    process.stdout.write('\0');
    process.exit(code || 0);
});

process.on('SIGINT', () => {
    process.exit(0);
});

process.on('SIGTERM', () => {
    process.exit(0);
});
