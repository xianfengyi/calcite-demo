# SQL优化原理
一般的优化规则有：常量折叠、谓词下推等，当前Demo，我们验证下Calcite的谓词下推优化，当前Calcite还有很多其他的优化规则，同时我们也可以自定义优化规则

# Calcite实现
有两张表，用户表users和订单表orders,表的字段信息如下：
```java
private static CalciteSchema getCalciteRootSchema() {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(false, false);
        rootSchema.add("users", new AbstractTable() {
@Override
public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        RelDataTypeFactory.Builder builder = new RelDataTypeFactory.Builder(typeFactory);
        builder.add("id", new BasicSqlType(typeSystem, SqlTypeName.VARCHAR));
        builder.add("name", new BasicSqlType(typeSystem, SqlTypeName.VARCHAR));
        builder.add("age", new BasicSqlType(typeSystem, SqlTypeName.INTEGER));
        return builder.build();
        }
        });
        rootSchema.add("orders", new AbstractTable() {
@Override
public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        RelDataTypeFactory.Builder builder = new RelDataTypeFactory.Builder(typeFactory);
        builder.add("id", new BasicSqlType(typeSystem, SqlTypeName.VARCHAR));
        builder.add("user_id", new BasicSqlType(typeSystem, SqlTypeName.VARCHAR));
        builder.add("goods", new BasicSqlType(typeSystem, SqlTypeName.VARCHAR));
        builder.add("price", new BasicSqlType(typeSystem, SqlTypeName.DECIMAL));
        return builder.build();
        }
        });
        return rootSchema;
}
```
对这两张表进行关联查询，查询SQL如下：
```sql
SELECT u.id, name, age, sum(price) 
FROM users AS u join orders AS o ON u.id = o.user_id
WHERE age >= 20 AND age <= 30
GROUP BY u.id, name, age
ORDER BY u.id
```
对SQL进行解析以及校验，并转化为关系代数，代码如下：
```java
// 将 SQL 转换为 SQLNode
SqlNode originSqlNode = sqlParse(sql);

// 对 SQL 进行校验
SqlNode validatedSqlNode = validateSql(originSqlNode);

// 转换为关系代数 RelNode
RelRoot relRoot = toRelNode(validatedSqlNode);
```
未优化前，执行计划为：
```shell
LogicalSort(sort0=[$0], dir0=[ASC]): rowcount = 37.5, cumulative cost = {2530.1562517881393 rows, 7007.557979625184 cpu, 0.0 io}, id = 19
  LogicalAggregate(group=[{0, 1, 2}], EXPR$3=[SUM($3)]): rowcount = 37.5, cumulative cost = {2492.6562517881393 rows, 3202.0 cpu, 0.0 io}, id = 18
    LogicalProject(ID=[$0], NAME=[$1], AGE=[$2], price=[$6]): rowcount = 375.0, cumulative cost = {2450.0 rows, 3202.0 cpu, 0.0 io}, id = 17
      LogicalFilter(condition=[AND(>=($2, 20), <=($2, 30))]): rowcount = 375.0, cumulative cost = {2075.0 rows, 1702.0 cpu, 0.0 io}, id = 16
        LogicalJoin(condition=[=($0, $4)], joinType=[inner]): rowcount = 1500.0, cumulative cost = {1700.0 rows, 202.0 cpu, 0.0 io}, id = 15
          LogicalTableScan(table=[[users]]): rowcount = 100.0, cumulative cost = {100.0 rows, 101.0 cpu, 0.0 io}, id = 13
          LogicalTableScan(table=[[orders]]): rowcount = 100.0, cumulative cost = {100.0 rows, 101.0 cpu, 0.0 io}, id = 14
```
利用Calcite的HepPlanner对其进行优化，优化代码如下：
```java
private static RelNode optimize(RelNode relNode) {
    HepProgramBuilder builder = new HepProgramBuilder();
    // 添加优化规则
    builder.addRuleInstance(CoreRules.FILTER_INTO_JOIN);

    HepPlanner hepPlanner = new HepPlanner(builder.build());
    hepPlanner.setRoot(relNode);
    return hepPlanner.findBestExp();
}
```
经过优化后，执行变化为：
```shell
LogicalSort(sort0=[$0], dir0=[ASC]): rowcount = 37.5, cumulative cost = {1055.1562517881393 rows, 5607.557979625184 cpu, 0.0 io}, id = 30
  LogicalAggregate(group=[{0, 1, 2}], EXPR$3=[SUM($3)]): rowcount = 37.5, cumulative cost = {1017.6562517881393 rows, 1802.0 cpu, 0.0 io}, id = 28
    LogicalProject(ID=[$0], NAME=[$1], AGE=[$2], price=[$6]): rowcount = 375.0, cumulative cost = {975.0 rows, 1802.0 cpu, 0.0 io}, id = 26
      LogicalJoin(condition=[=($0, $4)], joinType=[inner]): rowcount = 375.0, cumulative cost = {600.0 rows, 302.0 cpu, 0.0 io}, id = 35
        LogicalFilter(condition=[SEARCH($2, Sarg[[20..30]])]): rowcount = 25.0, cumulative cost = {125.0 rows, 201.0 cpu, 0.0 io}, id = 32
          LogicalTableScan(table=[[users]]): rowcount = 100.0, cumulative cost = {100.0 rows, 101.0 cpu, 0.0 io}, id = 13
        LogicalTableScan(table=[[orders]]): rowcount = 100.0, cumulative cost = {100.0 rows, 101.0 cpu, 0.0 io}, id = 14
```
可以看到，过滤条件已经下推到join下面，这样就达到了优化的效果.

完整的示例，可以看本module的Demo！