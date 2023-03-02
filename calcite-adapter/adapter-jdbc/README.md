# 描述
本模块DEMO，主要是学习了解使用Calcite请求JDBC数据源，具体案例包括：单数据源请求，多数据源JOIN请求,直接执行Linq4j代码查询等
# 单数据源请求
我们选用MYSQL数据库，表DDL如下：
```sql
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `sex` varchar(5) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;

INSERT INTO `test`.`student` (`id`, `name`, `sex`) VALUES (1, '小明', '男');
INSERT INTO `test`.`student` (`id`, `name`, `sex`) VALUES (2, '小红', '女');
```
查询SQL如下：
```sql
select sex,count(sex) as sex_count
from student
where id>0
group by sex
```
测试代码如下：
```java
public static void main(String[] args) throws Exception {
    String path = MultiDatasourceQuery.class.getResource("/multi_datasource_model.json").toString();
    Properties properties = new Properties();
    properties.setProperty("caseSensitive", "false");
    properties.put("model", path.replace("file:", ""));

    String sql = "select student.name, sum(score.grade) as grade " +
            "from db1.student as student join db2.score as score on student.id=score.student_id " +
            "where student.id>0 " +
            "group by student.name ";

    try (Connection conn = DriverManager.getConnection("jdbc:calcite:",properties)) {
        // 查询数据
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetUtil.printRs(rs);
    } catch (SQLException exception) {
        exception.printStackTrace();
    }
}
```
查询结果：
```shell
SEX:男 , SEX_COUNT:1
SEX:女 , SEX_COUNT:1
```

# 多数据源JOIN
在上面数据源的基础上，增加一种新的数据库，我们选用postgresql, 表DDL如下：
```sql
CREATE TABLE IF NOT EXISTS score
(
    id integer NOT NULL,
    student_id integer NOT NULL,
    grade integer NOT NULL,
    PRIMARY KEY (id)
)

insert into score(id,student_id,grade)values(1,1,80);
insert into score(id,student_id,grade)values(2,2,90);
```
查询SQL为：
```sql
select student.name, sum(score.grade) as grade
from db1.student as student join db2.score as score on student.id=score.student_id
where student.id>0
group by student.name
```
测试代码为：
```java
public static void main(String[] args) throws Exception {
    String path = MultiDatasourceQuery.class.getResource("/multi_datasource_model.json").toString();
    Properties properties = new Properties();
    properties.setProperty("caseSensitive", "false");
    properties.put("model", path.replace("file:", ""));
    properties.put("charset", "utf8");

    String sql = "select student.name, sum(score.grade) as grade " +
            "from db1.student as student join db2.score as score on student.id=score.student_id " +
            "where student.id>0 " +
            "group by student.name ";

    try (Connection conn = DriverManager.getConnection("jdbc:calcite:",properties)) {
        // 查询数据
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetUtil.printRs(rs);
    } catch (SQLException exception) {
        exception.printStackTrace();
    }
}
```
查询结果：
```shell
NAME:小明 , GRADE:80
NAME:小红 , GRADE:90
```

# 直接执行Linq4j代码
这个DEMO,是直接执行SQL生成的LINQ JAVA查询代码，对于这个查询SQL：
```sql
select sex,count(sex) as sex_count
from student
where id>0
group by sex
```
最终通过Linq4j生成的代码是查询JAVA代码是类似这样的：
```java
public org.apache.calcite.linq4j.Enumerable bind(final org.apache.calcite.DataContext root) {
  final org.apache.calcite.linq4j.function.Function1 rowBuilderFactory = new org.apache.calcite.linq4j.function.Function1() {
    public org.apache.calcite.linq4j.function.Function0 apply(final java.sql.ResultSet resultSet) {
      return new org.apache.calcite.linq4j.function.Function0() {
          public Object apply() {
            try {
              final Object[] values = new Object[2];
              values[0] = resultSet.getObject(1);
              values[1] = resultSet.getLong(2);
              if (resultSet.wasNull()) {
                values[1] = null;
              }
              return values;
            } catch (java.sql.SQLException e) {
              throw new RuntimeException(
                e);
            }
          }
        }
      ;
    }
    public Object apply(final Object resultSet) {
      return apply(
        (java.sql.ResultSet) resultSet);
    }
  }
  ;
  final org.apache.calcite.runtime.ResultSetEnumerable enumerable = org.apache.calcite.runtime.ResultSetEnumerable.of((javax.sql.DataSource) root.getRootSchema().getSubSchema("db1").unwrap(javax.sql.DataSource.class), "SELECT `sex`, COUNT(*) AS `SEX_COUNT`\nFROM `student`\nWHERE `id` > 0\nGROUP BY `sex`", rowBuilderFactory);
  enumerable.setTimeout(root);
  return enumerable;
}

public Class getElementType() {
  return java.lang.Object[].class;
}
```
下面我们直接执行一下上述代码，相关Demo如下：
```java
public static void main(String[] args) throws Exception {
        DataContext dataContext = getDataContext();
        Bindable bindable = getBindable(2);
        Enumerable enumerable = bindable.bind(dataContext);
        EnumerableUtil.printEnumerator(enumerable.enumerator());
    }

    private static DataContext getDataContext() throws Exception {
        CalciteConnection calciteConnection = (CalciteConnection) getConnection();
        return Schemas.createDataContext(calciteConnection, calciteConnection.getRootSchema());
    }

    private static Bindable getBindable(int fieldCount) throws Exception {
        ICompilerFactory compilerFactory = CompilerFactoryFactory.getDefaultCompilerFactory();
        final IClassBodyEvaluator cbe = compilerFactory.newClassBodyEvaluator();
        cbe.setClassName("Baz");
        cbe.setExtendedClass(Utilities.class);
        cbe.setImplementedInterfaces(fieldCount == 1 ? new Class[]{Bindable.class, Typed.class}
                : new Class[]{ArrayBindable.class});
        cbe.setParentClassLoader(EnumerableInterpretable.class.getClassLoader());
        // executeExpression is java code generated by linq4j
        return (Bindable) cbe.createInstance(new StringReader(executeExpression));
    }

    private static Connection getConnection() throws Exception {
        String path = BindableExecute.class.getResource("/single-datasource-model.json").toString();
        Properties info = new Properties();
        info.setProperty("caseSensitive", "false");
        info.put("model", path.replace("file:", ""));
        return DriverManager.getConnection("jdbc:calcite:", info);
    }
```
最终输出结果，与SQL查询的结果一样，只是格式不同：
````shell
[男, 1]
[女, 2]
````
详细demo可以看代码中的BindableExecute.