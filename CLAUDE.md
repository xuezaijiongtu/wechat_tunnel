# 后端开发规范

> 本规范基于《阿里巴巴Java开发手册》，结合项目实际情况制定。

## 项目概述

**项目名称**：`{项目名称}`（如：xp-frd-xxx）  
**应用名称**：`{应用名称}`（如：xp-frd-xxx-boot）  
**技术栈**：Java 8 + Spring Boot 2.x + MyBatis Plus 3.5.x + MySQL 8.0 + Redis + XXL-Job  
**根包路径**：`com.xiaopeng.frd.{模块名}`  
**父项目**：xp-frd-parent

## 模块结构

```
{项目名称}/
├── {项目名称}-boot/     # 启动模块：Controller、Task（XXL-Job）、Config
├── {项目名称}-service/  # 业务模块：Service、Entity、Mapper、DTO、VO、Enums、Client
└── sql/                 # 数据库脚本
```

### 模块职责

- **{项目名称}-boot**：REST 接口层、XXL-Job 定时任务、Spring Boot 启动配置
- **{项目名称}-service**：业务逻辑、数据访问、外部客户端调用、数据对象定义

## 包结构规范

### boot 模块包结构

```
com.xiaopeng.frd.{模块名}/
├── {应用名}Application.java       # 启动类
├── controller/                    # REST 接口层
│   ├── XxxController.java
│   └── ...
└── task/                          # XXL-Job 定时任务
    └── XxxJob.java
```

### service 模块包结构

```
com.xiaopeng.frd.{模块名}/
├── service/        # 业务服务层（继承 ServiceImpl 或独立 Service）
├── entity/         # 数据库实体（DO，与数据库表一一对应）
├── mapper/         # MyBatis Plus Mapper 接口ovr
├── dto/            # 数据传输对象（入参）
├── vo/             # 视图对象（出参）
├── enums/          # 枚举定义
├── client/         # 外部 HTTP 客户端
│   └── dto/        # 客户端专用 DTO
└── common/         # 公共类（PageResult 等）
```

## 一、命名规范

### 1.1 类命名规范

> 【强制】类名使用 UpperCamelCase 风格，但以下情形例外：DO/BO/DTO/VO/AO/PO 等。

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `{业务名}Controller` | `UserController` |
| Service | `{业务名}Service` | `UserService` |
| Entity（DO） | `{业务名}Entity` | `UserEntity` |
| Mapper | `{业务名}Mapper` | `UserMapper` |
| DTO（入参） | `{业务名}{操作}DTO` | `UserQueryDTO`、`UserAddDTO` |
| VO（出参） | `{业务名}VO` | `UserVO`、`OrderVO` |
| 枚举 | `{业务名}{含义}Enum` | `UserStatusEnum`、`OrderTypeEnum` |
| 定时任务 | `{业务名}Job` | `DataSyncJob` |
| 客户端 | `{服务名}Client` | `ThirdPartyClient` |
| 配置类 | `{用途}Config` | `MybatisPlusConfig` |
| 常量类 | `{业务名}Constants` | `PaymentConstants` |
| 工具类 | `{功能}Utils` | `DateUtils`、`StringUtils` |
| 异常类 | `{业务名}Exception` | `BusinessException` |

### 1.2 方法命名规范

> 【强制】方法名、参数名、成员变量、局部变量都统一使用 lowerCamelCase 风格。

| 操作 | 前缀 | 示例 |
|------|------|------|
| 新增 | `add` / `create` / `save` | `addUser`、`createOrder` |
| 编辑 | `update` / `modify` | `updateUser` |
| 删除 | `delete` / `remove` | `deleteUser` |
| 查询单个 | `get` / `query` / `find` | `getUserById`、`findByName` |
| 查询列表 | `list` / `query` | `listUsers`、`queryOrders` |
| 分页查询 | `page` | `pageUsers` |
| 检索 | `search` | `searchUsers` |
| 统计 | `count` | `countByStatus` |
| 判断 | `is` / `has` / `can` | `isValid`、`hasPermission` |
| 批量操作 | `batch{操作}` | `batchDelete`、`batchSave` |

### 1.3 常量命名规范

> 【强制】常量命名全部大写，单词间用下划线隔开，力求语义表达完整清楚。

```java
// 正例
public class OrderConstants {
    /** 订单状态：待支付 */
    public static final int STATUS_PENDING = 0;
    /** 订单状态：已支付 */
    public static final int STATUS_PAID = 1;
    /** 默认超时时间（毫秒） */
    public static final long DEFAULT_TIMEOUT = 30000L;
}

// 反例
public static final int status = 0;  // 常量应全大写
public static final long timeout = 30000l;  // 小写l容易与1混淆，应使用大写L
```

### 1.4 接口路径规范

- 统一前缀：`/web/{业务模块}`
- 使用小驼峰命名：`/web/user`、`/web/orderInfo`
- 操作路径：`/add`、`/edit`、`/delete/{id}`、`/detail/{id}`、`/list`、`/page`

