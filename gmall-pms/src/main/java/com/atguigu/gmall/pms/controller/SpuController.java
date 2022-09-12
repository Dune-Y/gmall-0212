package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SpuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * spu信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-07-26 22:40:19
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("category/{categoryId}")
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySpuByCidAndPage(PageParamVo paramVo,@PathVariable("categoryId")Long categoryId){

        PageResultVo pageResultVo = spuService.querySpuByCidAndPage(paramVo,categoryId);

        return ResponseVo.ok(pageResultVo);
    }

    /**
     * 列表
     */

    @PostMapping("json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo pageParamVo){
        PageResultVo page = spuService.queryPage(pageParamVo);
        List<SpuEntity> list = (List<SpuEntity>)page.getList();
        return ResponseVo.ok(list);
    }

    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySpuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = spuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id){
		SpuEntity spu = spuService.getById(id);

        return ResponseVo.ok(spu);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SpuVo spuVo){

		spuService.bigSave(spuVo);
        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SpuEntity spu){
		spuService.updateById(spu);
        this.rabbitTemplate.convertAndSend("PMS_SPU_EXCHANGE","item.update",spu.getId());
        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		spuService.removeByIds(ids);
        return ResponseVo.ok();
    }



}
