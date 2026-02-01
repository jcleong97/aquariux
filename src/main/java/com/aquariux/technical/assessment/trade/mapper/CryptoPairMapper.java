package com.aquariux.technical.assessment.trade.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.aquariux.technical.assessment.trade.entity.CryptoPair;

@Mapper
public interface CryptoPairMapper {
    
    @Select("""
            SELECT id FROM crypto_pairs WHERE pair_name = #{pairName}
            """)
    Long findIdByPairName(String pairName);

    @Select("""
            SELECT id, base_symbol_id as baseSymbolId, quote_symbol_id as quoteSymbolId, pair_name as pairName 
            FROM crypto_pairs WHERE id = #{id}
            """)
    CryptoPair findById(Long id);
}