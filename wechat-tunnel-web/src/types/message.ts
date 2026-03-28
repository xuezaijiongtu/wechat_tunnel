/** 消息方向 */
export type MessageDirection = 'inbound' | 'outbound';

/** 消息 */
export interface Message {
    userId: string;
    content: string;
    direction: MessageDirection;
    timestamp: number;
}

/** 会话 */
export interface Session {
    userId: string;
    lastMessage: string;
    lastMessageTime: number;
    unreadCount: number;
}

/** WebSocket 连接状态 */
export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected';

/** 客户端 -> 服务端：发送消息 */
export interface WsSendMessage {
    type: 'send_message';
    userId: string;
    content: string;
}

/** 服务端 -> 客户端：新消息 */
export interface WsNewMessage {
    type: 'new_message';
    message: Message;
}

/** 服务端 -> 客户端：连接状态变化 */
export interface WsConnectionStatus {
    type: 'connection_status';
    connected: boolean;
}

/** 服务端 -> 客户端的所有消息类型 */
export type WsServerMessage = WsNewMessage | WsConnectionStatus;
