package com.seckill.order.service.impl;

import com.seckill.order.service.DlqReplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DlqReplayServiceImpl implements DlqReplayService {

    @Override
    public void replay(String eventType) {
        log.info("Replay requested for dlq eventType={}", eventType);
    }
}
