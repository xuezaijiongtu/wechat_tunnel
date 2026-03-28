package com.xiaopeng.frd.wechat.controller;

import com.xiaopeng.frd.wechat.common.ResponseWrapper;
import com.xiaopeng.frd.wechat.dto.QRCodeDTO;
import com.xiaopeng.frd.wechat.dto.QRStatusDTO;
import com.xiaopeng.frd.wechat.service.WeixinILinkService;
import com.xiaopeng.frd.wechat.vo.ConnectionStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信登录接口
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@RestController
@RequestMapping("/api/weixin")
public class WeixinController {

    private final WeixinILinkService weixinILinkService;

    public WeixinController(WeixinILinkService weixinILinkService) {
        this.weixinILinkService = weixinILinkService;
    }

    /**
     * 获取QR登录二维码
     *
     * @return 二维码信息
     */
    @GetMapping("/qr")
    public ResponseWrapper<QRCodeDTO> getQRCode() {
        try {
            QRCodeDTO dto = weixinILinkService.getQRCode();
            return ResponseWrapper.success(dto);
        } catch (Exception e) {
            log.error("获取QR二维码失败", e);
            return ResponseWrapper.fail("500", "获取二维码失败: " + e.getMessage());
        }
    }

    /**
     * 轮询QR扫码状态
     *
     * @param qrcode 二维码字符串
     * @return 扫码状态
     */
    @GetMapping("/qr/status")
    public ResponseWrapper<QRStatusDTO> getQRStatus(@RequestParam String qrcode) {
        try {
            QRStatusDTO dto = weixinILinkService.pollQRStatus(qrcode);

            // 扫码确认后启动Long-Polling
            if ("confirmed".equals(dto.getStatus())) {
                weixinILinkService.startPolling();
                log.info("扫码确认, 已启动Long-Polling");
            }

            return ResponseWrapper.success(dto);
        } catch (Exception e) {
            log.error("轮询QR状态失败", e);
            return ResponseWrapper.fail("500", "轮询状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取微信连接状态
     *
     * @return 连接状态
     */
    @GetMapping("/status")
    public ResponseWrapper<ConnectionStatusVO> getStatus() {
        ConnectionStatusVO vo = ConnectionStatusVO.builder()
                .connected(weixinILinkService.isPolling())
                .connectedAt(weixinILinkService.getConnectedAt())
                .build();
        return ResponseWrapper.success(vo);
    }
}
