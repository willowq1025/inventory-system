package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    @Select("SELECT * FROM sys_notice WHERE deleted = 0 ORDER BY create_time DESC")
    List<SysNotice> selectAllNotice();

    /** 我的消息：全体公告 + 明确投递给该用户的站内信 */
    @Select("SELECT n.* FROM sys_notice n WHERE n.deleted = 0 " +
            "AND (n.target_type = 1 OR EXISTS (SELECT 1 FROM sys_notice_read r " +
            "WHERE r.notice_id = n.id AND r.user_id = #{userId})) " +
            "ORDER BY n.create_time DESC")
    List<SysNotice> selectMine(@Param("userId") Long userId);

}
