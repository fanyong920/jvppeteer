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
    if (data.include('\0')) {
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
pipeRead.on('close', (code) => {
    kill(chrome);
    process.exit(code || 0);
});

// 添加进程清理
process.on('exit', () => {
    kill(chrome);
});

process.on('SIGINT', () => {
    kill(chrome);
    process.exit(0);
});

process.on('SIGTERM', () => {
    kill(chrome);
    process.exit(0);
});

function kill(chrome) {
    if (chrome?.pid && pidExists(chrome.pid)
    ) {
        if (process.platform === 'win32') {
            try {
                childProcess.execSync(
                    `taskkill /pid ${chrome.pid} /T /F`,
                );
            } catch (error) {
                // taskkill can fail to kill the process e.g. due to missing permissions.
                // Let's kill the process via Node API. This delays killing of all child
                // processes of `this.proc` until the main Node.js process dies.
                console.error("execSync error: ",error);
                chrome.kill();
            }
        } else {
            // on linux the process group can be killed with the group id prefixed with
            // a minus sign. The process group id is the group leader's pid.
            const processGroupId = -chrome.pid;

            try {
                process.kill(processGroupId, 'SIGKILL');
            } catch (error) {

                // Killing the process group can fail due e.g. to missing permissions.
                // Let's kill the process via Node API. This delays killing of all child
                // processes of `this.proc` until the main Node.js process dies.
                console.error("process.kill error: ",error);
                chrome.kill('SIGKILL');
            }
        }
    }
}

function pidExists(pid) {
    try {
        return process.kill(pid, 0);
    } catch (error) {
        console.error("pidExists error: ",error);
        return false;
    }
}