[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/u1xW62gh)

# Campus Dungeon

Campus Dungeon 是本小组面向“软件工程实训任务二：小组协同开发”的 Java/Maven 游戏项目。项目当前仓库历史中已经完成了 `Campus Defense Zuul` 命令行文字冒险原型，用于验证房间、道具、背包、NPC、任务门禁、评分和结局等核心玩法；后续开发方向已确定升级为 BYOG-inspired 的图形化随机地牢游戏。

新的目标不是继续扩展 Zuul 命令行，而是在当前 Maven 工程中独立实现一个校园答辩主题的图形地牢：玩家输入 seed 生成随机世界，在迷雾中探索图书馆、实验室、机房、办公室和答辩大厅，收集材料、和 NPC 互动、解锁门禁，最终完成软件工程实践答辩。

## 当前状态

当前 `master` 上的可运行代码仍是 Zuul 原型，入口为：

```bash
mvn test
java -cp target/classes cn.edu.whut.sept.zuul.Main
```

原型已实现：

- 校园地图：校门、讲堂、图书馆、实验室、办公室、花园、传送门、答辩教室等场景。
- 房间物品：每个房间可以放置物品，物品包含名称、描述和重量。
- 玩家背包：玩家可以携带物品，背包受负重上限限制。
- 核心命令：`go`、`look`、`back`、`take`、`drop`、`items`、`eat`、`talk`、`status`、`score`、`help`、`quit`。
- 多步返回、魔法饼干、传送房间、任务门禁、NPC 线索、步数评分和结局评价。

Zuul 原型的完整演示脚本：

```bash
mvn test
printf 'status\ngo north\ntalk librarian\ntake report\ngo south\ngo south\ntalk assistant\ntake laptop\ngo east\ntake pass\ngo west\ngo north\ngo east\ntalk mentor\ntake slides\nscore\ngo east\nquit\n' \
  | java -cp target/classes cn.edu.whut.sept.zuul.Main
```

## 项目转向

小组讨论后认为，当前 Zuul 玩法虽然已经满足基础功能扩展，但核心难度仍然偏低，展示效果也不如图形化游戏。后续项目升级为 `Campus Dungeon`，保留“校园答辩准备”主题，同时引入 BYOG 风格的随机世界、图形瓦片渲染、保存读取和输入回放测试。

转向原则：

- 已关闭的 Zuul Issue 和 PR 保留为早期原型历史。
- 不改写旧 Issue 历史，不把旧方向 Issue 强行改成新方向。
- 仍然 open 的旧 Zuul 交付 Issue 后续关闭，并说明新测试/CI/文档工作将在 Campus Dungeon Issue 中重新拆分。
- 后续代码包名建议从 `cn.edu.whut.sept.zuul` 切换为 `cn.edu.whut.sept.dungeon`。

本地设计沉淀位于：

- `docs/campus-dungeon-design.md`：项目转向设计、架构、玩法、存档、测试策略。
- `docs/campus-dungeon-issue-plan.md`：可发布到 GitHub 的 Issue 分工计划。

注意：`docs/` 是本地课程资料和设计沉淀目录，已按小组仓库边界约定加入 `.gitignore`，不会进入 Git 跟踪。

## Campus Dungeon 目标功能

第一阶段目标是完成一个可玩的图形化随机地牢闭环：

- Java Swing 自研瓦片渲染和 HUD。
- seed 随机生成 `80 x 40` 左右的房间/走廊地图。
- BYOG 风格输入：`n<seed>s` 新游戏、`l` 读取、`w/a/s/d` 移动、`e` 交互、`i` 查看状态、`:q` 保存退出。
- 玩家移动、墙体碰撞、步数统计和视野更新。
- 迷雾探索：未探索区域黑色、已探索区域暗色、当前视野正常显示。
- JSON 保存和读取完整 `GameState`。
- 道具、背包、NPC、门禁和答辩大厅通关流程。
- `playWithInputString` 风格输入回放接口，用于不打开 GUI 的 JUnit 自动化测试。

MVP 通关条件：

```text
has(report)
has(laptop)
has(slides)
has(pass)
at(defense-hall door)
interact()
```

核心任务规则：

```text
report requires student-card + librarian/library interaction
slides requires usb + computer-room interaction
pass requires teacher interaction + report/laptop/slides
defense hall requires pass + report/laptop/slides
```

