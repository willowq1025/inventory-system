-- ============================================================================
--  V3__seed_recent_daily_stats.sql  经营看板趋势图演示数据
--  相对日期 + 固定 ID + INSERT IGNORE（uk_stat_date），可重复执行；
--  不覆盖 daily-stat 定时任务对已有日期的计算结果。
-- ============================================================================
INSERT IGNORE INTO biz_daily_stat
(id, stat_date, purchase_amount, sale_amount, purchase_count, sale_count, stock_total, low_stock_count) VALUES
(900000000000000001, DATE_SUB(CURDATE(), INTERVAL 6 DAY),  8600.00, 12800.00,  6, 12, 1560, 1),
(900000000000000002, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 11200.00, 15600.00,  8, 15, 1580, 1),
(900000000000000003, DATE_SUB(CURDATE(), INTERVAL 4 DAY),  9800.00, 18300.00,  7, 18, 1605, 0),
(900000000000000004, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 14600.00, 21400.00, 11, 21, 1630, 2),
(900000000000000005, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 13400.00, 19800.00, 10, 19, 1648, 1),
(900000000000000006, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 16800.00, 24600.00, 13, 24, 1665, 0),
(900000000000000007, CURDATE(),                            7200.00, 10400.00,  5,  9, 1670, 1);
