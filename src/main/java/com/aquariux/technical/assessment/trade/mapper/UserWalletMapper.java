package com.aquariux.technical.assessment.trade.mapper;

import com.aquariux.technical.assessment.trade.dto.internal.UserWalletDto;
import com.aquariux.technical.assessment.trade.entity.UserWallet;

import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserWalletMapper {
    
    @Select("""
            SELECT s.symbol, s.name, uw.balance 
            FROM symbols s 
            INNER JOIN user_wallets uw ON s.id = uw.symbol_id AND uw.user_id = #{userId} 
            ORDER BY s.symbol
            """)
    List<UserWalletDto> findByUserId(Long userId);

    @Select("SELECT id, user_id as userId, symbol_id as symbolId, balance, updated_at as updatedAt " +
        "FROM user_wallets WHERE user_id = #{userId} AND symbol_id = #{symbolId}")
    UserWallet findByUserIdAndSymbolId(@Param("userId") Long userId, @Param("symbolId") Long symbolId);

    @Update("UPDATE user_wallets SET balance = #{balance}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND symbol_id = #{symbolId}")
    int updateBalance(@Param("userId") Long userId, @Param("symbolId") Long symbolId, @Param("balance") BigDecimal balance);

    // Optional: insert new wallet when user first acquires a crypto (business rule)
    @Insert("INSERT INTO user_wallets (user_id, symbol_id, balance, updated_at) " +
            "VALUES (#{userId}, #{symbolId}, #{balance}, CURRENT_TIMESTAMP)")
    void insert(UserWallet wallet);
}