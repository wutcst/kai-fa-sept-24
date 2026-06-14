[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/u1xW62gh)

# Campus Defense Zuul

Campus Defense Zuul 是基于 `world-of-zuul` 扩展的命令行文字冒险游戏。游戏主题是“软件工程实践答辩”：玩家需要在校园场景中探索房间、收集答辩材料、和 NPC 交流获取线索、通过门禁检查，最终进入答辩教室完成实践答辩。

本项目用于“软件工程实训任务二：小组协同开发”，开发过程按 GitHub Issue 拆分、分支开发、PR 合并和 Maven 验证推进。

## 游戏目标

玩家从校门出发，需要收集以下答辩材料：

- `report`：软件工程实践报告
- `laptop`：项目演示电脑
- `slides`：答辩演示文稿
- `pass`：答辩通行证

收齐材料后进入 `defense classroom` 即可通关。游戏会输出任务清单、移动步数、分数和结局评价。

## 已实现功能

- 校园地图：校门、讲堂、图书馆、实验室、办公室、花园、传送门、答辩教室等场景。
- 房间物品：每个房间可以放置物品，物品包含名称、描述和重量。
- 玩家背包：玩家可以携带物品，背包受负重上限限制。
- 核心命令：`go`、`look`、`back`、`take`、`drop`、`items`、`eat`、`talk`、`status`、`score`、`help`、`quit`。
- 多步返回：玩家可以通过 `back` 沿历史路径返回。
- 魔法饼干：吃掉 `cookie` 后提升负重能力。
- 传送房间：传送门可随机移动玩家，但需要先取得 `usb`。
- 任务门禁：答辩教室需要玩家携带完整答辩材料才能进入。
- NPC 线索：图书馆管理员、实验室助教、讲堂学长、答辩老师提供任务提示。
- 分数与结局：有效移动计步，关键物品和任务完成计分，通关后输出评价等级。

## 命令说明

| 命令 | 示例 | 说明 |
|---|---|---|
| `go <direction>` | `go north` | 向指定方向移动 |
| `look` | `look` | 查看当前房间、出口、物品和 NPC |
| `back` | `back` | 回到上一个房间 |
| `take <item>` | `take report` | 拾取当前房间中的物品 |
| `drop <item>` | `drop coin` | 丢弃背包中的物品 |
| `items` | `items` | 查看房间物品、背包和负重 |
| `eat cookie` | `eat cookie` | 吃掉魔法饼干并提升负重 |
| `talk <npc>` | `talk librarian` | 与当前房间 NPC 对话 |
| `status` | `status` | 查看任务进度、步数、分数和负重 |
| `score` | `score` | 同 `status` |
| `help` | `help` | 查看所有命令 |
| `quit` | `quit` | 退出游戏 |

## 运行方式

项目使用 Maven 管理，当前 `pom.xml` 配置 Java 8 编译目标。

编译并运行测试：

```bash
mvn test
```

启动游戏：

```bash
mvn test
java -cp target/classes cn.edu.whut.sept.zuul.Main
```

当前 `pom.xml` 尚未配置可执行 jar 的 `Main-Class`，因此 `mvn package` 可以生成 jar，但还不能直接通过 `java -jar target/zuul-1.0-SNAPSHOT.jar` 启动。该工作由 CI/打包 Issue 继续完善。

## 演示脚本

下面的脚本可以走通一条完整答辩流程：

```bash
mvn test
printf 'status\ngo north\ntalk librarian\ntake report\ngo south\ngo south\ntalk assistant\ntake laptop\ngo east\ntake pass\ngo west\ngo north\ngo east\ntalk mentor\ntake slides\nscore\ngo east\nquit\n' \
  | java -cp target/classes cn.edu.whut.sept.zuul.Main
```

预期结果包括：

- `status` 显示任务清单、步数、分数和负重。
- NPC 输出线索，例如 `librarian`、`assistant`、`mentor`。
- 进入答辩教室后输出 `Ending rank: Excellent`。

## 开发流程

本项目按课程要求采用 GitHub 协作流程：

