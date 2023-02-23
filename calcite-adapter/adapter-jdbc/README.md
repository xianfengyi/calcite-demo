# 描述
本模块DEMO，主要是学习了解使用Calcite请求JDBC数据源，具体案例包括：单数据源请求，多数据源JOIN请求等
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