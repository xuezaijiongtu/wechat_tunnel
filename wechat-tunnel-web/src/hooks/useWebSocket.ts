import { useEffect, useRef, useState, useCallback } from 'react';
import { WebSocketClient } from '../services/websocket';
import type { ConnectionStatus, WsServerMessage } from '../types/message';

// 根据环境变量构建 WebSocket URL
const getWsUrl = () => {
    const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
    const wsProtocol = apiBaseUrl.startsWith('https') ? 'wss' : 'ws';
    const wsHost = apiBaseUrl.replace(/^https?:\/\//, '');
    return `${wsProtocol}://${wsHost}/ws/chat`;
};

const WS_URL = getWsUrl();

interface UseWebSocketOptions {
    onMessage?: (data: WsServerMessage) => void;
    autoConnect?: boolean;
}

export function useWebSocket(options: UseWebSocketOptions = {}) {
    const { onMessage, autoConnect = true } = options;
    const [status, setStatus] = useState<ConnectionStatus>('disconnected');
    const clientRef = useRef<WebSocketClient | null>(null);
    const onMessageRef = useRef(onMessage);
    onMessageRef.current = onMessage;

    useEffect(() => {
        const client = new WebSocketClient(WS_URL);
        clientRef.current = client;

        client.setOnStatusChange((connected) => {
            setStatus(connected ? 'connected' : 'disconnected');
        });

        client.setOnMessage((data) => {
            onMessageRef.current?.(data);
        });

        if (autoConnect) {
            setStatus('connecting');
            client.connect();
        }

        return () => {
            client.disconnect();
        };
    }, [autoConnect]);

    const sendMessage = useCallback((userId: string, content: string) => {
        clientRef.current?.send({ type: 'send_message', userId, content });
    }, []);

    const connect = useCallback(() => {
        setStatus('connecting');
        clientRef.current?.connect();
    }, []);

    const disconnect = useCallback(() => {
        clientRef.current?.disconnect();
    }, []);

    return { status, sendMessage, connect, disconnect };
}
