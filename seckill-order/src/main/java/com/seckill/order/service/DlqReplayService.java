package com.seckill.order.service;

public interface DlqReplayService {

    void replay(String eventType);
}
