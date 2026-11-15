package com.enterprise.module.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.report.entity.BizDailyStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

@Mapper
public interface BizDailyStatMapper extends BaseMapper<BizDailyStat> {

    @Select("SELECT * FROM biz_daily_stat WHERE stat_date = #{statDate} LIMIT 1")
    BizDailyStat selectByDate(@Param("statDate") LocalDate statDate);

}
