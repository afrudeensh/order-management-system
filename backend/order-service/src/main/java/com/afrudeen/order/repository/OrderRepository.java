package com.afrudeen.order.repository;
import com.afrudeen.order.entity.Order;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // JOIN FETCH loads orders AND items in ONE query (avoids the N+1 problem)
    @Query("""
           select distinct o from Order o
           left join fetch o.items
           where o.userId = :userId
           order by o.createdAt desc
     """)
    List<Order> findByUserWithItems(@Param("userId") Long userId);
}