1. 先在 GitHub Issue 中拆分任务。
2. 每个 Issue 使用独立分支开发，例如 `feat/game-commands`。
3. 完成后运行 Maven 验证和必要的手动交互脚本。
4. 创建 Pull Request，在 PR 中关联 Issue 并记录验证结果。
5. 合并后关闭对应 Issue。

提交说明采用 `type(scope): summary` 风格，例如：

```text
feat(command): add inventory and navigation commands
feat(map): expand campus defense adventure
```

## Issue 与 PR 记录

| Issue | 内容 | PR |
|---|---|---|
| [#3](https://github.com/wutcst/kai-fa-zuul/issues/3) | 玩家、物品与房间领域模型 | [#12](https://github.com/wutcst/kai-fa-zuul/pull/12) |
| [#4](https://github.com/wutcst/kai-fa-zuul/issues/4) | 核心交互命令 | [#13](https://github.com/wutcst/kai-fa-zuul/pull/13) |
| [#5](https://github.com/wutcst/kai-fa-zuul/issues/5) | 校园地图、任务线入口与胜利条件 | [#14](https://github.com/wutcst/kai-fa-zuul/pull/14) |
| [#9](https://github.com/wutcst/kai-fa-zuul/issues/9) | 任务系统、门禁解锁与通关流程 | [#15](https://github.com/wutcst/kai-fa-zuul/pull/15) |
| [#10](https://github.com/wutcst/kai-fa-zuul/issues/10) | NPC 对话与线索系统 | [#16](https://github.com/wutcst/kai-fa-zuul/pull/16) |
| [#11](https://github.com/wutcst/kai-fa-zuul/issues/11) | 步数、分数与结局评价 | [#17](https://github.com/wutcst/kai-fa-zuul/pull/17) |
| [#8](https://github.com/wutcst/kai-fa-zuul/issues/8) | README 与报告证据清单 | 当前文档分支 |

待完善工程交付项：

- [#6](https://github.com/wutcst/kai-fa-zuul/issues/6)：补充核心玩法单元测试
- [#7](https://github.com/wutcst/kai-fa-zuul/issues/7)：添加 Maven 自动测试与可执行打包

## 分工记录

README 中的最终分工应以 GitHub Issue assignee、PR 和 Review 记录为准。目前功能开发主线已形成以下证据：

- 领域模型、命令、地图、任务门禁、NPC 和评分功能均通过独立 Issue/PR 完成。
- 每个 PR 中记录了 `mvn test` 和对应手动验证命令。
- 工程交付主线仍需补充 JUnit 测试、CI 和可执行 jar 证据。

## AI 辅助说明模板

课程允许使用 AI 辅助，但报告中需要说明模型和辅助范围。可在报告中按以下格式填写：

```text
本项目开发过程中使用 AI 工具辅助进行了需求拆分、Issue 规划、部分代码实现建议、README 草稿整理和验证命令设计。核心代码由小组成员基于课程要求审阅、运行、修改和提交。AI 辅助内容均经过人工确认，并通过 Maven 编译或手动流程验证。
```

## 项目结构

```text
src/main/java/cn/edu/whut/sept/zuul/
  Command.java          # 命令抽象类
  CommandWords.java     # 命令注册表
  Parser.java           # 命令行输入解析
  Game.java             # 地图、任务、分数和主流程
  Room.java             # 房间、出口、物品、NPC
  Player.java           # 玩家位置、历史路径、背包和负重
  Item.java             # 物品模型
  Npc.java              # NPC 模型
  *Command.java         # 各类命令实现
```

## 当前限制

- 目前还没有系统化 JUnit 测试，后续由 [#6](https://github.com/wutcst/kai-fa-zuul/issues/6) 补齐。
- 目前还没有 GitHub Actions 和可执行 jar 配置，后续由 [#7](https://github.com/wutcst/kai-fa-zuul/issues/7) 完成。
- 目前是命令行文字游戏，没有 GUI、数据库或网络多人模式；这是为了保持两人小组的实现范围可控，并把重点放在面向对象设计和协作流程上。
