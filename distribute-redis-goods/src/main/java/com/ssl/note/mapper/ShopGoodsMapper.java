package com.ssl.note.mapper;

import com.ssl.note.model.ShopGoods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShopGoodsMapper {

    int deleteByPrimaryKey(Long goodsId);

    int insert(ShopGoods record);

    int insertSelective(ShopGoods record);

    ShopGoods selectByPrimaryKey(Long goodsId);

    int updateByPrimaryKeySelective(ShopGoods record);

    int updateByPrimaryKey(ShopGoods record);
}