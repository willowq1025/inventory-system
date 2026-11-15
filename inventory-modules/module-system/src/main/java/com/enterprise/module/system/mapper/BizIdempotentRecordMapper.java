package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.BizIdempotentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Mapper
public interface BizIdempotentRecordMapper extends BaseMapper<BizIdempotentRecord> {

    @Delete("DELETE FROM biz_idempotent_record WHERE expire_time < #{now}")
    int deleteExpired(@Param("now") LocalDateTime now);

}
