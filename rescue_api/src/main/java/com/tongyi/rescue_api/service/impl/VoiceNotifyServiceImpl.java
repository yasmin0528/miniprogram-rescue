package com.tongyi.rescue_api.service.impl;

import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.tongyi.rescue_api.domain.entity.Master;
import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.repository.MasterRepository;
import com.tongyi.rescue_api.service.VoiceNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class VoiceNotifyServiceImpl implements VoiceNotifyService {

    private final MasterRepository masterRepository;

    @Value("${aliyun.voice.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.voice.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.voice.ttsCode}")
    private String ttsCode;

    @Value("${aliyun.voice.calledShowNumber}")
    private String calledShowNumber;

    @Value("${aliyun.voice.playTimes:3}")
    private Integer playTimes;

    @Value("${aliyun.voice.connectTimeout:5000}")
    private Integer connectTimeout;

    @Value("${aliyun.voice.readTimeout:10000}")
    private Integer readTimeout;

    @Value("${aliyun.voice.maxAttempts:3}")
    private Integer maxAttempts;

    public VoiceNotifyServiceImpl(MasterRepository masterRepository) {
        this.masterRepository = masterRepository;
    }

    @Override
    public void notifyAgencyOnOrderCreated(Order order) {
        if (order == null) {
            return;
        }
        if (!StringUtils.hasText(order.getAgencyId())) {
            return;
        }

        Master master = masterRepository.findById(order.getAgencyId())
                .orElse(null);
        if (master == null || !StringUtils.hasText(master.getPhoneNumber())) {
            log.warn("[voiceNotify] agency not found or phone missing, agencyId={}, orderId={}", order.getAgencyId(), order.getId());
            return;
        }

        try {
            SingleCallByTtsResponse resp = callVoice(master.getPhoneNumber());
            if (resp == null || resp.body == null || !com.aliyun.teautil.Common.equalString(resp.body.code, "OK")) {
                String msg = resp != null && resp.body != null ? resp.body.message : "null response";
                log.warn("[voiceNotify] call failed, agencyId={}, orderId={}, message={}", order.getAgencyId(), order.getId(), msg);
            } else {
                log.info("[voiceNotify] call success, agencyId={}, orderId={}, calledNumber={}", order.getAgencyId(), order.getId(), master.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("[voiceNotify] exception, agencyId={}, orderId={}, err={}", order.getAgencyId(), order.getId(), e.getMessage(), e);
        }
    }

    private SingleCallByTtsResponse callVoice(String calledNumber) throws Exception {
        Client client = createClient();

        SingleCallByTtsRequest req = new SingleCallByTtsRequest()
                .setCalledNumber(calledNumber)
                .setTtsCode(ttsCode)
                .setCalledShowNumber(calledShowNumber)
                .setPlayTimes(playTimes);

        RuntimeOptions runtime = new RuntimeOptions();
        runtime.readTimeout = readTimeout;
        runtime.connectTimeout = connectTimeout;
        runtime.autoretry = true;
        runtime.maxAttempts = maxAttempts;

        return client.singleCallByTtsWithOptions(req, runtime);
    }

    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.readTimeout = readTimeout;
        config.connectTimeout = connectTimeout;
        return new Client(config);
    }
}
