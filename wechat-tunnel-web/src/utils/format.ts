/**
 * 格式化用户ID为可读的显示名称
 * @param userId 完整的用户ID
 * @returns 格式化后的显示名称
 */
export function formatUserId(userId: string): string {
    if (!userId) return '未知用户';

    // 如果是微信ID格式：o9cq802kdbrhsQ2_2q0ZibFbFcg0@im.wechat
    // 提取 @ 前面的部分，并只显示前8位
    const atIndex = userId.indexOf('@');
    if (atIndex > 0) {
        const id = userId.substring(0, atIndex);
        return `微信用户 ${id.substring(0, 8)}`;
    }

    // 如果没有 @，直接显示前8位
    return `用户 ${userId.substring(0, 8)}`;
}

/**
 * 格式化时间戳
 * @param timestamp 时间戳（毫秒）
 * @returns 格式化后的时间字符串
 */
export function formatTimestamp(timestamp: number): string {
    if (!timestamp) return '';

    const date = new Date(timestamp);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    if (isToday) {
        return date.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }

    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    const isYesterday = date.toDateString() === yesterday.toDateString();

    if (isYesterday) {
        return '昨天 ' + date.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    return date.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}
