import React, { useCallback } from 'react';
import { message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { ProForm, ProFormText } from '@ant-design/pro-components';
import { useNavigate } from 'react-router-dom';
import { login } from '../../services/api';
import { setToken } from '../../services/auth';

const Login: React.FC = () => {
    const navigate = useNavigate();

    const handleSubmit = useCallback(async (values: { password: string }) => {
        try {
            const token = await login(values.password);
            setToken(token);
            message.success('登录成功');
            navigate('/weixin-qr', { replace: true });
        } catch {
            message.error('密码错误，请重试');
        }
    }, [navigate]);

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh',
            background: '#f0f2f5',
        }}>
            <div style={{
                width: 360,
                padding: '40px 24px',
                background: '#fff',
                borderRadius: 8,
                boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
            }}>
                <h2 style={{ textAlign: 'center', marginBottom: 32 }}>
                    WeChat Tunnel
                </h2>
                <ProForm
                    onFinish={handleSubmit}
                    submitter={{
                        searchConfig: { submitText: '登录' },
                        resetButtonProps: false,
                        submitButtonProps: { block: true, size: 'large' },
                    }}
                >
                    <ProFormText.Password
                        name="password"
                        fieldProps={{
                            size: 'large',
                            prefix: <LockOutlined />,
                        }}
                        placeholder="请输入访问密码"
                        rules={[{ required: true, message: '请输入密码' }]}
                    />
                </ProForm>
            </div>
        </div>
    );
};

export default Login;
