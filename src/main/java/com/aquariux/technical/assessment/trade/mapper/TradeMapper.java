package com.aquariux.technical.assessment.trade.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.aquariux.technical.assessment.trade.entity.CryptoPrice;
import com.aquariux.technical.assessment.trade.entity.Trade;

@Mapper
public interface TradeMapper {
    
    // TODO: What database operations do you need for trading?
    // Feel free to add multiple methods, complex queries, or additional mapper interfaces as needed
    @Insert("""
    INSERT INTO trades (user_id, crypto_pair_id, trade_type, quantity, price, total_amount, trade_time)
    VALUES (#{userId}, #{cryptoPairId}, #{tradeType}, #{quantity}, #{price}, #{totalAmount}, #{tradeTime})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Trade trade);
}