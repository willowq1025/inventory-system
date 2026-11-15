package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysNoticeRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import java.util.List;

@Mapper
public interface SysNoticeReadMapper extends BaseMapper<SysNoticeRead> {

    @Select("SELECT notice_id FROM sys_notice_read WHERE user_id = #{userId} AND read_flag = 1")
    List<Long> selectReadIds(@Param("userId") Long userId);

    /** 已读标记（全体公告首次阅读时自动补记录） */
    @Insert("INSERT INTO sys_notice_read (id, notice_id, user_id, read_flag, read_time) " +
            "VALUES (#{id}, #{noticeId}, #{userId}, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE read_flag = 1, read_time = NOW()")
    int markRead(@Param("id") Long id, @Param("noticeId") Long noticeId, @Param("userId") Long userId);

}
