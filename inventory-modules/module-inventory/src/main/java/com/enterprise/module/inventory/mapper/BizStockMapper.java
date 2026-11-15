package com.enterprise.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.inventory.entity.BizStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Mapper
public interface BizStockMapper extends BaseMapper<BizStock> {

    /**
     * 库存增减的【唯一原子入口】。
     * 单条 UPDATE 由 MySQL 行锁保证并发安全，available_quantity + delta >= 0 条件保证绝不超卖。
     * 返回 0 表示库存不足或记录不存在，调用方据此抛业务异常并回滚整个事务。
     */
    @Update("UPDATE biz_stock SET quantity = quantity + #{delta}, " +
            "available_quantity = available_quantity + #{delta}, " +
            "version = version + 1, update_time = NOW() " +
            "WHERE product_id = #{productId} AND warehouse_id = #{warehouseId} AND deleted = 0 " +
            "AND available_quantity + #{delta} >= 0 AND quantity + #{delta} >= 0")
    int changeQuantity(@Param("productId") Long productId,
                       @Param("warehouseId") Long warehouseId,
                       @Param("delta") long delta);

    /** 锁定/解锁：只调整锁定量与可用量，不改变实物总量 */
    @Update("UPDATE biz_stock SET locked_quantity = locked_quantity + #{delta}, " +
            "available_quantity = available_quantity - #{delta}, " +
            "version = version + 1, update_time = NOW() " +
            "WHERE product_id = #{productId} AND warehouse_id = #{warehouseId} AND deleted = 0 " +
            "AND locked_quantity + #{delta} >= 0 AND available_quantity - #{delta} >= 0")
    int changeLocked(@Param("productId") Long productId,
                     @Param("warehouseId") Long warehouseId,
                     @Param("delta") long delta);

    /** 初始化库存行（唯一键冲突时静默忽略，避免并发插入报错） */
    @Insert("INSERT IGNORE INTO biz_stock " +
            "(id, product_id, warehouse_id, quantity, locked_quantity, available_quantity, " +
            " create_by, create_dept, create_time, update_time, deleted, version) " +
            "VALUES (#{id}, #{productId}, #{warehouseId}, 0, 0, 0, " +
            " #{createBy}, #{createDept}, NOW(), NOW(), 0, 0)")
    int insertIgnore(BizStock stock);

    @Select("SELECT * FROM biz_stock WHERE product_id = #{productId} " +
            "AND warehouse_id = #{warehouseId} AND deleted = 0 LIMIT 1 FOR UPDATE")
    BizStock selectForUpdate(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Select("SELECT COALESCE(SUM(quantity),0) FROM biz_stock WHERE deleted = 0")
    long totalQuantity();

    @Select("SELECT COUNT(*) FROM biz_stock s JOIN biz_product p ON p.id = s.product_id " +
            "WHERE s.deleted = 0 AND p.deleted = 0 AND s.available_quantity <= p.safety_stock")
    int countLowStock();

    /** 库存台账分页（JOIN 商品/仓库，支持低库存过滤） */
    @Select("<script>" +
            "SELECT s.id, s.product_id, s.warehouse_id, s.quantity, s.locked_quantity, s.available_quantity, " +
            "s.create_time, s.update_time, p.product_code, p.product_name, p.unit, COALESCE(p.safety_stock, 0) AS safety_stock, " +
            "w.warehouse_name FROM biz_stock s " +
            "LEFT JOIN biz_product p ON p.id = s.product_id AND p.deleted = 0 " +
            "LEFT JOIN biz_warehouse w ON w.id = s.warehouse_id AND w.deleted = 0 " +
            "WHERE s.deleted = 0 " +
            "<if test='q.productId != null'> AND s.product_id = #{q.productId} </if>" +
            "<if test='q.warehouseId != null'> AND s.warehouse_id = #{q.warehouseId} </if>" +
            "<if test='q.keyword != null and q.keyword.length() > 0'> AND (p.product_name LIKE CONCAT('%', #{q.keyword}, '%') OR p.product_code LIKE CONCAT('%', #{q.keyword}, '%')) </if>" +
            "<if test='q.lowStockOnly != null and q.lowStockOnly'> AND s.available_quantity &lt;= p.safety_stock </if>" +
            "ORDER BY s.update_time DESC</script>")
    IPage<com.enterprise.module.inventory.vo.StockVO> selectStockPage(Page<?> page, @Param("q") com.enterprise.module.inventory.dto.StockQuery q);


}
