package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.OrderRequest;
import com.furniture.furniture_backend.dto.OrderResponse;
import com.furniture.furniture_backend.entity.Order;
import com.furniture.furniture_backend.entity.OrderItem;
import com.furniture.furniture_backend.entity.Product;
import com.furniture.furniture_backend.entity.User;
import com.furniture.furniture_backend.repository.OrderRepository;
import com.furniture.furniture_backend.repository.ProductRepository;
import com.furniture.furniture_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.furniture_backend.dto.PageResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // ========== PHƯƠNG THỨC CƠ BẢN ==========

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }

    public PageResponse<OrderResponse> getAllOrdersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return convertToPageResponse(orderPage);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return OrderResponse.fromOrder(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
        return OrderResponse.fromOrder(order);
    }

    public List<OrderResponse> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }

    // ========== TẠO VÀ CẬP NHẬT ĐỌN HÀNG ==========

    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShippingAddress(request.getShippingAddress());
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setNote(request.getNote());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(
                            () -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

            if (!product.getIsAvailable()) {
                throw new RuntimeException("Product is not available: " + product.getName());
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal price = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(price);
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);

            // Cập nhật stock
            productService.updateStock(product.getId(), itemRequest.getQuantity());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);

        if (status == Order.OrderStatus.DELIVERED) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        }

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long id, Order.PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setPaymentStatus(paymentStatus);
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        // Hoàn lại stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            product.setIsAvailable(true);
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // ========== HELPER ==========

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PageResponse<OrderResponse> convertToPageResponse(Page<Order> orderPage) {
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast());
    }
}