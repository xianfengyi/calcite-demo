import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.rules.ProjectMergeRule;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Application {

    private static final FrameworkConfig config = Frameworks.newConfigBuilder()
            .parserConfig(SqlParser.config()
                    .withParserFactory(SqlParserImpl.FACTORY)
                    .withCaseSensitive(false)
                    .withQuoting(Quoting.BACK_TICK)
                    .withQuotedCasing(Casing.TO_UPPER)
                    .withUnquotedCasing(Casing.TO_UPPER)
                    .withConformance(SqlConformanceEnum.ORACLE_12))
            .build();


    public static void main(String[] args) {
        String sql =
                "SELECT u.id, name, age, sum(price) " + "FROM users AS u join orders AS o ON u.id = o.user_id " +
                        "WHERE age >= 20 AND age <= 30 " + "GROUP BY u.id, name, age " + "ORDER BY u.id";
        // 将 SQL 转换为 SQLNode
        SqlNode originSqlNode = sqlParse(sql);
        // 对 SQL 进行校验
        SqlNode validatedSqlNode = validateSql(originSqlNode);
        // 转换为关系代数 RelNode
        RelRoot relRoot = toRelNode(validatedSqlNode);
        // 对查询进行优化
        RelNode optimizedRelNode = optimize(relRoot.rel);
        // 执行计划
        execute(optimizedRelNode);
    }

    private static SqlNode sqlParse(String sql) {
        try {
            // 创建SqlParser, 用于解析SQL字符串
            SqlParser parser = SqlParser.create(sql, config.getParserConfig());
            // 解析SQL字符串, 生成SqlNode树
            return parser.parseStmt();
        } catch (SqlParseException e) {
            throw new RuntimeException("sql parse fail");
        }
    }

    private static SqlNode validateSql(SqlNode sqlNode) {
        // 创建Schema, 一个Schema中包含多个表
        SimpleTable userTable = SimpleTable.newBuilder("users")
                .addField("id", SqlTypeName.VARCHAR)
                .addField("name", SqlTypeName.VARCHAR)
                .addField("age", SqlTypeName.INTEGER)
                .withFilePath("/path/to/user.csv")
                .withRowCount(10)
                .build();
        SimpleTable orderTable = SimpleTable.newBuilder("orders")
                .addField("id", SqlTypeName.VARCHAR)
                .addField("user_id", SqlTypeName.VARCHAR)
                .addField("goods", SqlTypeName.VARCHAR)
                .addField("price", SqlTypeName.DECIMAL)
                .withFilePath("/path/to/order.csv")
                .withRowCount(10).build();
        Schema schema = SimpleSchema.newBuilder("s").addTable(userTable).addTable(orderTable).build();
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(false, false);
        rootSchema.add(schema.getSchemaName(), schema);

        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

        // 创建CatalogReader, 用于指示如何读取Schema信息
        Prepare.CatalogReader catalogReader = new CalciteCatalogReader(rootSchema,
                Collections.singletonList(schema.getSchemaName()), typeFactory, config);
        // 创建SqlValidator, 用于执行SQL验证
        SqlValidator.Config validatorConfig =
                SqlValidator.Config.DEFAULT.withLenientOperatorLookup(config.lenientOperatorLookup()).withSqlConformance(config.conformance()).withDefaultNullCollation(config.defaultNullCollation()).withIdentifierExpansion(true);
        SqlValidator validator = SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalogReader,
                typeFactory, validatorConfig);
        // 执行SQL验证
        return validator.validate(sqlNode);
    }

    private static RelRoot toRelNode(SqlNode sqlNode) {
        // 创建VolcanoPlanner, VolcanoPlanner在后面的优化中还需要用到
        VolcanoPlanner planner = new VolcanoPlanner(RelOptCostImpl.FACTORY, Contexts.of(config));
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        // 创建SqlToRelConverter
        RelOptCluster cluster = RelOptCluster.create(planner, new RexBuilder(typeFactory));
        SqlToRelConverter.Config converterConfig =
                SqlToRelConverter.config().withTrimUnusedFields(true).withExpand(false);
        SqlToRelConverter converter = new SqlToRelConverter(null, validator, catalogReader, cluster,
                StandardConvertletTable.INSTANCE, converterConfig);
        // 将SqlNode树转化为RelNode树
        RelRoot relRoot = converter.convertQuery(sqlNode, false, true);
        return relRoot;
    }

    private static RelNode optimize(RelNode relNode) {
        HepProgramBuilder builder = new HepProgramBuilder();
        HepPlanner hepPlanner = new HepPlanner(builder.build());

        // 优化规则
        RuleSet rules = RuleSets.ofList(CoreRules.FILTER_TO_CALC, CoreRules.PROJECT_TO_CALC,
                CoreRules.FILTER_CALC_MERGE, CoreRules.PROJECT_CALC_MERGE, CoreRules.FILTER_INTO_JOIN,     //
                // 过滤谓词下推到Join之前
                EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE, EnumerableRules.ENUMERABLE_PROJECT_TO_CALC_RULE,
                EnumerableRules.ENUMERABLE_FILTER_TO_CALC_RULE, EnumerableRules.ENUMERABLE_JOIN_RULE,
                EnumerableRules.ENUMERABLE_SORT_RULE, EnumerableRules.ENUMERABLE_CALC_RULE,
                EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
        Program program = Programs.of(RuleSets.ofList(rules));
        RelNode optimizerRelTree = program.run(hepPlanner, relNode,
                relNode.getTraitSet().plus(EnumerableConvention.INSTANCE), Collections.emptyList(),
                Collections.emptyList());
        return optimizerRelTree;
    }

    private static void execute(RelNode optimizerRelTree) {
        EnumerableRel enumerable = (EnumerableRel) optimizerRelTree;
        Map<String, Object> internalParameters = new LinkedHashMap<>();
        EnumerableRel.Prefer prefer = EnumerableRel.Prefer.ARRAY;
        Bindable bindable = EnumerableInterpretable.toBindable(internalParameters, null, enumerable, prefer);
        Enumerable bind = bindable.bind(new SimpleDataContext(rootSchema.plus()));
        Enumerator enumerator = bind.enumerator();
        while (enumerator.moveNext()) {
            Object current = enumerator.current();
            Object[] values = (Object[]) current;
            StringBuilder sb = new StringBuilder();
            for (Object v : values) {
                sb.append(v).append(",");
            }
            sb.setLength(sb.length() - 1);
            System.out.println(sb);
        }
    }
}
