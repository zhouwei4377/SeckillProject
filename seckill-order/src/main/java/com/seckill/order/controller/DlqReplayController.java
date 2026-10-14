package com.seckill.order.controller;

import com.seckill.order.service.DlqReplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin")
public class DlqReplayController {

    @Resource
    private DlqReplayService dlqReplayService;

    @GetMapping("/dlq")
    public ResponseEntity<String> listDlq() {
        return ResponseEntity.ok("DLQ inspection is enabled. Use replay endpoint for manual recovery.");
    }

    @PostMapping("/dlq/replay/{eventType}")
    public ResponseEntity<String> replay(@PathVariable String eventType) {
        dlqReplayService.replay(eventType);
        return ResponseEntity.ok("replay scheduled for " + eventType);
    }
}
