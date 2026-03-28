import { useState, useCallback, useRef } from 'react';
import type { Message, Session } from '../types/message';

export function useMessages() {
    const [messageMap, setMessageMap] = useState<Record<string, Message[]>>({});
    const [sessions, setSessions] = useState<Session[]>([]);
    const messageMapRef = useRef(messageMap);
    messageMapRef.current = messageMap;

    /** 添加一条消息，同时更新会话列表 */
    const addMessage = useCallback((msg: Message) => {
        setMessageMap((prev) => {
            const list = prev[msg.userId] ?? [];
            return { ...prev, [msg.userId]: [...list, msg] };
        });

        setSessions((prev) => {
            const idx = prev.findIndex((s) => s.userId === msg.userId);
            const updated: Session = {
                userId: msg.userId,
                lastMessage: msg.content,
                lastMessageTime: msg.timestamp,
                unreadCount: idx >= 0 ? prev[idx].unreadCount + (msg.direction === 'inbound' ? 1 : 0) : (msg.direction === 'inbound' ? 1 : 0),
            };
            const next = idx >= 0
                ? prev.map((s, i) => (i === idx ? updated : s))
                : [...prev, updated];
            return next.sort((a, b) => b.lastMessageTime - a.lastMessageTime);
        });
    }, []);

    /** 获取指定用户的消息列表 */
    const getMessages = useCallback(
        (userId: string): Message[] => messageMap[userId] ?? [],
        [messageMap],
    );

    /** 清除指定会话的未读计数 */
    const clearUnread = useCallback((userId: string) => {
        setSessions((prev) =>
            prev.map((s) => (s.userId === userId ? { ...s, unreadCount: 0 } : s)),
        );
    }, []);

    /** 批量设置消息（用于加载历史消息） */
    const setMessages = useCallback((userId: string, msgs: Message[]) => {
        setMessageMap((prev) => ({ ...prev, [userId]: msgs }));
    }, []);

    /** 批量设置会话列表（用于初始化） */
    const initSessions = useCallback((list: Session[]) => {
        setSessions(list.sort((a, b) => b.lastMessageTime - a.lastMessageTime));
    }, []);

    return {
        sessions,
        messageMap,
        addMessage,
        getMessages,
        clearUnread,
        setMessages,
        initSessions,
    };
}
