package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ ClassName GmallUmsClient
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/8 14:54
 * @ Version 1.0
 */

@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
