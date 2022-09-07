package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @ ClassName ItemService
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/9/5 21:03
 * @ Version 1.0
 */
@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {

        ItemVo itemVo = new ItemVo();
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            //1. 根据skuId查询sku V
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new RuntimeException("您访问的商品不存在");
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getSubtitle());
            itemVo.setSubtitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, executorService);
        //2. 根据三级分类的id查询一二三级分类 V
        CompletableFuture<Void> categoryFuture = skuFuture.thenAcceptAsync(skuEntity -> {

            ResponseVo<List<CategoryEntity>> categoryResponseVo = this.pmsClient.queryLvl123CategoriesByCid3(skuEntity.getCategoryId());
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, executorService);

        //3. 根据品牌id查询品牌 V
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuId);
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        });

        //4. 根据spuId查询spu V
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        });
        //5. 根据skuId查询sku的图片列表 V
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> listResponseVo = this.pmsClient.querySkuImagesListBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = listResponseVo.getData();
            if (skuImagesEntities != null) {
                itemVo.setImages(skuImagesEntities);
            }
        }, executorService);

        //6. 根据skuId查询营销信息 V
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, executorService);

        //7. 根据skuId查询库存 V
        CompletableFuture<Void> wareSkuFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, executorService);

        //8. 根据spuId查询spu下所有sku的销售属性列表 V
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValuesBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, executorService);
        //9. 根据skuId查询当前sku的销售属性 V
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
            }
        });

        //10. 根据spuId查询spu下所有销售属性组合与skuId的映射关系 V
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = this.pmsClient.queryMappingBySpuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkuJsons(json);
        }, executorService);

        //11. 根据spuId查询spu的描述信息 V
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, executorService);

        //12. 查询规格参数分组及组下的规格参数和值 V
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupResponseVo = this.pmsClient.queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
            itemVo.setGroups(groupResponseVo.getData());
        }, executorService);

        CompletableFuture.allOf(groupFuture, descFuture, mappingFuture, saleAttrFuture, saleAttrsFuture, wareSkuFuture, salesFuture, imagesFuture, spuFuture, brandFuture, categoryFuture).join();

        //异步化改造页面静态化
        executorService.execute(() -> {
            this.generateHtml(itemVo);
        });

        return itemVo;
    }

    private void generateHtml(ItemVo itemVo)  {
        try(PrintWriter printWriter  = new PrintWriter("C:\\TransData\\html\\" + itemVo.getSkuId() + ".html");) {
            // 初始化一个上下对象
            Context context = new Context();
            // 给模板传递一个动态数据
            context.setVariable("itemVo",itemVo);
            this.templateEngine.process("item", context ,printWriter);
            // 页面静态化方法: 1-模板名称 2-上下文对象 3-文件流
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
//        // new MyThread().start();
///*        new Thread(()-> {
//            System.out.println("我是实现了Runnable接口实现了多线程程序: " + Thread.currentThread().getName());
//        }).start();
//        System.out.println("我是main方法: " + Thread.currentThread().getName());*/
//
//        FutureTask task = new FutureTask<>(new MyCallable());
//        new Thread(task).start();
//        while (!task.isDone()){
//            System.out.println("子任务还没有执行完成...");
//        }
////        try {
////            System.out.println("3获取了子任务的返回结果集:" + task.get());
////        } catch (Exception e) {
////            System.out.println("4获取子任务的异常信息: " + e.getMessage());
////        }
/*        ExecutorService executorService = new ThreadPoolExecutor(10, 15, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        executorService.execute(()->{
            System.out.println("这是线程池初始化了程序:" + Thread.currentThread().getName());
        });*/

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过CompletableFuture的runAsync初始化了一个多线程程序");
            return "hello CompatableFuture";
        });

        CompletableFuture<String> future1 = future.thenApplyAsync(t -> {
            System.out.println("===============thenApplyAsync=================");
            System.out.println("上一个任务的返回结果集: " + t);
            return "hello thenApplyAsync";
        });

        CompletableFuture<Void> future2 = future.thenAcceptAsync(t -> {
            System.out.println("===============thenAcceptAsync=================");
            System.out.println("上一个任务的返回结果集" + t);
        });
        CompletableFuture<Void> future3 = future.thenRunAsync(() -> {
            System.out.println("===============thenRunAsync=================");
            System.out.println("不获取上一个任务的返回结果集, 也没有自己的返回结果集");
        });
        CompletableFuture.allOf(future1, future2, future3).join();

        /*.whenCompleteAsync((t, u) -> {
            System.out.println("===================================" + Thread.currentThread().getName());
            System.out.println("子任务返回结果集t: " + t);
            System.out.println("子任务异常信息u: " + u);
        }).exceptionally(t -> {
            System.out.println("子任务异常信息t: " + t);
            return "hello exceptionally";
        });*/
        //System.out.println(future.get());

        System.out.println("1是main方法:" + Thread.currentThread().getName());
        System.in.read();
    }
}

class MyCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        System.out.println("2这是实现Callable接口实现了多线程程序:" + Thread.currentThread().getName());
        return "hello callable...";
    }
}


class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("我是集成了Thread基实现了多线程程序:" + Thread.currentThread().getName());
    }
}

