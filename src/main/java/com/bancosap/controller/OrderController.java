package com.bancosap.controller;

import com.bancosap.dto.request.SimulatedBuyRequest;
import com.bancosap.dto.request.SimulatedConvertRequest;
import com.bancosap.dto.request.SimulatedSellRequest;
import com.bancosap.dto.response.SimulatedOrderResponse;
import com.bancosap.entity.SimulatedOrder;
import com.bancosap.repository.SimulatedOrderRepository;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Ordens Simuladas de Criptoativos", description = "Endpoints para compra, venda e conversão direta com cotações reais")
public class OrderController {

    private final OrderService orderService;
    private final SimulatedOrderRepository orderRepository;

    public OrderController(OrderService orderService, SimulatedOrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/buy")
    @Operation(summary = "Executar compra simulada de criptoativo com saldo em Reais")
    public ResponseEntity<SimulatedOrderResponse> buy(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SimulatedBuyRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(orderService.executeBuy(currentUser.getId(), request, ip));
    }

    @PostMapping("/sell")
    @Operation(summary = "Executar venda simulada de criptoativo recebendo Reais")
    public ResponseEntity<SimulatedOrderResponse> sell(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SimulatedSellRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(orderService.executeSell(currentUser.getId(), request, ip));
    }

    @PostMapping("/convert")
    @Operation(summary = "Executar conversão direta entre duas criptomoedas (ex: BTC para ETH)")
    public ResponseEntity<SimulatedOrderResponse> convert(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SimulatedConvertRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(orderService.executeConvert(currentUser.getId(), request, ip));
    }

    @GetMapping
    @Operation(summary = "Histórico paginado de ordens simuladas do usuário")
    public ResponseEntity<Page<SimulatedOrderResponse>> getOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {
        Page<SimulatedOrder> page = orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return ResponseEntity.ok(page.map(o -> new SimulatedOrderResponse(
                o.getAuthenticationCode(),
                o.getOrderType(),
                o.getSymbolFrom(),
                o.getSymbolTo(),
                o.getAmountFrom(),
                o.getAmountTo(),
                o.getUnitPriceBrl(),
                o.getFeeBrl(),
                o.getStatus(),
                o.getCreatedAt()
        )));
    }
}
