import type { WsSendMessage, WsServerMessage } from '../types/message';

const RECONNECT_DELAY = 3000;

export type MessageHandler = (data: WsServerMessage) => void;

export class WebSocketClient {
    private ws: WebSocket | null = null;
    private url: string;
    private onMessage: MessageHandler | null = null;
    private onStatusChange: ((connected: boolean) => void) | null = null;
    private shouldReconnect = true;
    private reconnectTimer: ReturnType<typeof setTimeout> | null = null;

    constructor(url: string) {
        this.url = url;
    }

    /** 建立连接 */
    connect(): void {
        this.shouldReconnect = true;
        this.doConnect();
    }

    /** 断开连接 */
    disconnect(): void {
        this.shouldReconnect = false;
        this.clearReconnectTimer();
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }

    /** 发送消息 */
    send(msg: WsSendMessage): void {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(msg));
        }
    }

    /** 注册消息回调 */
    setOnMessage(handler: MessageHandler): void {
        this.onMessage = handler;
    }

    /** 注册连接状态回调 */
    setOnStatusChange(handler: (connected: boolean) => void): void {
        this.onStatusChange = handler;
    }

    /** 当前是否已连接 */
    get connected(): boolean {
        return this.ws?.readyState === WebSocket.OPEN;
    }

    private doConnect(): void {
        try {
            this.ws = new WebSocket(this.url);
        } catch {
            this.scheduleReconnect();
            return;
        }

        this.ws.onopen = () => {
            this.onStatusChange?.(true);
        };

        this.ws.onmessage = (event) => {
            try {
                const data: WsServerMessage = JSON.parse(event.data);
                this.onMessage?.(data);
            } catch {
                // ignore malformed messages
            }
        };

        this.ws.onclose = () => {
            this.onStatusChange?.(false);
            this.scheduleReconnect();
        };

        this.ws.onerror = () => {
            this.ws?.close();
        };
    }

    private scheduleReconnect(): void {
        if (!this.shouldReconnect) return;
        this.clearReconnectTimer();
        this.reconnectTimer = setTimeout(() => this.doConnect(), RECONNECT_DELAY);
    }

    private clearReconnectTimer(): void {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
    }
}
