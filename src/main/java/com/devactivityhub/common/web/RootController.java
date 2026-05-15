package com.devactivityhub.common.web;

import com.devactivityhub.common.api.ApiMessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RootController {

    @GetMapping("/health")
    public ApiMessageResponse health() {
        return new ApiMessageResponse("dev-activity-hub is running");
    }
}