```
/web/{模块}/add
/web/{模块}/edit
/web/{模块}/delete/{id}
/web/{模块}/detail/{id}
/web/{模块}/list
/web/{模块}/page
```

## 二、代码格式规范

### 2.1 基本格式

> 【强制】采用 4 个空格缩进，禁止使用 tab 字符。

- 单行字符数限制不超过 120 个，超出需要换行
- if/for/while/switch/do 等保留字与括号之间都必须加空格
- 任何二目、三目运算符的左右两边都需要加一个空格
- 方法参数在定义和传入时，多个参数逗号后边必须加空格

```java
// 正例
public void process(String param1, String param2) {
    if (param1 != null && param2 != null) {
        int result = param1.length() + param2.length();
    }
}

// 反例
public void process(String param1,String param2){
    if(param1!=null&&param2!=null){
        int result=param1.length()+param2.length();
    }
}
```

## 三、Controller 层规范

```java
/**
 * {业务名}管理接口
 *
 * @author {作者}
 * @date {日期}
 */
@Slf4j
@RestController
@RequestMapping("/web/{业务模块}")
public class XxxController {

    private final XxxService xxxService;

    /**
     * 构造器注入（推荐）
     */
    public XxxController(XxxService xxxService) {
        this.xxxService = xxxService;
    }

    /**
     * 新增{业务名}
     *
     * @param dto 新增参数
     * @return 新增记录ID
     */
    @PostMapping("/add")
    public ResponseWrapper<Long> add(@RequestBody @Validated XxxAddDTO dto) {
        try {
            Long id = xxxService.add(dto);
            return ResponseWrapper.success(id);
        } catch (Exception e) {
            log.error("新增{业务名}失败, param={}", dto, e);
            return ResponseWrapper.fail("500", "新增失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询{业务名}列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ResponseWrapper<PageResult<XxxVO>> page(@RequestBody XxxQueryDTO query) {
        PageResult<XxxVO> result = xxxService.page(query);
        return ResponseWrapper.success(result);
    }

    /**
     * 查询{业务名}详情
     *
     * @param id 主键ID
     * @return 详情信息
     */
    @GetMapping("/detail/{id}")
    public ResponseWrapper<XxxVO> detail(@PathVariable Long id) {
        XxxVO vo = xxxService.getDetail(id);
        if (vo == null) {
            return ResponseWrapper.fail("500", "数据不存在");
        }
        return ResponseWrapper.success(vo);
    }
}
```

**Controller 层规则：**
- 【强制】统一返回 `ResponseWrapper<T>`（来自 `com.xiaopeng.frd.core.api.ResponseWrapper`）
- 【强制】成功用 `ResponseWrapper.success(data)` 或 `ResponseWrapper.success()`
- 【强制】失败用 `ResponseWrapper.fail("code", msg)`，code 为字符串类型
- 【强制】增删改操作需 try-catch，记录 error 日志后返回失败响应
- 【强制】不写业务逻辑，只做参数接收和 Service 调用
- 【强制】类和方法必须使用 Javadoc 注释，包含 @author 和 @date


## 四、Service 层规范

```java
/**
 * {业务名}服务
 *
 * @author {作者}
 * @date {日期}
 */
@Slf4j
@Service
public class XxxService extends ServiceImpl<XxxMapper, XxxEntity> {

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 新增{业务名}
     *
     * @param dto 新增参数
     * @return 新增记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long add(XxxAddDTO dto) {
        XxxEntity entity = convertToEntity(dto);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        save(entity);
        return entity.getId();
    }

    /**
     * 分页查询
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageResult<XxxVO> page(XxxQueryDTO query) {
        // 分页参数默认值处理
        int pageNum = query.getPage() != null ? query.getPage() : 1;
        int pageSize = query.getSize() != null ? query.getSize() : 10;
        XxxQueryDTO.Param param = query.getParam();

        // 构建查询条件
        LambdaQueryWrapper<XxxEntity> wrapper = new LambdaQueryWrapper<>();
        if (param != null) {
            if (StringUtils.hasText(param.getKeyword())) {
                wrapper.like(XxxEntity::getTitle, param.getKeyword());
            }
            if (param.getStatus() != null) {
                wrapper.eq(XxxEntity::getStatus, param.getStatus());
            }
        }
        wrapper.orderByDesc(XxxEntity::getCreateTime);

        // 执行分页查询
        Page<XxxEntity> page = page(new Page<>(pageNum, pageSize), wrapper);
        
        // 转换为VO
        List<XxxVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 查询详情
     *
     * @param id 主键ID
     * @return 详情信息，不存在返回null
     */
    public XxxVO getDetail(Long id) {
        XxxEntity entity = getById(id);
        if (entity == null) {
            return null;
        }
        return convertToVO(entity);
    }

    // ==================== 私有方法 ====================

    /**
     * DTO转Entity
     */
    private XxxEntity convertToEntity(XxxAddDTO dto) {
        XxxEntity entity = new XxxEntity();
        entity.setTitle(dto.getTitle());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    /**
     * Entity转VO
     */
    private XxxVO convertToVO(XxxEntity entity) {
        XxxVO vo = new XxxVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setStatus(entity.getStatus());
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(DATE_FORMATTER));
        }
        return vo;
    }
}
```

