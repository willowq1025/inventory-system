package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * biz_idempotent_record（biz_idempotent_record）
 */
@Data
@TableName("biz_idempotent_record")
public class BizIdempotentRecord {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 幂等键 */
    @TableField("idempotency_key")
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    /** 模块 */
    @TableField("module")
    @NotBlank(message = "模块不能为空")
    private String module;

    /** 请求摘要 */
    @TableField("request_hash")
    private String requestHash;

    /** 响应快照 */
    @TableField("response_snapshot")
    private String responseSnapshot;

    /** 0处理中 1成功 2失败 */
    @TableField("status")
    private Integer status;

    /** 过期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
