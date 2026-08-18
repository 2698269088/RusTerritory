# RusTerritory

一个支持 Folia 的 Minecraft 领地管理插件

## 功能特性

- **领地创建与管理** - 使用选区工具创建三维领地
- **Folia 支持** - 完全兼容 Folia 服务端
- **权限控制** - 精细的领地保护设置
- **JSON 存储** - 按玩家 UUID 存储领地数据
- **热重载** - 支持配置文件热重载
- **多世界支持** - 领地跨世界管理

## 命令

### 玩家命令 `/rus`

| 命令 | 说明 | 权限 |
|-----|------|------|
| `/rus ade` | 进入/退出选区模式 | 需要创建权限 |
| `/rus create <名称>` | 创建领地 | 需要创建权限 |
| `/rus delete <名称>` | 删除领地 | 需要删除权限 |
| `/rus confirm` | 确认删除操作 | - |
| `/rus list` | 列出自己的领地 | - |
| `/rus tp <名称>` | 传送到领地中心 | - |
| `/rus info [名称]` | 查看领地信息 | - |
| `/rus set <名称> <属性> <true/false>` | 设置领地属性 | - |
| `/rus rename <旧名称> <新名称>` | 重命名领地 | - |
| `/rus transfer <名称> <玩家>` | 转让领地 | - |
| `/rus help` | 显示帮助 | - |

### 管理员命令 `/rut`

| 命令 | 说明 |
|-----|------|
| `/rut ade` | 进入/退出选区模式 |
| `/rut create <名称> [玩家]` | 创建领地（可指定拥有者） |
| `/rut delete <名称>` | 删除任意领地 |
| `/rut confirm` | 确认删除操作 |
| `/rut list` | 列出所有领地 |
| `/rut tp <名称>` | 传送到领地中心 |
| `/rut info [名称]` | 查看领地信息 |
| `/rut set <名称> <属性> <true/false>` | 设置领地属性 |
| `/rut rename <旧名称> <新名称>` | 重命名领地 |
| `/rut transfer <名称> <玩家>` | 转让领地 |
| `/rut config <key> <value>` | 修改配置 |
| `/rut reload` | 重载配置和数据 |
| `/rut help` | 显示帮助 |

## 权限节点

| 权限 | 说明 | 默认 |
|-----|------|------|
| `rusterry.admin` | 管理员权限 | OP |
| `rusterry.player` | 玩家权限 | 所有玩家 |

## 配置文件

```yaml
# 保护功能开关
protection:
  pvp: true        # 禁止PVP
  build: true      # 禁止放置方块
  destroy: true    # 禁止破坏方块
  interact: true   # 禁止交互
  enter: true      # 禁止进入

# 领地设置
territory:
  max-size: 256        # 领地最大体积（方块）
  max-per-player: 5    # 玩家最大领地数量

# 选区工具设置
selection:
  tool: WOODEN_SWORD   # 选区工具
  require-sneak: true  # 需要蹲下

# 玩家权限
player:
  can-create: false    # 允许普通玩家创建
  can-delete: false    # 允许普通玩家删除
```

## 领地属性

| 属性 | 说明 | 默认值 |
|-----|------|--------|
| `pvp` | 允许PVP | false |
| `build` | 允许建造 | false |
| `destroy` | 允许破坏 | false |
| `interact` | 允许交互 | false |
| `enter` | 允许进入 | true |

## 使用方法

### 创建领地

1. 输入 `/rus ade` 进入选区模式
2. 手持木剑（可配置），蹲下右键点击第一个方块
3. 蹲下右键点击第二个方块
4. 输入 `/rus create <名称>` 创建领地

### 保护规则

- **管理员** - 不受任何限制
- **领地拥有者** - 在自己的领地内不受限制
- **其他玩家** - 受领地保护规则限制

## 数据存储

领地数据存储在 `/plugins/RusTerritory/data/` 目录下，按玩家 UUID 命名 JSON 文件。

## 构建

```bash
mvn clean package
```

## 依赖

- Java 21
- Folia API 1.21.1

## 许可证

MIT License