**Service 层规则：**
- 【强制】继承 `ServiceImpl<Mapper, Entity>` 获得 MyBatis Plus 基础 CRUD 能力
- 【强制】写操作必须加 `@Transactional(rollbackFor = Exception.class)`
- 【强制】使用 `LambdaQueryWrapper` 构建查询条件，避免硬编码字段名
- 【强制】分页查询统一返回 `PageResult<VO>`
- 【强制】时间格式化使用 `DateTimeFormatter`，禁止使用 `SimpleDateFormat`（线程不安全）
- 【推荐】Entity 与 VO 的转换逻辑封装为私有方法

## 五、Entity（DO）规范

> 【强制】DO（Data Object）与数据库表结构一一对应，通过 DAO 层向上传输数据源对象。

```java
/**
 * {业务名}实体
 *
 * @author {作者}
 * @date {日期}
 */
@Data
@TableName("t_{表名}")
public class XxxEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 是否删除: 0-未删除, 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建人ID */
    private String createUserId;

    /** 创建人姓名 */
    private String createUserName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人ID */
    private String updateUserId;

    /** 更新人姓名 */
    private String updateUserName;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
```

**Entity 规则：**
- 【强制】使用 `@Data`（Lombok）
- 【强制】表名必须以 `t_` 开头，如 `@TableName("t_user")`
- 【强制】主键使用 `@TableId(type = IdType.AUTO)`，类型为 `Long`
- 【强制】逻辑删除字段命名为 `deleted`（不要用 `isDeleted`），加 `@TableLogic` 注解
- 【强制】POJO 类中布尔类型变量不要加 is 前缀，数据库字段必须加 is_
- 【强制】必备字段：`id`、`createTime`、`updateTime`、`deleted`
- 【强制】时间字段使用 `LocalDateTime`
- 【强制】字段注释使用 Javadoc `/** 注释 */` 格式
- 【强制】状态字段在注释中说明每个值的含义

## 六、Mapper 规范

> 【强制】Mapper 接口上禁止直接编写 SQL，所有自定义 SQL 必须写在对应的 XML 文件中。

### 6.1 Mapper 接口

```java
/**
 * {业务名}Mapper接口
 *
 * @author {作者}
 * @date {日期}
 */
@Mapper
public interface XxxMapper extends BaseMapper<XxxEntity> {

    /**
     * 根据条件查询列表
     *
     * @param param 查询参数
     * @return 结果列表
     */
    List<XxxEntity> selectByCondition(@Param("param") XxxQueryParam param);

    /**
     * 批量更新状态
     *
     * @param ids 主键ID列表
     * @param status 状态值
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
}
```

### 6.2 Mapper XML 文件

XML 文件路径：`resources/mapper/XxxMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.xiaopeng.frd.{模块名}.mapper.XxxMapper">

    <!-- 通用查询映射结果 -->
    <resultMap id="BaseResultMap" type="com.xiaopeng.frd.{模块名}.entity.XxxEntity">
        <id column="id" property="id"/>
        <result column="title" property="title"/>
        <result column="status" property="status"/>
        <result column="is_deleted" property="deleted"/>
        <result column="create_user_id" property="createUserId"/>
        <result column="create_user_name" property="createUserName"/>
        <result column="create_time" property="createTime"/>
        <result column="update_user_id" property="updateUserId"/>
        <result column="update_user_name" property="updateUserName"/>
        <result column="update_time" property="updateTime"/>
    </resultMap>

    <!-- 通用查询列 -->
    <sql id="Base_Column_List">
        id, title, status, is_deleted, create_user_id, create_user_name, 
        create_time, update_user_id, update_user_name, update_time
    </sql>

    <!-- 根据条件查询列表 -->
    <select id="selectByCondition" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM t_xxx
        WHERE is_deleted = 0
        <if test="param.keyword != null and param.keyword != ''">
            AND title LIKE CONCAT('%', #{param.keyword}, '%')
        </if>
        <if test="param.status != null">
            AND status = #{param.status}
        </if>
        ORDER BY create_time DESC
    </select>

    <!-- 批量更新状态 -->
    <update id="batchUpdateStatus">
        UPDATE t_xxx
        SET status = #{status}, update_time = NOW()
        WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </update>

</mapper>
```

**Mapper 规则：**
- 【强制】Mapper 接口上禁止使用 `@Select`、`@Insert`、`@Update`、`@Delete` 等注解编写 SQL
- 【强制】所有自定义 SQL 必须写在对应的 `XxxMapper.xml` 文件中
- 【强制】XML 文件放在 `resources/mapper/` 目录下，文件名与 Mapper 接口名一致
- 【强制】使用 `#{}` 进行参数绑定，禁止使用 `${}` 防止 SQL 注入
- 【强制】复杂查询必须定义 `resultMap`，不要使用 `resultType`
- 【强制】定义 `Base_Column_List` SQL 片段，避免使用 `SELECT *`
- 【强制】多参数方法必须使用 `@Param` 注解标注参数名
- 【推荐】简单的单表 CRUD 操作使用 MyBatis Plus 提供的方法，复杂查询使用 XML

