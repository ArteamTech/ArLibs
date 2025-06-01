# ArLibs Condition System - Implementation Summary

## Overview

我们已经成功为ArLibs添加了一个完整的条件表达式模块，用于在配置文件中验证玩家是否满足特定条件。

## 已实现的功能

### 1. 核心条件类型

- **Permission Conditions** (`permission`/`perm`): 验证玩家权限
- **Placeholder Conditions** (`placeholder`/`papi`): 验证PlaceholderAPI占位符值
- **Logical Conditions**: 
  - `any` (OR逻辑): 任意条件满足即可
  - `all` (AND逻辑): 所有条件都必须满足

### 2. 算术比较操作符

支持以下比较操作符：
- `>` (大于)
- `>=` (大于等于)
- `<` (小于)
- `<=` (小于等于)
- `==` (等于)
- `!=` (不等于)

### 3. 条件解析器

- 灵活的表达式解析
- 支持嵌套条件
- 宽容的空格处理
- 自动类型推断（数值vs字符串比较）

### 4. 条件管理器

- 自动缓存已解析的条件
- 性能优化
- 批量条件评估
- 条件验证

### 5. 命令行界面

通过 `/arlibs condition` 命令提供：
- `test <expression>` - 测试条件表达式
- `validate <expression>` - 验证条件语法
- `debug <expression>` - 调试条件解析
- `cache <clear|size|info>` - 管理条件缓存
- `help` - 显示帮助信息

### 6. 直接条件测试

支持直接测试条件，无需明确指定 "test" 关键字：
```
/arlibs condition permission essentials.fly
/arlibs condition papi %player_level% >= 10
```

## 文件结构

```
src/main/kotlin/com/arteam/arLibs/condition/
├── Condition.kt                    # 基础条件接口
├── ComparisonOperator.kt           # 比较操作符枚举
├── ConditionManager.kt             # 条件管理器
├── ConditionParser.kt              # 条件解析器
├── conditions/
│   ├── AllCondition.kt            # AND逻辑条件
│   ├── AnyCondition.kt            # OR逻辑条件
│   ├── PermissionCondition.kt     # 权限条件
│   └── PlaceholderCondition.kt    # 占位符条件
└── examples/
    └── ConditionTestExamples.kt   # 测试示例
```

## 使用示例

### 基础条件
```yaml
# 权限检查
conditions:
  - "permission essentials.fly"
  - "perm worldedit.selection"

# 占位符检查
conditions:
  - "papi %player_level% >= 10"
  - "placeholder %player_world% == survival"
  - "%player_health% > 5"
```

### 逻辑条件
```yaml
# OR逻辑 - 任意条件满足
conditions:
  - "any [permission group.vip; papi %player_level% >= 50]"

# AND逻辑 - 所有条件必须满足
conditions:
  - "all [permission worldedit.use; papi %player_world% == creative]"

# 嵌套条件
conditions:
  - "all [permission group.premium; any [papi %player_level% >= 50; permission essentials.god]]"
```

### 宽容的格式支持
```yaml
# 这些格式都是有效的：
conditions:
  - "all [papi %player_level% >= 10; %player_sneaking%]"
  - "all[papi %player_level% >= 10;%player_sneaking%]"
  - "all [ papi %player_level% >= 10 ; %player_sneaking% ]"
```

## 命令使用示例

```bash
# 测试条件
/arlibs condition test "permission essentials.fly"
/arlibs condition permission essentials.fly

# 验证语法
/arlibs condition validate "all [papi %player_level% >= 10; %player_sneaking%]"

# 调试解析
/arlibs condition debug "any [permission group.vip; %player_level% >= 50]"

# 缓存管理
/arlibs condition cache size
/arlibs condition cache clear
/arlibs condition cache info

# 获取帮助
/arlibs condition help
```

## 性能特性

- **缓存机制**: 已解析的条件会被缓存，避免重复解析
- **并发安全**: 使用ConcurrentHashMap确保线程安全
- **懒加载**: 条件只在需要时才被解析
- **批量操作**: 支持批量条件评估

## 错误处理

- 语法错误会被优雅地处理，不会导致崩溃
- 详细的错误信息和调试支持
- 缺失PlaceholderAPI时的回退处理
- 完整的日志记录

## 扩展性

系统设计为可扩展的：
- 新的条件类型可以通过实现`Condition`接口轻松添加
- 新的比较操作符可以添加到`ComparisonOperator`枚举
- 解析器支持新的表达式格式

## 已修复的问题

1. **空格处理**: 现在支持`any`/`all`与`[]`之间的可选空格
2. **子条件解析**: 修复了复杂嵌套条件的解析问题
3. **占位符支持**: 支持不带前缀的占位符条件（如`%player_level%`）
4. **错误处理**: 改进了错误消息和调试信息
5. **Tab补全**: 添加了完整的命令补全支持

## 下一步

条件系统现在已经完全可用，可以：
1. 在其他插件中使用条件验证
2. 在配置文件中定义复杂的条件逻辑
3. 通过命令行测试和调试条件
4. 根据需要扩展新的条件类型 