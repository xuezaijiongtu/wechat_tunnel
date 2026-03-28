import React, { useState, useCallback, useEffect } from 'react';
import { Layout, Typography, Tag } from 'antd';
import SessionList from './SessionList';
import MessageArea from './MessageArea';
import InputBox from './InputBox';
import { useWebSocket } from '../../hooks/useWebSocket';
import { useMessages } from '../../hooks/useMessages';
import { getSessions, getMessages } from '../../services/api';
import { formatUserId } from '../../utils/format';
import type { ConnectionStatus, WsServerMessage } from '../../types/message';

const { Sider, Content } = Layout;
const { Text } = Typography;

const statusTagMap: Record<ConnectionStatus, { color: string; text: string }> = {
    connected: { color: 'green', text: '已连接' },
    connecting: { color: 'orange', text: '连接中' },
    disconnected: { color: 'red', text: '已断开' },
};

const Chat: React.FC = () => {
    const [activeUserId, setActiveUserId] = useState<string | null>(null);
    const { sessions, addMessage, getMessages: getMsgs, clearUnread, setMessages, initSessions } = useMessages();

    const handleWsMessage = useCallback(
        (data: WsServerMessage) => {
            if (data.type === 'new_message') {
                addMessage(data.message);
            }
        },
        [addMessage],
    );

    const { status, sendMessage } = useWebSocket({ onMessage: handleWsMessage });

    // 初始化：加载会话列表
    useEffect(() => {
        getSessions()
            .then((list) => {
                if (Array.isArray(list)) {
                    initSessions(list);
                }
            })
            .catch(() => {});
    }, [initSessions]);

    // 切换会话时加载历史消息
    useEffect(() => {
        if (!activeUserId) return;
        getMessages(activeUserId)
            .then((msgs) => {
                if (Array.isArray(msgs)) {
                    setMessages(activeUserId, msgs);
                }
            })
            .catch(() => {});
    }, [activeUserId, setMessages]);

    const handleSelectSession = useCallback(
        (userId: string) => {
            setActiveUserId(userId);
            clearUnread(userId);
        },
        [clearUnread],
    );

    const handleSend = useCallback(
        (content: string) => {
            if (!activeUserId) return;
            sendMessage(activeUserId, content);
            // 不在本地添加消息，等待后端WebSocket推送回来，避免重复显示
        },
        [activeUserId, sendMessage],
    );

    const tag = statusTagMap[status];
    const currentMessages = activeUserId ? getMsgs(activeUserId) : [];

    return (
        <Layout style={{ height: '100vh' }}>
            <Sider width={300} style={{ background: '#fff', borderRight: '1px solid #f0f0f0', display: 'flex', flexDirection: 'column' }}>
                <div style={{ padding: '12px 16px', borderBottom: '1px solid #f0f0f0', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <Text strong>会话</Text>
                    <Tag color={tag.color}>{tag.text}</Tag>
                </div>
                <div style={{ flex: 1, overflowY: 'auto' }}>
                    <SessionList sessions={sessions} activeUserId={activeUserId} onSelect={handleSelectSession} />
                </div>
            </Sider>
            <Content style={{ display: 'flex', flexDirection: 'column', background: '#f5f5f5' }}>
                {activeUserId && (
                    <div style={{ padding: '16px 20px', borderBottom: '1px solid #e8e8e8', background: '#fff' }}>
                        <Text strong style={{ fontSize: 16 }}>{formatUserId(activeUserId)}</Text>
                    </div>
                )}
                <div style={{ flex: 1, overflow: 'hidden' }}>
                    <MessageArea messages={currentMessages} activeUserId={activeUserId} />
                </div>
                <InputBox disabled={!activeUserId || status !== 'connected'} onSend={handleSend} />
            </Content>
        </Layout>
    );
};

export default Chat;