## 七、DTO 规范

> 【强制】DTO（Data Transfer Object）用于 Service 层接收外部传入的数据。

```java
/**
 * {业务名}查询参数
 *
 * @author {作者}
 * @date {日期}
 */
@Data
public class XxxQueryDTO {

    /** 页码，默认1 */
    private Integer page;

    /** 每页大小，默认10 */
    private Integer size;

    /** 查询参数 */
    private Param param;

    /**
     * 查询条件
     */
    @Data
    public static class Param {
        /** 关键词 */
        private String keyword;
        /** 状态 */
        private Integer status;
        /** 时间范围 */
        private String[] timeRange;
    }
}
```

**DTO 规则：**
- 【强制】分页查询 DTO 包含 `page`、`size` 字段，默认值在 Service 层处理
- 【推荐】复杂查询条件封装为内部静态类 `Param`
- 【强制】字段使用包装类型（`Integer` 而非 `int`）
- 【推荐】日期范围使用 `String[]` 类型

## 八、VO 规范

> 【强制】VO（View Object）用于向前端返回展示数据。

```java
/**
 * {业务名}视图对象
 *
 * @author {作者}
 * @date {日期}
 */
@Data
public class XxxVO {

    /** 主键ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 状态 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 创建时间 */
    private String createTime;

    /** 更新时间 */
    private String updateTime;

    /** 创建人 */
    private String createUserName;
}
```

**VO 规则：**
- 【强制】时间字段使用 `String` 类型，格式为 `yyyy-MM-dd HH:mm:ss`
- 【推荐】状态码字段配套提供中文名称字段（如 `status` + `statusName`）
- 【强制】不暴露敏感字段（密码、内部标识等）

## 九、统一响应格式

本项目统一使用 `com.xiaopeng.frd.core.api.ResponseWrapper` 作为 API 响应封装类。

**ResponseWrapper 常用方法：**
```java
// 成功响应（无数据）
return ResponseWrapper.success();

// 成功响应（带数据）
return ResponseWrapper.success(data);

// 失败响应（code为字符串类型）
return ResponseWrapper.fail("500", "操作失败: " + e.getMessage());
```

## 十、分页结果格式

```java
/**
 * 通用分页结果
 *
 * @author {作者}
 * @date {日期}
 */
@Data
public class PageResult<T> {

    /** 当前页码 */
    private Integer currentPage;

    /** 总页数 */
    private Integer totalPage;

    /** 总条数 */
    private Long total;

    /** 数据列表 */
    private List<T> records;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrentPage(page);
        result.setTotalPage(size > 0 ? (int) Math.ceil((double) total / size) : 0);
        return result;
    }

    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setRecords(Collections.emptyList());
        result.setTotal(0L);
        result.setCurrentPage(1);
        result.setTotalPage(0);
        return result;
    }
}
```


## 十一、数据库规范

### 11.1 表设计规范

**表命名规范：**
- 【强制】表名必须以 `t_` 开头
- 【强制】表名、字段名必须使用小写字母或数字，禁止出现数字开头
- 【强制】表名不使用复数名词
- 【强制】禁用保留字，如 desc、range、match、delayed 等
- 【推荐】表的命名遵循 `t_{业务名}` 或 `t_{模块}_{业务名}`

**必备字段（所有业务表）：**

