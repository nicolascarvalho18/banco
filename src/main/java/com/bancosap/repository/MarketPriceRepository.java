package com.bancosap.repository;

import com.bancosap.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
    Optional<MarketPrice> findBySymbolIgnoreCase(String symbol);
    List<MarketPrice> findAllByOrderByMarketCapBrlDesc();
    List<MarketPrice> findByCategoryOrderByMarketCapBrlDesc(String category);
}
