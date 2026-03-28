import React, { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';

const QRTest: React.FC = () => {
    const [data, setData] = useState<any>(null);
    const [error, setError] = useState<string>('');

    useEffect(() => {
        fetch('/api/weixin/qr')
            .then(res => res.json())
            .then(json => {
                console.log('API Response:', json);
                setData(json.data);
            })
            .catch(err => {
                console.error('API Error:', err);
                setError(err.message);
            });
    }, []);

    return (
        <div style={{ padding: 50 }}>
            <h1>二维码测试页面</h1>
            {error && <p style={{ color: 'red' }}>错误: {error}</p>}
            {!data && !error && <p>加载中...</p>}
            {data && (
                <div>
                    <p>qrcode: {data.qrcode}</p>
                    <p>qrcodeImage: {data.qrcodeImage}</p>
                    <h2>二维码:</h2>
                    <QRCodeSVG value={data.qrcodeImage} size={300} />
                </div>
            )}
        </div>
    );
};

export default QRTest;
