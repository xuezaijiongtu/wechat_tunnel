import request from './request';
import type { Session, Message } from '../types/message';

/** 登录，返回 token 字符串 */
export function login(password: string): Promise<string> {
    return request.post('/auth/login', { password });
}

/** 获取微信二维码 */
export function getWeixinQR(): Promise<{ qrcode: string; qrcodeImage: string }> {
    return request.get('/weixin/qr');
}

/** 轮询扫码状态 */
export function pollQRStatus(qrcode: string): Promise<{ status: string; botToken?: string }> {
    return request.get('/weixin/qr/status', { params: { qrcode } });
}

/** 获取微信连接状态 */
export function getWeixinStatus(): Promise<{ connected: boolean }> {
    return request.get('/weixin/status');
}

/** 获取会话列表 */
export function getSessions(): Promise<Session[]> {
    return request.get('/sessions');
}

/** 获取消息历史 */
export function getMessages(userId: string): Promise<Message[]> {
    return request.get('/messages', { params: { userId } });
}
