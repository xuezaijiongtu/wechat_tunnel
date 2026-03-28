const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
    // HTTP API 代理
    app.use(
        createProxyMiddleware({
            target: 'http://localhost:8080',
            changeOrigin: true,
            pathFilter: '/api',
        })
    );

    // WebSocket 代理 - 使用 http:// 而不是 ws://
    app.use(
        createProxyMiddleware({
            target: 'http://localhost:8080',
            changeOrigin: true,
            pathFilter: '/ws',
            ws: true,
        })
    );
};
