import React, { memo, useEffect, useRef } from 'react';
import { Empty } from 'antd';
import type { Message } from '../../types/message';
import { formatUserId } from '../../utils/format';
import { parseWeChatEmoji } from '../../utils/emoji';

interface MessageAreaProps {
    messages: Message[];
    activeUserId: string | null;
}

const MessageArea: React.FC<MessageAreaProps> = ({ messages, activeUserId }) => {
    const bottomRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    if (!activeUserId) {
        return (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                <Empty description="选择一个会话开始聊天" />
            </div>
        );
    }

    if (messages.length === 0) {
        return (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                <Empty description="暂无消息" />
            </div>
        );
    }

    const formatTime = (ts: number) => {
        const d = new Date(ts);
        return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    };

    return (
        <div style={{
            padding: '20px',
            overflowY: 'auto',
            height: '100%',
            background: '#f5f5f5'
        }}>
            {messages.map((msg, idx) => {
                const isOutbound = msg.direction === 'outbound';
                return (
                    <div
                        key={`${msg.timestamp}-${idx}`}
                        style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: isOutbound ? 'flex-end' : 'flex-start',
                            marginBottom: 20,
                        }}
                    >
                        {!isOutbound && (
                            <div style={{
                                fontSize: 12,
                                color: '#666',
                                marginBottom: 6,
                                marginLeft: 4
                            }}>
                                {formatUserId(msg.userId)}
                            </div>
                        )}
                        <div style={{
                            maxWidth: '60%',
                            padding: '12px 16px',
                            borderRadius: 12,
                            wordBreak: 'break-word',
                            fontSize: 15,
                            lineHeight: 1.6,
                            backgroundColor: isOutbound ? '#95ec69' : '#fff',
                            border: isOutbound ? 'none' : '1px solid #e8e8e8',
                            boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
                            whiteSpace: 'pre-wrap',
                        }}>
                            {parseWeChatEmoji(msg.content)}
                        </div>
                        <div style={{
                            fontSize: 12,
                            color: '#999',
                            marginTop: 6,
                            marginLeft: isOutbound ? 0 : 4,
                            marginRight: isOutbound ? 4 : 0
                        }}>
                            {formatTime(msg.timestamp)}
                        </div>
                    </div>
                );
            })}
            <div ref={bottomRef} />
        </div>
    );
};

export default memo(MessageArea);
