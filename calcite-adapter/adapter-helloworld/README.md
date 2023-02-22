# 背景和目标
Calcite官方提供的CSV的Example很完善，然后对乎初学者缺有些复杂了，对于一个初学Calcite的同学来说，最希望的
就是能像学一门新语言一样，快速跑通HelloWorld就好，因此本Demo就是一个简单的HelloWorld Demo.

本Demo希望实现的目标为：通过Calcite读取文件中的Hello World，并打印出来，业务逻辑非常简单，没有CSV解析等，这样我们
可以更关注Calcite的编程模式。

# Hello World Demo
利用Calcite开发一个查询器，或者说是一个adapter，主要分为如下几步：
* 定义自己的Table，实现ScannableTable/FilterableTable/TranslateTable其中一种；
* 定义自己的Enumerator，这个是定义如何访问数据；
* 定义自己的Schema，这个是管理Table的；
* 定义一个SchemaFactory，这个是生成Schema的工厂；
* 编写Schema Model文件，指定SchemaFactory等。

通过上面几步，一个最简单的DEMO就可以完成了，下面一起看下代码

## CustomTable
我们自己实现一个Table，名为CustomTable，定义如下：
```java
public class CustomTable extends AbstractTable implements ScannableTable {

    private Source source;

    public CustomTable(Source source) {
        this.source = source;
    }

    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        JavaTypeFactory typeFactory = (JavaTypeFactory) relDataTypeFactory;
        List<String> names = new ArrayList<String>();
        names.add("value");

        List<RelDataType> types = new ArrayList<RelDataType>();
        types.add(typeFactory.createSqlType(SqlTypeName.VARCHAR));

        return typeFactory.createStructType(Pair.zip(names, types));
    }

    public Enumerable<Object[]> scan(DataContext dataContext) {
        return new AbstractEnumerable<Object[]>() {

            public Enumerator<Object[]> enumerator() {
                return new CustomEnumerator<Object[]>(source);
            }
        };
    }
}
```
## CustomEnumerator
其中scan方法中Enumerator为我们自定义的CustomEnumerator，代码如下：
```java
public class CustomEnumerator<E> implements Enumerator<E> {

    private E current;

    private BufferedReader br;

    public CustomEnumerator(Source source) {
        try {
            this.br = new BufferedReader(source.reader());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public E current() {
        return current;
    }

    public boolean moveNext() {
        try {
            String line = br.readLine();
            if (line == null) {
                return false;
            }
            current = (E) new Object[]{line};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void reset() {
        System.out.println("不支持此操作");
    }

    public void close() {

    }
}
```
## CustomSchema
Schema主要是管理表的，通过getTableMap返回表名和表的映射，因此我们将我们定义的好的表放入到集合中，代码如下所示：
```java
public class CustomSchema extends AbstractSchema {

    private Map<String, Table> tableMap;

    @Override
    protected Map<String, Table> getTableMap() {
        URL url = CustomSchema.class.getResource("/data.txt");
        Source source = Sources.of(url);
        if (tableMap == null) {
            final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
            builder.put("T_TEST_TABLE", new CustomTable(source));
            tableMap = builder.build();
        }
        return tableMap;
    }
}
```
## CustomSchemaFactory
最后，定一个SchemaFactory用于生成Schem，代码如下所示：
```java
public class CustomSchemaFactory implements SchemaFactory {

    public Schema create(SchemaPlus schemaPlus, String name, Map<String, Object> operand) {
        return new CustomSchema();
    }
}
```
## Schema Model文件
我们的Schema Model文件定义如下，主要指定SchemaFactory:
```json
{
    "version": "1.0",
    "defaultSchema": "TEST",
    "schemas": [
        {
            "name": "TEST",
            "type": "custom",
            "factory": "com.github.pioneeryi.CustomSchemaFactory",
            "operand": {}
        }
    ]
}
```
## 数据文件
data.txt，内容如下：
```shell
hello,baby
```

## 测试
```java
public class Application {

    public static void main(String[] args) {
        try {
            String path = URLDecoder.decode(Application.class.getResource("/model.json").toString(), "UTF-8");
            Properties info = new Properties();
            info.put("model", path.replace("file:", ""));
            Connection connection = DriverManager.getConnection("jdbc:calcite:", info);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from T_TEST_TABLE");
            while (resultSet.next()) {
                System.out.println(resultSet.getObject("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
输出打印：
```shell
Hello,World
```


