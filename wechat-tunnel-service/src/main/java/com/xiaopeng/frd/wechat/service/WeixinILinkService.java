package com.xiaopeng.frd.wechat.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaopeng.frd.wechat.dto.GetUpdatesResponse;
import com.xiaopeng.frd.wechat.dto.QRCodeDTO;
import com.xiaopeng.frd.wechat.dto.QRStatusDTO;
import com.xiaopeng.frd.wechat.dto.WeixinMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * 微信iLink协议服务
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Service
public class WeixinILinkService {

    @Value("${weixin.base-url:https://ilinkai.weixin.qq.com}")
    private String baseUrl;

    @Value("${weixin.long-polling-timeout:35000}")
    private int longPollingTimeout;

    @Value("${weixin.bot-type:3}")
    private int botType;

    private final RestTemplate restTemplate;

    public WeixinILinkService() {
        this.restTemplate = new RestTemplate();
        // 配置UTF-8字符集支持
        this.restTemplate.getMessageConverters().forEach(converter -> {
            if (converter instanceof org.springframework.http.converter.StringHttpMessageConverter) {
                ((org.springframework.http.converter.StringHttpMessageConverter) converter)
                        .setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8);
            }
        });
    }

    private String botToken;
    private String getUpdatesBuf = "";
    private volatile boolean polling = false;
    private Long connectedAt;

    /** 收到微信消息的回调 */
    private Consumer<WeixinMessageDTO> messageCallback;

    /**
     * 设置消息回调
     *
     * @param callback 消息处理回调
     */
    public void setMessageCallback(Consumer<WeixinMessageDTO> callback) {
        this.messageCallback = callback;
    }

    /**
     * 获取QR登录二维码
     *
     * @return 二维码信息
     */
    public QRCodeDTO getQRCode() {
        String url = baseUrl + "/ilink/bot/get_bot_qrcode?bot_type=" + botType;
        log.info("获取QR二维码, url={}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);

            JSONObject json = JSON.parseObject(response.getBody());
            QRCodeDTO dto = new QRCodeDTO();
            dto.setQrcode(json.getString("qrcode"));
            // 微信API返回的字段是 qrcode_img_content，不是 qrcode_image
            dto.setQrcodeImage(json.getString("qrcode_img_content"));
            return dto;
        } catch (Exception e) {
            log.error("获取QR二维码失败", e);
            throw new RuntimeException("获取QR二维码失败: " + e.getMessage());
        }
    }

    /**
     * 轮询QR扫码状态
     *
     * @param qrcode 二维码字符串
     * @return 扫码状态
     */
    public QRStatusDTO pollQRStatus(String qrcode) {
        String url = baseUrl + "/ilink/bot/get_qrcode_status?qrcode=" + qrcode;
        log.debug("轮询QR状态, qrcode={}", qrcode);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);

            JSONObject json = JSON.parseObject(response.getBody());
            QRStatusDTO dto = new QRStatusDTO();
            dto.setStatus(json.getString("status"));

            if ("confirmed".equals(dto.getStatus())) {
                this.botToken = json.getString("bot_token");
                dto.setBotToken(this.botToken);
                log.info("扫码确认成功, botToken已获取");
            }

            return dto;
        } catch (Exception e) {
            log.error("轮询QR状态失败", e);
            throw new RuntimeException("轮询QR状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动Long-Polling循环
     */
    @Async
    public void startPolling() {
        polling = true;
        connectedAt = System.currentTimeMillis();
        log.info("启动Long-Polling循环");

        int backoffMs = 1000;
        while (polling) {
            try {
                GetUpdatesResponse resp = getUpdates();

                if (resp.getErrcode() != null && resp.getErrcode() == -14) {
                    log.warn("Session过期(errcode=-14), 停止Polling");
                    stopPolling();
                    break;
                }

                if (resp.getMsgs() != null) {
                    for (WeixinMessageDTO msg : resp.getMsgs()) {
                        if (messageCallback != null) {
                            messageCallback.accept(msg);
                        }
                    }
                }

                if (resp.getGetUpdatesBuf() != null) {
                    getUpdatesBuf = resp.getGetUpdatesBuf();
                }

                // 成功后重置退避
                backoffMs = 1000;
            } catch (Exception e) {
                log.error("Long-polling异常, 退避{}ms后重试", backoffMs, e);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                backoffMs = Math.min(backoffMs * 2, 30000);
            }
        }

        connectedAt = null;
        log.info("Long-Polling循环已停止");
    }

    /**
     * 停止Long-Polling
     */
    public void stopPolling() {
        polling = false;
    }

    /**
     * 发送消息到微信
     *
     * @param toUserId     目标用户ID
     * @param text         消息文本
     * @param contextToken 上下文Token
     */
    public void sendMessage(String toUserId, String text, String contextToken) {
        String url = baseUrl + "/ilink/bot/sendmessage";
        log.info("发送消息, toUserId={}", toUserId);

        try {
            JSONObject body = new JSONObject();
            JSONObject msg = new JSONObject();
            msg.put("to_user_id", toUserId);
            msg.put("context_token", contextToken);

            JSONArray itemList = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("type", 1);
            long now = System.currentTimeMillis();
            item.put("create_time_ms", now);
            item.put("update_time_ms", now);
            item.put("is_completed", true);
            JSONObject textItem = new JSONObject();
            textItem.put("text", text);
            item.put("text_item", textItem);
            itemList.add(item);
            msg.put("item_list", itemList);

            body.put("msg", msg);

            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), authHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 检查响应
            String responseBody = response.getBody();
            log.info("发送消息响应: {}", responseBody);

            if (responseBody != null) {
                JSONObject respJson = JSON.parseObject(responseBody);
                Integer errcode = respJson.getInteger("errcode");
                if (errcode != null && errcode != 0) {
                    String errmsg = respJson.getString("errmsg");
                    log.error("消息发送失败, errcode={}, errmsg={}, toUserId={}", errcode, errmsg, toUserId);
                    throw new RuntimeException("微信API返回错误: " + errcode + " - " + errmsg);
                }
            }

            log.info("消息发送成功, toUserId={}", toUserId);
        } catch (Exception e) {
            log.error("发送消息失败, toUserId={}", toUserId, e);
            throw new RuntimeException("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 是否正在轮询
     *
     * @return true-正在轮询
     */
    public boolean isPolling() {
        return polling;
    }

    /**
     * 获取连接时间
     *
     * @return 连接时间戳，未连接返回null
     */
    public Long getConnectedAt() {
        return connectedAt;
    }

    // ==================== 私有方法 ====================

    /**
     * 拉取消息更新
     */
    private GetUpdatesResponse getUpdates() {
        String url = baseUrl + "/ilink/bot/getupdates";

        JSONObject body = new JSONObject();
        body.put("get_updates_buf", getUpdatesBuf);

        JSONObject baseInfo = new JSONObject();
        baseInfo.put("bot_type", botType);
        body.put("base_info", baseInfo);

        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), authHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JSONObject json = JSON.parseObject(response.getBody());
        GetUpdatesResponse resp = new GetUpdatesResponse();
        resp.setErrcode(json.getInteger("errcode"));
        resp.setErrmsg(json.getString("errmsg"));
        resp.setGetUpdatesBuf(json.getString("get_updates_buf"));

        JSONArray msgsArray = json.getJSONArray("msgs");
        if (msgsArray != null) {
            List<WeixinMessageDTO> msgs = new ArrayList<>();
            for (int i = 0; i < msgsArray.size(); i++) {
                JSONObject msgJson = msgsArray.getJSONObject(i);
                WeixinMessageDTO dto = new WeixinMessageDTO();
                dto.setFromUserId(msgJson.getString("from_user_id"));
                dto.setContextToken(msgJson.getString("context_token"));
                dto.setItemList(msgJson.get("item_list"));
                dto.setRawMessage(msgJson);
                msgs.add(dto);
            }
            resp.setMsgs(msgs);
        }

        return resp;
    }

    /**
     * 生成防重放UIN
     */
    private String generateUIN() {
        int uint32 = new Random().nextInt();
        return Base64.getEncoder().encodeToString(String.valueOf(uint32).getBytes());
    }

    /**
     * 构造认证Headers
     */
    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("AuthorizationType", "ilink_bot_token");
        if (botToken != null) {
            headers.set("Authorization", "Bearer " + botToken);
        }
        headers.set("X-WECHAT-UIN", generateUIN());
        return headers;
    }
}
