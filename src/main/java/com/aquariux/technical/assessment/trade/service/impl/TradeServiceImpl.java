package com.aquariux.technical.assessment.trade.service.impl;

import com.aquariux.technical.assessment.trade.dto.request.TradeRequest;
import com.aquariux.technical.assessment.trade.dto.response.TradeResponse;
import com.aquariux.technical.assessment.trade.entity.CryptoPair;
import com.aquariux.technical.assessment.trade.entity.CryptoPrice;
import com.aquariux.technical.assessment.trade.entity.Trade;
import com.aquariux.technical.assessment.trade.entity.UserWallet;
import com.aquariux.technical.assessment.trade.enums.TradeType;
import com.aquariux.technical.assessment.trade.exception.InsufficientBalanceException;
import com.aquariux.technical.assessment.trade.mapper.CryptoPairMapper;
import com.aquariux.technical.assessment.trade.mapper.CryptoPriceMapper;
import com.aquariux.technical.assessment.trade.mapper.TradeMapper;
import com.aquariux.technical.assessment.trade.mapper.UserWalletMapper;
import com.aquariux.technical.assessment.trade.service.TradeServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeServiceInterface {

    private final TradeMapper tradeMapper;
    private final CryptoPairMapper cryptoPairMapper;
    private final CryptoPriceMapper cryptoPriceMapper;
    private final UserWalletMapper userWalletMapper;
    // Add additional beans here if needed for your implementation

    @Transactional
    @Override
    public TradeResponse executeTrade(TradeRequest tradeRequest) {
        // TODO: Implement the core trading engine
        // What should happen when a user executes a trade?

        // 1. Validate input
        if (tradeRequest.getUserId() == null || tradeRequest.getTradeType() == null 
            || tradeRequest.getPairName() == null || tradeRequest.getQuantity() == null
            || tradeRequest.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid trade request");
        }

        // 2. Validate crypto pair
        Long cryptoPairId = cryptoPairMapper.findIdByPairName(tradeRequest.getPairName());
        if (cryptoPairId == null) {
            throw new IllegalArgumentException("Unsupported pair: " + tradeRequest.getPairName());
        }
        CryptoPair pair = cryptoPairMapper.findById(cryptoPairId);
        
        // 3. Get latest price
        CryptoPrice price = cryptoPriceMapper.findLatestByPairName(tradeRequest.getPairName());
        if (price == null) {
            throw new IllegalStateException("No price available for " + tradeRequest.getPairName());
        }

        BigDecimal executionPrice;
        Long spendSymbolId;
        Long receiveSymbolId;
        BigDecimal totalAmount;

        if(tradeRequest.getTradeType() == TradeType.BUY) {
            executionPrice = price.getAskPrice();
            spendSymbolId = pair.getQuoteSymbolId();   // USDT (quote)
            receiveSymbolId = pair.getBaseSymbolId();  // BTC or ETH
            totalAmount = tradeRequest.getQuantity().multiply(executionPrice);
        }else{
            executionPrice = price.getBidPrice();
            spendSymbolId = pair.getBaseSymbolId();
            receiveSymbolId = pair.getQuoteSymbolId();
            totalAmount = tradeRequest.getQuantity().multiply(executionPrice);
        }

        // 4. Check balance
        BigDecimal spendAmount = tradeRequest.getTradeType() == TradeType.BUY ? totalAmount : tradeRequest.getQuantity();
        UserWallet spendWallet = userWalletMapper.findByUserIdAndSymbolId(tradeRequest.getUserId(), spendSymbolId);
        if (spendWallet == null || spendWallet.getBalance().compareTo(spendAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 5. Update wallets (spend and receive)
        BigDecimal newSpendBalance = spendWallet.getBalance().subtract(spendAmount);
        userWalletMapper.updateBalance(tradeRequest.getUserId(), spendSymbolId, newSpendBalance);

        // Update or create receive wallet
        BigDecimal receiveAmount = tradeRequest.getTradeType() == TradeType.BUY ? tradeRequest.getQuantity() : totalAmount;
        UserWallet receiveWallet = userWalletMapper.findByUserIdAndSymbolId(tradeRequest.getUserId(), receiveSymbolId);
        BigDecimal newReceiveBalance = receiveWallet != null 
            ? receiveWallet.getBalance().add(receiveAmount)
            : receiveAmount;
        if (receiveWallet == null) {
            // Create wallet - user first time acquiring this crypto
            UserWallet newWallet = new UserWallet();
            newWallet.setUserId(tradeRequest.getUserId());
            newWallet.setSymbolId(receiveSymbolId);
            newWallet.setBalance(receiveAmount);
            userWalletMapper.insert(newWallet);
        } else {
            userWalletMapper.updateBalance(tradeRequest.getUserId(), receiveSymbolId, newReceiveBalance);
        }

        // 6. Insert trade record
        Trade trade = new Trade();
        trade.setUserId(tradeRequest.getUserId());
        trade.setCryptoPairId(cryptoPairId);
        trade.setTradeType(tradeRequest.getTradeType().name());
        trade.setQuantity(tradeRequest.getQuantity());
        trade.setPrice(executionPrice);
        trade.setTotalAmount(totalAmount);
        trade.setTradeTime(LocalDateTime.now());
        tradeMapper.insert(trade);

        // 7. Build response
        TradeResponse response = new TradeResponse();
        response.setTradeId(trade.getId());
        response.setPairName(tradeRequest.getPairName());
        response.setTradeType(tradeRequest.getTradeType().name());
        response.setQuantity(tradeRequest.getQuantity());
        response.setPrice(executionPrice);
        response.setTotalAmount(totalAmount);
        response.setTradeTime(trade.getTradeTime());
        response.setStatus("SUCCESS");
        return response;

        
    }
}