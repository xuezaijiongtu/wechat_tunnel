import React, { memo, useCallback } from 'react';
import { List, Avatar, Badge, Typography } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import type { Session } from '../../types/message';
import { formatUserId } from '../../utils/format';

const { Text, Paragraph } = Typography;

interface SessionListProps {
    sessions: Session[];
    activeUserId: string | null;
    onSelect: (userId: string) => void;
}

const SessionList: React.FC<SessionListProps> = ({ sessions, activeUserId, onSelect }) => {
    const formatTime = useCallback((ts: number) => {
        if (!ts) return '';
        const d = new Date(ts);
        const now = new Date();
        const isToday = d.toDateString() === now.toDateString();
        if (isToday) {
            return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        }
        return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
    }, []);

    return (
        <List
            dataSource={sessions}
            locale={{ emptyText: '暂无会话' }}
            renderItem={(item) => (
                <List.Item
                    onClick={() => onSelect(item.userId)}
                    style={{
                        cursor: 'pointer',
                        padding: '12px 16px',
                        backgroundColor: item.userId === activeUserId ? '#e6f4ff' : undefined,
                    }}
                >
                    <List.Item.Meta
                        avatar={
                            <Badge count={item.unreadCount} size="small">
                                <Avatar icon={<UserOutlined />} />
                            </Badge>
                        }
                        title={
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <Text ellipsis style={{ maxWidth: 140, fontWeight: 500 }}>
                                    {formatUserId(item.userId)}
                                </Text>
                                <Text type="secondary" style={{ fontSize: 12, flexShrink: 0 }}>
                                    {formatTime(item.lastMessageTime)}
                                </Text>
                            </div>
                        }
                        description={
                            <Paragraph ellipsis style={{ marginBottom: 0, fontSize: 13, color: '#999' }}>
                                {item.lastMessage || ' '}
                            </Paragraph>
                        }
                    />
                </List.Item>
            )}
        />
    );
};

export default memo(SessionList);