```sql
`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
`create_user_id` varchar(128) NOT NULL DEFAULT '' COMMENT '创建人id',
`create_user_name` varchar(128) NOT NULL DEFAULT '' COMMENT '创建人名称',
`create_time` datetime(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4) COMMENT '创建时间',
`update_user_id` varchar(128) NOT NULL DEFAULT '' COMMENT '更新人id',
`update_user_name` varchar(128) NOT NULL DEFAULT '' COMMENT '更新人名称',
`update_time` datetime(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4) ON UPDATE CURRENT_TIMESTAMP(4) COMMENT '修改时间',
`is_deleted` bigint(20) NOT NULL DEFAULT '0' COMMENT '是否删除：大于0:是，0:否',
```

**字段规范：**
- 【强制】表达是与否概念的字段，必须使用 `is_xxx` 的方式命名，数据类型是 `unsigned tinyint`
- 【强制】小数类型为 `decimal`，禁止使用 `float` 和 `double`
- 【强制】varchar 是可变长字符串，长度不要超过 5000
- 【强制】如果存储的字符串长度几乎相等，使用 `char` 定长字符串类型
- 【强制】字段必须非空且有默认值 NOT NULL DEFAULT ''

**索引规范：**
- 【强制】主键索引名为 `pk_{字段名}`
- 【强制】唯一索引名为 `uk_{表名}_{字段名}`
- 【强制】普通索引名为 `idx_{表名}_{字段名}`
- 【强制】业务上具有唯一特性的字段，必须建成唯一索引
- 【推荐】建组合索引时，区分度最高的在最左边

**建表示例：**

```sql
CREATE TABLE `t_vehicle_stop_inspection` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `batch_no` varchar(64) NOT NULL DEFAULT '' COMMENT '批次号，格式：BATCH_YYYYMMDD_HHMMSS',
  `batch_start_time` datetime(4) NOT NULL DEFAULT '1970-01-01 00:00:00.0000' COMMENT '批次开始时间（数据时间范围）',
  `batch_end_time` datetime(4) NOT NULL DEFAULT '1970-01-01 00:00:00.0000' COMMENT '批次结束时间（数据时间范围）',
  `vin` varchar(64) NOT NULL DEFAULT '' COMMENT '车架号(plate)',
  `inspection_status` varchar(32) NOT NULL DEFAULT '10' COMMENT '质检状态：10-待质检，20-质检中，30-质检完成，40-质检失败',
  `result_status` varchar(32) NOT NULL DEFAULT '' COMMENT '业务结论：0-未知，10-Normal，20-Abnormal',
  `cost_ms` bigint(20) NOT NULL DEFAULT '0' COMMENT 'API调用耗时(毫秒)',
  `abnormal_rank` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '异常等级（0-10分）',
  `abnormal_detail` text COMMENT '异常详情（JSON格式或文本）',
  `usage_description` text COMMENT '用车描述',
  `usage_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '车辆使用率',
  `review_conclusion` tinyint(4) NOT NULL DEFAULT '0' COMMENT '复核结论：0-未复核，5-存疑，10-正常，20-异常',
  `review_remark` varchar(512) NOT NULL DEFAULT '' COMMENT '复核原因备注',
  `error_message` text COMMENT '错误信息（API调用失败时记录）',
  `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `create_user_id` varchar(128) NOT NULL DEFAULT '' COMMENT '创建人id',
  `create_user_name` varchar(128) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `create_time` datetime(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4) COMMENT '创建时间',
  `update_user_id` varchar(128) NOT NULL DEFAULT '' COMMENT '更新人id',
  `update_user_name` varchar(128) NOT NULL DEFAULT '' COMMENT '更新人名称',
  `update_time` datetime(4) NOT NULL DEFAULT CURRENT_TIMESTAMP(4) ON UPDATE CURRENT_TIMESTAMP(4) COMMENT '修改时间',
  `is_deleted` bigint(20) NOT NULL DEFAULT '0' COMMENT '是否删除：大于0:是，0:否',
  PRIMARY KEY (`id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_vin` (`vin`),
  KEY `idx_inspection_status` (`inspection_status`),
  KEY `idx_abnormal_rank` (`abnormal_rank`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status_retry` (`inspection_status`, `retry_count`) COMMENT '质检任务查询优化',
  KEY `idx_batch_status` (`batch_no`, `inspection_status`) COMMENT '批次统计查询优化'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车辆违停质检表';
```

### 11.2 SQL 规范

- 【强制】在表查询中，一律不要使用 `*` 作为查询的字段列表，需要哪些字段必须明确写明
- 【强制】sql.xml 配置参数使用 `#{}`，禁止使用 `${}` 防止 SQL 注入
- 【强制】不得使用外键与级联，一切外键概念必须在应用层解决
- 【强制】禁止使用存储过程
- 【强制】数据订正时，要先 select，避免出现误删除
- 【强制】in 操作控制在 1000 个之内
- 【强制】更新数据表记录时，必须同时更新 `update_time` 字段值

### 11.3 MyBatis Plus 配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true   # 下划线转驼峰
  global-config:
    db-config:
      logic-delete-field: deleted        # 逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto                      # 自增主键
```

## 十二、定时任务规范（XXL-Job）

```java
/**
 * {业务名}定时任务
 *
 * @author {作者}
 * @date {日期}
 */
@Slf4j
@Component
public class XxxJob {

    private final JdbcTemplate jdbcTemplate;
    private final XxxService xxxService;

    @Value("${task.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public XxxJob(JdbcTemplate jdbcTemplate, XxxService xxxService) {
        this.jdbcTemplate = jdbcTemplate;
        this.xxxService = xxxService;
    }

    /**
     * 定时任务执行入口
     */
    @XxlJob("xxxTaskHandler")
    public void execute() {
        if (!schedulerEnabled) {
            log.info("任务调度已禁用，跳过执行");
            return;
        }
        
        log.info("开始执行定时任务...");
        long startTime = System.currentTimeMillis();
        
        try {
            // 任务逻辑
            doExecute();
            log.info("定时任务执行完成, 耗时={}ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }

    private void doExecute() {
        // 具体业务逻辑
    }
}
```

**Job 规则：**
- 【强制】通过配置项控制任务开关
- 【强制】使用 `@XxlJob("handlerName")` 注解标记任务方法
- 【强制】任务执行前后记录日志，包含耗时信息
- 【强制】异常必须捕获并记录，避免影响后续调度

### 防并发执行模式

```java
// CAS 式置 running=1，只有从0改成1成功才继续
int affected = jdbcTemplate.update(
    "UPDATE t_xxx SET running = 1 WHERE id = ? AND running = 0", taskId);
if (affected == 0) {
    log.info("任务正在执行中，跳过本次触发, taskId={}", taskId);
    return;
}
try {
    // 执行任务逻辑
    doExecute(taskId);
} finally {
    // 无论成功失败，都重置 running 标识
    jdbcTemplate.update("UPDATE t_xxx SET running = 0 WHERE id = ?", taskId);
}
```

## 十三、枚举规范

> 【强制】如果变量值仅在一个固定范围内变化，用 enum 类型来定义。

```java
/**
 * {业务名}状态枚举
 *
 * @author {作者}
 * @date {日期}
 */
public enum XxxStatusEnum {

    /** 空闲 */
    IDLE(0, "空闲"),
    /** 运行中 */
    RUNNING(1, "运行中"),
    /** 已暂停 */
    PAUSED(2, "已暂停"),
    /** 已完成 */
    COMPLETED(3, "已完成"),
    /** 失败 */
    FAILED(4, "失败");

    private final int code;
    private final String desc;

    XxxStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     */
    public static XxxStatusEnum getByCode(int code) {
        for (XxxStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
```

**枚举规则：**
- 【强制】所有的枚举类型字段必须要有注释，说明每个数据项的用途
- 【推荐】提供 `getByCode` 静态方法用于根据 code 获取枚举

## 十四、日志规范

> 【强制】应用中不可直接使用日志系统（Log4j、Logback）中的 API，而应依赖使用日志框架 SLF4J 中的 API。

```java
// 正例
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j  // 或使用 Lombok 注解
public class XxxService {
    
    public void process(String orderId, Long userId) {
        // 使用占位符，不要字符串拼接
        log.info("开始处理订单, orderId={}, userId={}", orderId, userId);
        
        // debug日志需要判断级别
        if (log.isDebugEnabled()) {
            log.debug("详细调试信息: {}", buildDebugInfo());
        }
        
        try {
            doProcess(orderId);
        } catch (Exception e) {
            // 异常日志必须包含现场信息和堆栈
            log.error("订单处理失败, orderId={}, userId={}", orderId, userId, e);
        }
    }
}

// 反例
log.info("开始处理订单: " + orderId);  // 禁止字符串拼接
log.debug(buildDebugInfo());           // 未判断日志级别
log.error("处理失败");                  // 缺少现场信息和堆栈
```

**日志规则：**
- 【强制】使用 `@Slf4j`（Lombok）或 `LoggerFactory.getLogger()`
- 【强制】使用占位符 `{}` 而非字符串拼接
- 【强制】关键业务节点记录 `INFO` 日志，包含业务标识
- 【强制】异常记录 `ERROR` 日志，必须包含异常堆栈（传入 `e` 参数）
- 【强制】对于 trace/debug/info 级别的日志输出，必须进行日志级别的开关判断
- 【强制】生产环境禁止输出 debug 日志
- 【推荐】循环内的重复操作使用 `DEBUG` 级别


## 十五、异常处理规范

> 【强制】Java 类库中定义的可以通过预检查方式规避的 RuntimeException 异常不应该通过 catch 的方式来处理。

```java
// Service 层：返回 null，由 Controller 判断
public XxxVO getDetail(Long id) {
    // 预检查方式规避 NPE
    if (id == null) {
        return null;
    }
    XxxEntity entity = getById(id);
    if (entity == null) {
        return null;
    }
    return convertToVO(entity);
}

// Controller 层：捕获异常并返回统一响应
@PostMapping("/add")
public ResponseWrapper<Long> add(@RequestBody @Validated XxxAddDTO dto) {
    try {
        Long id = xxxService.add(dto);
        return ResponseWrapper.success(id);
    } catch (Exception e) {
        log.error("新增失败, param={}", dto, e);
        return ResponseWrapper.fail("500", "新增失败: " + e.getMessage());
    }
}

@GetMapping("/detail/{id}")
public ResponseWrapper<XxxVO> detail(@PathVariable Long id) {
    XxxVO vo = xxxService.getDetail(id);
    if (vo == null) {
        return ResponseWrapper.fail("500", "数据不存在");
    }
    return ResponseWrapper.success(vo);
}
```

**异常处理规则：**
- 【强制】捕获异常是为了处理它，不要捕获了却什么都不处理而抛弃之
- 【强制】有 try 块放到了事务代码中，catch 异常后，如果需要回滚事务，一定要注意手动回滚事务
- 【强制】finally 块必须对资源对象、流对象进行关闭，有异常也要做 try-catch
- 【强制】不要在 finally 块中使用 return
- 【强制】防止 NPE，是程序员的基本修养
- 【推荐】写操作（增删改）必须 try-catch；查询操作可不 try-catch

## 十六、OOP 规约

- 【强制】避免通过一个类的对象引用访问此类的静态变量或静态方法，直接用类名来访问即可
- 【强制】所有的覆写方法，必须加 `@Override` 注解
- 【强制】Object 的 equals 方法容易抛空指针异常，应使用常量或确定有值的对象来调用 equals
- 【强制】所有整型包装类对象之间值的比较，全部使用 equals 方法比较
- 【强制】构造方法里面禁止加入任何业务逻辑，如果有初始化逻辑，请放在 init 方法中
- 【强制】POJO 类必须写 toString 方法

```java
// 正例
if ("SUCCESS".equals(status)) { }  // 常量在前，避免 NPE

// 整型比较
Integer a = 128;
Integer b = 128;
if (a.equals(b)) { }  // 正确，不要用 ==
```

## 十七、集合处理规范

- 【强制】只要覆写 equals，就必须覆写 hashCode
- 【强制】使用集合转数组的方法，必须使用集合的 `toArray(T[] array)`，传入类型完全一致、长度为 0 的空数组
- 【强制】不要在 foreach 循环里进行元素的 remove/add 操作，remove 元素请使用 Iterator 方式
- 【强制】使用 entrySet 遍历 Map 类集合 KV，而不是 keySet 方式进行遍历

```java
// 正例：集合转数组
List<String> list = new ArrayList<>();
String[] array = list.toArray(new String[0]);

// 正例：遍历 Map
Map<String, Object> map = new HashMap<>();
for (Map.Entry<String, Object> entry : map.entrySet()) {
    String key = entry.getKey();
    Object value = entry.getValue();
}

// 正例：删除元素
Iterator<String> iterator = list.iterator();
while (iterator.hasNext()) {
    String item = iterator.next();
    if (needRemove(item)) {
        iterator.remove();
    }
}

// 反例：foreach 中删除
for (String item : list) {
    if (needRemove(item)) {
        list.remove(item);  // 会抛 ConcurrentModificationException
    }
}
```

## 十八、并发处理规范

- 【强制】创建线程或线程池时请指定有意义的线程名称，方便出错时回溯
- 【强制】线程资源必须通过线程池提供，不允许在应用中自行显式创建线程
- 【强制】线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式
- 【强制】SimpleDateFormat 是线程不安全的类，一般不要定义为 static 变量
- 【强制】必须回收自定义的 ThreadLocal 变量，尤其在线程池场景下

```java
// 正例：创建线程池
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                      // corePoolSize
    10,                     // maximumPoolSize
    60L,                    // keepAliveTime
    TimeUnit.SECONDS,       // unit
    new LinkedBlockingQueue<>(100),  // workQueue
    new ThreadFactoryBuilder().setNameFormat("xxx-pool-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy()
);

// 正例：ThreadLocal 使用
private static final ThreadLocal<DateTimeFormatter> FORMATTER = 
    ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

try {
    String dateStr = FORMATTER.get().format(LocalDateTime.now());
} finally {
    FORMATTER.remove();  // 必须回收
}

// 反例
Executors.newFixedThreadPool(10);  // 不允许使用 Executors 创建
private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // 线程不安全
```

## 十九、配置规范

### application.yml 关键配置项

```yaml
spring:
  application:
    name: {应用名称}

# 外部 API 配置（通过环境变量注入敏感信息）
xxx:
  api-key: ${XXX_API_KEY:}
  endpoint: ${XXX_ENDPOINT:https://api.example.com}
  timeout: 15000

# 任务调度配置
task:
  scheduler:
    scan-interval: 60000
    enabled: true
```

**配置规则：**
- 【强制】敏感信息（API Key、密码）通过环境变量注入，格式：`${ENV_VAR:默认值}`
- 【强制】可调节的参数（超时、间隔）通过配置项暴露，不硬编码
- 【强制】使用 `@Value("${配置项:默认值}")` 注入配置

## 二十、代码质量要求

- 【强制】禁止使用 `System.out.println`，统一使用 `@Slf4j` 日志
- 【强制】不允许任何魔法值直接出现在代码中，使用枚举或常量类定义
- 【强制】分页查询的 `page`、`size` 参数必须有默认值兜底
- 【强制】JSON 字段的解析必须 try-catch，避免格式异常导致接口报错
- 【强制】时间格式化统一使用 `DateTimeFormatter`，禁止使用 `SimpleDateFormat`
- 【强制】批量操作需控制单批数量，避免大事务
- 【强制】单个方法的总行数不超过 80 行
- 【推荐】单个类的总行数不超过 1000 行

## 二十一、注释规范

> 【强制】类、类属性、类方法的注释必须使用 Javadoc 规范，使用 `/** 内容 */` 格式。

```java
/**
 * {业务名}服务
 * <p>
 * 提供{业务名}的增删改查功能
 * </p>
 *
 * @author {作者}
 * @date {日期}
 */
@Service
public class XxxService {

    /**
     * 根据ID查询详情
     *
     * @param id 主键ID
     * @return 详情信息，不存在返回null
     */
    public XxxVO getDetail(Long id) {
        // 参数校验
        if (id == null) {
            return null;
        }
        
        // 查询数据库
        XxxEntity entity = getById(id);
        
        // 转换为VO
        return convertToVO(entity);
    }
}
```

**注释规则：**
- 【强制】所有的类都必须添加创建者（@author）和创建日期（@date）
- 【强制】所有的抽象方法必须要用 Javadoc 注释
- 【强制】方法内部单行注释，在被注释语句上方另起一行，使用 `//` 注释
- 【强制】所有的枚举类型字段必须要有注释，说明每个数据项的用途
- 【强制】代码修改的同时，注释也要进行相应的修改

## 二十二、新增业务模块步骤

1. 在 `service` 模块新增：`Entity`、`Mapper`、`Service`、`DTO`、`VO`、`Enum`
2. 在 `boot` 模块新增：`Controller`
3. 在 `sql/` 目录新增建表 SQL 脚本（表名以 `t_` 开头）
4. 遵循本规范的命名规范和代码风格

## 二十二、Maven 依赖管理

### 父 POM 配置

```xml
<parent>
    <groupId>com.xiaopeng.frd</groupId>
    <artifactId>xp-frd-parent</artifactId>
    <version>{版本号}</version>
</parent>
```

### 内部模块依赖

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.xiaopeng.frd</groupId>
            <artifactId>{项目名称}-service</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 常用依赖版本

| 依赖 | 版本 |
|------|------|
| fastjson | 2.0.31 |
| httpclient | 4.5.13 |
| lombok | 1.18.40 |
| mybatis-plus | 3.5.3.1 |
| mysql-connector | 8.0.33 |
| easyexcel | 3.2.0 |
| jackson | 2.15.2 |

---

> 本规范参考《阿里巴巴Java开发手册》嵩山版，结合项目实际情况制定。


## 前端开发规范（NON-NEGOTIABLE）

所有开发必须使用原生 Ant Design 替代：

- 表格：`ProTable` / `CRUD`（禁止 `Table`）
- 表单：`ProForm` / `GridForm` / `ModalForm` / `DrawerForm`（禁止 `Form`）
- 数据展示：`Descriptions` / `Card`
- `/m` 路由必须使用 `antd-mobile`
- 参考 `src/pages/ledger-manage` 代码组织方式

**CRUD 模块标准结构：**

```
module-name/
├── index.tsx              # 主组件（CRUD）
├── constant.ts            # 常量（ROW_KEY、BASE_URL等）
├── hooks/
│   ├── useSearchFormItems.tsx
│   ├── useTableColumns.tsx
│   └── useButtonsAuth.tsx  # 可选
└── utils.ts               # 可选
```

**代码质量：**

- 禁止 `any`（用 `unknown`）、禁止 `console`、禁止行内样式
- 单文件 ≤ 1000 行，必须通过 ESLint + Prettier
- `memo()` 包裹组件，`useCallback` 包裹事件，`useMemo` 缓存计算
- 表单/列配置抽离到 `useSearchFormItems` / `useTableColumns`
- 路由注册在 `src/routes/[module]/index.ts`，用 `lazy` 代码分割

**关键配置规范：**

1. 删除操作必须加 `popconfirmProps`
2. `columnActions` 用 `hidden` / `disabled` 实现行级权限
3. 表格配置 `rowKey` + `columnResizable: true`
4. 搜索表单 `hasModeSwitch: true`
5. 全局状态字段 `canResetValue: false`



## 合并代码规范

### 合并策略
- **优先使用 `git merge --no-ff`**（保留合并提交历史），除非明确要求 `rebase`
- 合并前必须确保当前分支已同步主分支（`git fetch origin` 且 `git merge origin/main` 无冲突）
- **禁止**在公共分支（如 `main`、`develop`）上直接修改历史（`rebase`、`reset` 等）

### 冲突解决原则
- 解决冲突时，保留双方的代码逻辑，**不要直接选择某一方的版本**
- 若冲突涉及业务逻辑不确定，应中断合并并向用户提问，不要自行猜测
- 解决完冲突后必须运行项目构建和关键测试确保无破坏

### 合并前检查清单
- [ ] 代码已通过所有测试
- [ ] 代码已通过编译打包
- [ ] 无未提交的临时文件或调试代码
- [ ] 提交信息符合 Conventional Commits 规范

### 合并后操作
- 合并完成后，默认不提示用户删除原分支
- 如合并触发了 CI/CD，提醒用户关注流水线状态

### AI 行为约束
- 在执行合并相关命令前，**先向用户展示计划**（例如：“我将执行 `git checkout main && git merge feature/xxx --no-ff`，是否继续？”）
- 遇到冲突时，不要自动选择一方，而是列出冲突文件并询问处理方式
- **禁止**执行 `git push --force` 或 `git push --force-with-lease`，除非用户明确确认且说明原因