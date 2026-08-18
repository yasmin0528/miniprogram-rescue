package com.tongyi.rescue_api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongyi.rescue_api.domain.dto.WechatCode2SessionResponse;
import com.tongyi.rescue_api.service.WechatService;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WechatServiceImpl implements WechatService {

    private static final Logger log = LoggerFactory.getLogger(WechatServiceImpl.class);

    @Autowired
    private Environment env;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String mask(String v) {
        if (!StringUtils.hasText(v)) {
            return "";
        }
        int left = Math.min(6, v.length());
        return v.substring(0, left) + "***";
    }

    @Override
    public WechatCode2SessionResponse code2Session(String clientType, String loginCode) {
        if (!StringUtils.hasText(loginCode)) {
            throw new IllegalArgumentException("loginCode 不能为空");
        }

        String prefix = "wechat.miniapp.configs." + clientType + ".";
        String appid = env.getProperty(prefix + "appid");
        String secret = env.getProperty(prefix + "secret");
        String code2SessionUrl = env.getProperty(prefix + "code2session-url", "https://api.weixin.qq.com/sns/jscode2session");

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(secret)) {
            throw new IllegalStateException("微信小程序 AppId/Secret 未配置: " + clientType);
        }

        log.info("[login-debug][backend] wechat code2session config clientType={}, appid={}, loginCodePrefix={}",
                clientType, appid, mask(loginCode));

        String url = String.format(
                "%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                code2SessionUrl,
                URLEncoder.encode(appid, StandardCharsets.UTF_8),
                URLEncoder.encode(secret, StandardCharsets.UTF_8),
                URLEncoder.encode(loginCode, StandardCharsets.UTF_8)
        );

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                log.info("[login-debug][backend] code2session raw response={}", result);
                return objectMapper.readValue(result.toString(), WechatCode2SessionResponse.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("调用微信 code2Session 接口失败", e);
        }
    }

    @Override
    public String getPhoneNumberByCode(String clientType, String phoneCode) {
        if (!StringUtils.hasText(phoneCode)) {
            return null;
        }

        String prefix = "wechat.miniapp.configs." + clientType + ".";
        String appid = env.getProperty(prefix + "appid");
        String secret = env.getProperty(prefix + "secret");
        String accessTokenUrl = env.getProperty(prefix + "access-token-url", "https://api.weixin.qq.com/cgi-bin/token");
        String phoneNumberUrl = env.getProperty(prefix + "phone-number-url", "https://api.weixin.qq.com/wxa/business/getuserphonenumber");

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(secret)) {
            throw new IllegalStateException("微信小程序 AppId/Secret 未配置: " + clientType);
        }

        log.info("[login-debug][backend] getPhoneNumberByCode start clientType={}, appid={}, phoneCodePrefix={}",
                clientType, appid, mask(phoneCode));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String accessToken = fetchAccessToken(httpClient, accessTokenUrl, appid, secret);
            if (!StringUtils.hasText(accessToken)) {
                throw new RuntimeException("获取微信 access_token 失败");
            }

            String requestUrl = phoneNumberUrl + "?access_token=" + accessToken;
            HttpPost request = new HttpPost(requestUrl);
            request.setHeader("Content-Type", "application/json; charset=UTF-8");
            request.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                    "{\"code\":\"" + phoneCode + "\"}", StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("[login-debug][backend] getuserphonenumber raw response={}", result);

                JsonNode root = objectMapper.readTree(result);
                int errcode = root.path("errcode").asInt(-1);
                if (errcode != 0) {
                    String errmsg = root.path("errmsg").asText();
                    throw new RuntimeException("调用 getuserphonenumber 失败，errcode=" + errcode + ", errmsg=" + errmsg);
                }

                JsonNode phoneInfo = root.path("phone_info");
                if (!phoneInfo.isMissingNode()) {
                    String phoneNumber = phoneInfo.path("phoneNumber").asText();
                    if (!StringUtils.hasText(phoneNumber)) {
                        phoneNumber = phoneInfo.path("purePhoneNumber").asText();
                    }
                    log.info("[login-debug][backend] getuserphonenumber parsed phonePrefix={}", mask(phoneNumber));
                    return StringUtils.hasText(phoneNumber) ? phoneNumber : null;
                }

                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("通过 phoneCode 换取手机号失败", e);
        }
    }

    private String fetchAccessToken(CloseableHttpClient httpClient, String accessTokenUrl, String appid, String secret) throws Exception {
        String requestUrl = String.format(
                "%s?grant_type=client_credential&appid=%s&secret=%s",
                accessTokenUrl,
                URLEncoder.encode(appid, StandardCharsets.UTF_8),
                URLEncoder.encode(secret, StandardCharsets.UTF_8)
        );

        HttpGet request = new HttpGet(requestUrl);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("[login-debug][backend] access_token raw response={}", result);

            JsonNode root = objectMapper.readTree(result);
            if (root.has("access_token")) {
                return root.get("access_token").asText();
            }

            int errcode = root.path("errcode").asInt(-1);
            String errmsg = root.path("errmsg").asText();
            throw new RuntimeException("获取 access_token 失败，errcode=" + errcode + ", errmsg=" + errmsg);
        }
    }
}
