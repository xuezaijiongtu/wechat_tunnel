import React, { memo, useState, useCallback } from 'react';
import { Input, Button } from 'antd';
import { SendOutlined } from '@ant-design/icons';

const { TextArea } = Input;

interface InputBoxProps {
    disabled: boolean;
    onSend: (content: string) => void;
}

const InputBox: React.FC<InputBoxProps> = ({ disabled, onSend }) => {
    const [value, setValue] = useState('');

    const handleSend = useCallback(() => {
        const text = value.trim();
        if (!text) return;
        onSend(text);
        setValue('');
    }, [value, onSend]);

    const handleKeyDown = useCallback(
        (e: React.KeyboardEvent) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSend();
            }
        },
        [handleSend],
    );

    return (
        <div style={{ display: 'flex', gap: 8, padding: '12px 16px', borderTop: '1px solid #f0f0f0' }}>
            <TextArea
                value={value}
                onChange={(e) => setValue(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={disabled ? '请先选择一个会话' : '输入消息，Enter 发送，Shift+Enter 换行'}
                disabled={disabled}
                autoSize={{ minRows: 1, maxRows: 4 }}
                style={{ flex: 1 }}
            />
            <Button
                type="primary"
                icon={<SendOutlined />}
                onClick={handleSend}
                disabled={disabled || !value.trim()}
            >
                发送
            </Button>
        </div>
    );
};

export default memo(InputBox);
