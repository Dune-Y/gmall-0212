package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import lombok.experimental.FieldDefaults;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ ClassName GmallPmsClient
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/8/31 11:22
 * @ Version 1.0
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
