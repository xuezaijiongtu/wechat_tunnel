import React, { useState, useEffect, useCallback, useRef, memo } from 'react';
import { Card, Typography, Spin, Button, Tag, message } from 'antd';
import { QrcodeOutlined, ReloadOutlined, CheckCircleOutlined, ScanOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { getWeixinQR, pollQRStatus } from '../../services/api';

const { Title, Text } = Typography;

type QRState = 'loading' | 'ready' | 'scanned' | 'confirmed' | 'expired' | 'error';

const POLL_INTERVAL = 2500;

const WeixinQRCode: React.FC = memo(() => {
    const navigate = useNavigate();
    const [qrState, setQrState] = useState<QRState>('loading');
    const [qrcodeImage, setQrcodeImage] = useState('');
    const [qrcode, setQrcode] = useState('');
    const pollTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const mountedRef = useRef(true);

    const stopPolling = useCallback(() => {
        if (pollTimerRef.current) {
            clearTimeout(pollTimerRef.current);
            pollTimerRef.current = null;
        }
    }, []);

    const fetchQR = useCallback(async () => {
        console.log('fetchQR 开始执行');
        setQrState('loading');
        stopPolling();
        try {
            console.log('准备调用 getWeixinQR');
            const data = await getWeixinQR();
            console.log('getWeixinQR 返回数据:', data);
            console.log('data.qrcode:', data.qrcode);
            console.log('data.qrcodeImage:', data.qrcodeImage);
            // 去掉 mountedRef 检查，让状态正常更新
            setQrcodeImage(data.qrcodeImage);
            setQrcode(data.qrcode);
            setQrState('ready');
            console.log('状态已设置为 ready, qrcode=', data.qrcode, 'qrcodeImage=', data.qrcodeImage);
        } catch (error) {
            console.error('fetchQR 错误:', error);
            setQrState('error');
            message.error('获取二维码失败，请重试');
        }
    }, [stopPolling]);

    const startPolling = useCallback(() => {
        if (!qrcode) return;

        const poll = async () => {
            try {
                const data = await pollQRStatus(qrcode);

                if (data.status === 'scanned') {
                    setQrState('scanned');
                    pollTimerRef.current = setTimeout(poll, POLL_INTERVAL);
                } else if (data.status === 'confirmed') {
                    setQrState('confirmed');
                    if (data.botToken) {
                        localStorage.setItem('botToken', data.botToken);
                    }
                    message.success('登录成功，正在跳转...');
                    setTimeout(() => {
                        navigate('/chat', { replace: true });
                    }, 1000);
                } else if (data.status === 'expired') {
                    setQrState('expired');
                } else {
                    pollTimerRef.current = setTimeout(poll, POLL_INTERVAL);
                }
            } catch {
                pollTimerRef.current = setTimeout(poll, POLL_INTERVAL);
            }
        };

        pollTimerRef.current = setTimeout(poll, POLL_INTERVAL);
    }, [qrcode, navigate]);

    useEffect(() => {
        fetchQR();
        return () => {
            mountedRef.current = false;
            stopPolling();
        };
    }, [fetchQR, stopPolling]);

    useEffect(() => {
        if (qrcode && qrState === 'ready') {
            startPolling();
        }
        return stopPolling;
    }, [qrcode, qrState, startPolling, stopPolling]);

    const renderStatus = useCallback(() => {
        switch (qrState) {
            case 'loading':
                return <Spin tip="正在获取二维码..." />;
            case 'ready':
                return <Tag icon={<QrcodeOutlined />} color="blue">请用微信扫码登录</Tag>;
            case 'scanned':
                return <Tag icon={<ScanOutlined />} color="orange">已扫码，请在手机上确认</Tag>;
            case 'confirmed':
                return <Tag icon={<CheckCircleOutlined />} color="green">登录成功！</Tag>;
            case 'expired':
                return <Tag color="red">二维码已过期，请重新获取</Tag>;
            case 'error':
                return <Tag color="red">获取二维码失败</Tag>;
            default:
                return null;
        }
    }, [qrState]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f5f5f5' }}>
            <Card style={{ width: 400, textAlign: 'center' }}>
                <Title level={3} style={{ marginBottom: 24 }}>微信扫码登录</Title>

                <div style={{ minHeight: 280, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', marginBottom: 16 }}>
                    {qrState === 'loading' && <Spin size="large" />}
                    {qrcode && qrState !== 'loading' && qrState !== 'error' && (
                        <div style={{ opacity: qrState === 'expired' ? 0.3 : 1 }}>
                            <QRCodeSVG
                                value={qrcodeImage}
                                size={240}
                                level="M"
                                includeMargin={true}
                            />
                        </div>
                    )}
                </div>

                <div style={{ marginBottom: 16 }}>{renderStatus()}</div>

                {(qrState === 'expired' || qrState === 'error') && (
                    <Button type="primary" icon={<ReloadOutlined />} onClick={fetchQR}>
                        重新获取
                    </Button>
                )}

                <div style={{ marginTop: 16 }}>
                    <Text type="secondary">打开微信，扫一扫登录</Text>
                </div>
            </Card>
        </div>
    );
});

export default WeixinQRCode;
