package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ ClassName GmallSmsApi
 * @ Description
 * @ Author Nimodo
 * @ Date 2022/8/1 16:07
 * @ Version 1.0
 */

public interface GmallSmsApi {

    @PostMapping("sms/skubounds/sales/save")
    ResponseVo saveSkuSaleInfo(@RequestBody SkuSaleVo saleVo);


}