## 协作分工

本小组当前按两人协作设计：

| 成员 | GitHub 用户名 | 主线职责 | 协作职责 |
|---|---|---|---|
| 成员 A | `siolyn` | 功能开发、核心架构、游戏规则实现 | 为测试、CI、README 和报告提供功能路径，修复质量主线发现的问题 |
| 成员 B | `sand8-ui` | 测试、CI、打包、README、报告证据、Review | 跟随功能 PR 做测试设计、Review 和验收证据记录 |

协作方式：

- 功能 Issue 由 `siolyn` 主责实现，`sand8-ui` 负责 Review 并把可自动化验证的场景同步到测试主线。
- 测试、CI、文档 Issue 由 `sand8-ui` 主责，`siolyn` 协助提供演示路径、输入脚本和修复反馈。
- 每个 Issue 使用独立分支开发，PR 描述中关联 Issue 并记录验证结果。
- 测试、CI、README 和报告证据跟随功能增量推进，不作为最后一次性补交。

## 开发流程

1. 先在 GitHub Issue 中拆分任务并分配成员。
2. 每个 Issue 使用独立分支开发，例如 `feat/dungeon-engine`。
3. 功能完成后运行 Maven 验证和必要的手动流程。
4. 创建 Pull Request，在 PR 中关联 Issue，记录测试结果和手动验证证据。
5. 由另一名成员 Review 后合并。

提交说明采用：

```text
type(scope): summary
```

示例：

```text
feat(world): add seeded dungeon generator
test(core): cover movement replay
ci(build): run maven tests on pull requests
```

## 运行与验证

当前 Zuul 原型：

```bash
mvn test
java -cp target/classes cn.edu.whut.sept.zuul.Main
```

后续 Campus Dungeon GUI 入口完成后，README 需要同步更新为新的运行命令，例如：

```bash
mvn test
java -cp target/classes cn.edu.whut.sept.dungeon.Main
```

后续打包 Issue 会配置可执行 jar。当前 `pom.xml` 尚未配置最终 `Main-Class`，因此不要把 `mvn package` 生成的 jar 当作最终发布包。

## 历史 Issue 与 PR

Zuul 原型阶段已形成以下开发记录：

| Issue | 内容 | PR |
|---|---|---|
| [#3](https://github.com/wutcst/kai-fa-zuul/issues/3) | 玩家、物品与房间领域模型 | [#12](https://github.com/wutcst/kai-fa-zuul/pull/12) |
| [#4](https://github.com/wutcst/kai-fa-zuul/issues/4) | 核心交互命令 | [#13](https://github.com/wutcst/kai-fa-zuul/pull/13) |
| [#5](https://github.com/wutcst/kai-fa-zuul/issues/5) | 校园地图、任务线入口与胜利条件 | [#14](https://github.com/wutcst/kai-fa-zuul/pull/14) |
| [#9](https://github.com/wutcst/kai-fa-zuul/issues/9) | 任务系统、门禁解锁与通关流程 | [#15](https://github.com/wutcst/kai-fa-zuul/pull/15) |
| [#10](https://github.com/wutcst/kai-fa-zuul/issues/10) | NPC 对话与线索系统 | [#16](https://github.com/wutcst/kai-fa-zuul/pull/16) |
| [#11](https://github.com/wutcst/kai-fa-zuul/issues/11) | 步数、分数与结局评价 | [#17](https://github.com/wutcst/kai-fa-zuul/pull/17) |
| [#8](https://github.com/wutcst/kai-fa-zuul/issues/8) | README 与报告证据清单 | [#18](https://github.com/wutcst/kai-fa-zuul/pull/18) |

Campus Dungeon 阶段的 Issue 将按新的分工计划重新发布。

## AI 辅助说明模板

课程允许使用 AI 辅助，但报告中需要说明模型和辅助范围。可在报告中按以下格式填写：

```text
本项目开发过程中使用 AI 工具辅助进行了课程要求解析、项目方向讨论、Issue 规划、部分设计文档整理、代码实现建议和验证命令设计。核心代码由小组成员基于课程要求审阅、运行、修改和提交。AI 辅助内容均经过人工确认，并通过 Maven 编译、自动化测试或手动流程验证。
```
