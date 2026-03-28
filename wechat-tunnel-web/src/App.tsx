import React, { lazy, Suspense } from 'react';
import { ConfigProvider, Spin } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

const WeixinQRCode = lazy(() => import('./pages/WeixinQRCode'));
const Chat = lazy(() => import('./pages/Chat'));
const QRTest = lazy(() => import('./pages/QRTest'));

const Loading: React.FC = () => (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
    </div>
);

const App: React.FC = () => {
    return (
        <ConfigProvider locale={zhCN}>
            <BrowserRouter>
                <Suspense fallback={<Loading />}>
                    <Routes>
                        <Route path="/" element={<WeixinQRCode />} />
                        <Route path="/qr-test" element={<QRTest />} />
                        <Route path="/chat" element={<Chat />} />
                    </Routes>
                </Suspense>
            </BrowserRouter>
        </ConfigProvider>
    );
};

export default App;
