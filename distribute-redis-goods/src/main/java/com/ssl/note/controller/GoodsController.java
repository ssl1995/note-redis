package com.ssl.note.controller;

import com.ssl.note.service.GoodsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoodsController {
    private static final Logger logger = LoggerFactory.getLogger(GoodsController.class);
    private static final String SUCCESS = "success";
    private static final String FAILUER = "failure";
    @Autowired
    private GoodsServiceImpl goodsService;

    /**
     * http://127.0.0.1:8081/updateGoods?orderId=98&goodsId=13&goodsNumber=1
     * 扣减存库的接口，地址为：http://127.0.0.1:8081/updateGoods?orderId=98&goodsId=13&goodsNumber=1
     */
    @GetMapping("/updateGoods")
    public String updateGoods(@RequestParam("goodsId") long goodsId,
                              @RequestParam("goodsNumber") int goodsNumber) {
        int ireturn;
        try {
            ireturn = goodsService.updateGoods(goodsId, goodsNumber);
        } catch (Exception e) {
            logger.error("扣减存库失败，goodsId：[" + goodsId + "]");
            e.printStackTrace();
            return FAILUER;
        }
        if (ireturn > 0) {
            return SUCCESS;
        } else {
            logger.error("扣减存库失败，goodsId：[" + goodsId + "]");
            return FAILUER;
        }
    }
}
