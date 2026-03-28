package com.xiaopeng.frd.wechat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接状态视图对象
 *
 * @author system
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionStatusVO {

    /** 是否已连接 */
    private Boolean connected;

    /** 连接时间戳 */
    private Long connectedAt;
}
