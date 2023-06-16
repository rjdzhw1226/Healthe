package com.itheima.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
/**
 * @description 根据xml的insert语句生成表创建语句
 * @author pengXiong
 * @date 2020/8/5 19:19
 */
public class Sqlmap2Table {

    public static Sqlmap2Table instance = new Sqlmap2Table();
    // 默认所有的varchar都是512，可以保证满足绝大多数的字段
    private final String DEFAULT_VARCHAR_LENGTH = "varchar(256)";

    // 有多条插入语句生成的create表做占位使用,方便替换
    private final String COLUMN_PLACEHOLDER = "%Cloumns%";

    private final String SELF_INCRECE_PRIMARY_KEY = "id";
    private final String SELF_INCRECE_PRIMARY_VALUE = " primary key auto_increment";


    // 统计对同一表有重复创建表的sql,需要用户手动合并两个表
    private Map<String, Integer> tableAppearFrequency = new HashMap<>();

    private Map<String, Map<String, String>> combinaTableAppearFrequency = new HashMap<>() ;

    private Sqlmap2Table() {
        // 放置重复出现的需要组合的表和表字段,最后完成后统一写文件, 该重复表key值运行一次后请从打印日志[出现两条创建表语句的表名为 : ]中取到
        String[] tableAppearFrequency = new String[]{
                // 出现两条创建表语句的表名 放入这里
                ""
        };
        for (String str : tableAppearFrequency) {
            combinaTableAppearFrequency.put(str, new HashMap<>());
        }
    }



    public static void main(String[] args) throws JDOMException, IOException {


        String sqlFile = "I:/IT/创建表/create_table.sql";
        String sqlMapPath = "I:/IT/server/src/main/resources/mybatis/mapper";//这里指定你的sqlmap配置文件所在路径
        // 统计对同一表有重复创建表的sql,需要用户手动合并两个表
        Sqlmap2Table.instance.buildInsertSql2Table(sqlMapPath, sqlFile);

    }

    /**
     * 打印出现超过>=2的表的表名字以及汇总等analysis执行完成之后再生成创建表sql
     * @param sqlMapPath
     * @param sqlFilePath
     * @throws IOException
     * @throws JDOMException
     */
    public void buildInsertSql2Table(String sqlMapPath, String sqlFilePath) throws IOException, JDOMException {
        // 先将文件删除
        File sqlFile = new File(sqlFilePath);
        sqlFile.delete();
        // 生成建表sql文件方法
        analysis(sqlMapPath,sqlFilePath);
        // 打印出现超过>=2的表的表名字
        List<Map.Entry> result = tableAppearFrequency.entrySet().stream().filter(table -> table.getValue()>=2).collect(Collectors.toList());
        System.out.println("出现两条创建表语句的表名和出现数量为 : "+ JSON.toJSONString(result));
        System.out.println("出现两条创建表语句的表名为 : "+ JSON.toJSONString(result.stream().map(tabl -> tabl.getKey()).toArray()));
        // 对重复出现表的表字段在map中汇总等analysis执行完成之后再生成创建表sql
        if (MapUtils.isNotEmpty(combinaTableAppearFrequency)) {
            for (Map.Entry<String, Map<String, String>> entry : combinaTableAppearFrequency.entrySet()) {
                Map<String, String> colunmMap = entry.getValue();
                String sql = colunmMap.get(COLUMN_PLACEHOLDER);
                StringBuffer sbCloumns = new StringBuffer();
                for (Map.Entry<String, String> coluns : colunmMap.entrySet()) {
                    String key = coluns.getKey();
                    String value = coluns.getValue();
                    if (Objects.equals(key, COLUMN_PLACEHOLDER)) {
                        continue;
                    }
                    // 构建表列参数
                    sbCloumns = buildCloumns(sbCloumns, key, value);
                }
                sql = sql.replace(COLUMN_PLACEHOLDER, sbCloumns.toString());
                // 将生成的sql写入文件
                writeSql2File(sql, sqlFilePath);
            }
        }
    }

    /**
     * 根据指定的目录进行遍历分析
     *
     * @param path
     * @throws IOException
     * @throws JDOMException
     */
    private void analysis(String path, String sqlFile) throws IOException, JDOMException {
        File filePath = new File(path);
        if (filePath.isDirectory() && !filePath.getName().equals(".svn")) {
            File[] fileList = filePath.listFiles();
            for (File file : fileList) {
                if (file.isDirectory()) {
                    analysis(file.getAbsolutePath(),sqlFile);
                } else {
                    analysisSql(file.getAbsolutePath(),sqlFile);
                }
            }
        }
    }


    /**
     * 分析单个的sqlmap配置文件
     *
     * @param sqlMapFile
     * @throws IOException
     * @throws JDOMException
     */
    private void analysisSql(String sqlMapFile, String sqlFilePath) throws IOException, JDOMException {
        // System.out.println("************"+sqlMapFile);
        boolean isNull=false;
        /**
         * 这里要把sqlmap文件中的这一行去掉：<br>
         * <!DOCTYPE sqlMap PUBLIC "-//iBATIS.com//DTD SQL Map 2.0//EN" "http://www.ibatis.com/dtd/sql-map-2.dtd"><br>
         * 否则JDom根据文件创建Document对象时，会报找不到www.ibatis.com这个异常，导致渲染不成功。
         */
        String xmlString = filterRead(sqlMapFile, "<!DOCTYPE");
        Document doc = getDocument(xmlString);
        Element foo = doc.getRootElement();
        // sql统一转换为小写,并且替换sql中的关键字字符
        String sqlStr = generateKeyWords(foo);
        // 获取拆分出的sql
        List<String> sqls = Arrays.asList(sqlStr.split(";")).parallelStream().filter( sql -> {
            return // sql不为空
                    StringUtils.isNotBlank(sql)
                            // 不包含last_insert_id,last_insert_id会被拆成两个放入list中
                            && !sql.contains("last_insert_id()");
            //&& !sql.contains("LAST_INSERT_ID()");
        }).collect(Collectors.toList());
        // 获取命名空间
        String mapper = ((Attribute)foo.getAttributes().get(0)).getValue();

        // 获取sql中Element的具体信息
        List<Element> allChildren = foo.getChildren();
        for(int i=0; i<allChildren.size(); i++) {
            Element element = allChildren.get(i);
            // 获取当中的某一条sql
            String operatorType = element.getName();
            if (!"insert".equals(operatorType)) {
                // 只按照insert方法处理
                continue;
            }
            // 获取执行的sql
            String sql = sqls.get(i);
            if(!sql.contains("insert") || sql.contains("@")) {
                // 特殊场景处理，
                // 1.有的sql包含查询语句，将该数据剔除
                // 2.存储过程暂时先不生成sql
                System.out.println("这是一条存储过程sql ： "+sql);
                continue;
            }
            // 根据mapper和调用方法获取插入sql的入参class
            Class[] paramClass = getMethodParamClass(mapper, element);
            System.out.println("contentSql : "+sql);
            // 生成插入sql语句
            String createTableSql = createTable(sql, paramClass);
            System.out.println("createTableSql : "+createTableSql);
            // 将生成的sql写入文件
            writeSql2File(createTableSql, sqlFilePath);
        }
    }


    /**
     * sql统一转换为小写,并且替换sql中的关键字字符
     * @param foo 单个sql元素
     * @return 替换后的sql
     */
    private String generateKeyWords(Element foo) {
        String sqlStr = foo.getValue().toLowerCase();
        sqlStr = sqlStr
                .replace("\n", "")
                .replace("insert ", ";insert ")
                .replace("update ", ";update ")
                .replace("delete ", ";delete ")
                .replace("select ", ";select ");
        return sqlStr;
    }


    /**
     * 根据mapper和调用方法获取插入sql的入参class
     * @param mapper dao层映射
     * @param element 单个插入sql元素
     * @return
     */
    private Class[] getMethodParamClass(String mapper, Element element){
        List<Attribute> elementAttrs = element.getAttributes();
        // 获取sql配置方法的名称,可以字段为id
        String methodName = null;
        for (Attribute att : elementAttrs) {
            if ("id".equals(att.getName())) {
                methodName = att.getValue();
                break;
            }
        }
        Class<?> mapperClass  = null;
        try {
            mapperClass = Class.forName(mapper);
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException Message : "+e.getMessage()+"mapperClass :"+JSON.toJSONString(mapperClass));
            e.printStackTrace();
        }
        Method[] methods = mapperClass.getMethods();
        for(Method method : methods) {
            if(methodName.equals(method.getName())) {
                // 判定是不是有泛型参数
                boolean havaGenericparam =
                        Arrays.asList(method.getGenericParameterTypes()).stream().allMatch( param -> param.getTypeName().contains("<"));
                if (!havaGenericparam) {
                    // 不是泛型直接返回参数类型
                    return method.getParameterTypes();
                } else {
                    // 泛型参数需要获取泛型类型<>尖括号中的真正数据类型
                    Type[] types = method.getGenericParameterTypes();
                    Class[] clazz = new Class[types.length];
                    for (int i=0; i<types.length; i++ ) {
                        String paramClazz = types[i].getTypeName();
                        if (paramClazz.contains("Map")) {
                            // Map<String, String>泛型截取最后一个String
                            paramClazz = (paramClazz = types[i].getTypeName()).substring(paramClazz.lastIndexOf(", ")+1, paramClazz.indexOf(">"));
                        } else if (paramClazz.contains("<")) {
                            // List<String>泛型截取最后一个String
                            paramClazz = (paramClazz = types[i].getTypeName()).substring(paramClazz.lastIndexOf("<")+1, paramClazz.indexOf(">"));
                        }
                        try {
                            // 获取到泛型中真正的参数后实例化为class
                            clazz[i] = Class.forName(paramClazz.trim());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    return clazz;
                }
            }
        }
        throw new IllegalArgumentException("class : "+ mapper+" 不存在这种类型的数据");
    }


    /**
     * 根据插入sql语句和对应入参生成sql语句方法
     * @param sql 插入sql语句
     * @param paramClass 插入sql语句对应的入参
     * @return
     */
    private String createTable(String sql, Class[] paramClass) {
        StringBuffer sb = new StringBuffer();
        // 替换dateformat日期类型为默认字段createTime
        sql = replaceDateFormatSql(sql);
        String reg = "[ ,\n,\t,#{,}(,)]";
        // 数据库表字段需要将\n,(,)替换为逗号
        String[] cloumns = sql.substring(indexOfInto(sql),indexOfCloumn(sql, false))
                .replaceAll(reg, ",")
                .split(",");
        // vo字段列需要将空格,#{,}都替换为空,使用,分割
        String[] voCloumns = sql.substring(indexOfCloumn(sql, true),sql.lastIndexOf("}"))
                .replaceAll(reg, ",")
                .split(",");
        List<String> cloumnLists = Arrays.asList(cloumns).parallelStream().distinct().filter(cloum -> StringUtils.isNotBlank(cloum)).collect(Collectors.toList());
        List<String> voCloumnLists = Arrays.asList(voCloumns).parallelStream().distinct().filter(cloum -> StringUtils.isNotBlank(cloum) && !cloum.contains("jdbcType")).collect(Collectors.toList());
        System.out.println("cloumnLists : "+JSON.toJSONString(cloumnLists) + " voCloumnLists : "+ JSON.toJSONString(voCloumnLists));
        if (cloumnLists.size()-1 !=  voCloumnLists.size()) {
            // 特殊场景处理, 当没有括号中间有values值时,会将values也归入表列数组中,当两个数量不相等时强制剔除一个
            cloumnLists.remove(cloumnLists.size()-1);
        }
        // 没有id主键的自动补齐主键
        if (!cloumnLists.contains(SELF_INCRECE_PRIMARY_KEY)) {
            cloumnLists.add(SELF_INCRECE_PRIMARY_KEY);
            voCloumnLists.add(SELF_INCRECE_PRIMARY_KEY);
        }

        String table = cloumnLists.get(0);
        // 统计对同一表有重复创建表的sql,需要用户手动合并两个表
        if (tableAppearFrequency.containsKey(table)) {
            tableAppearFrequency.put(table, tableAppearFrequency.get(table)+1);
        } else {
            tableAppearFrequency.put(table, 1);
        }
        // 表存在则删除表
        sb.append("\n").append("/**************************[ "+ table+" ]***************************/\n");
        sb.append("DROP TABLE IF EXISTS ").append(table).append(";\n");
        // 创建表头语句
        sb.append("CREATE TABLE ").append(table).append("(");
        // 移除第一个表明位置
        cloumnLists.remove(0);

        try {
            String cloumn = createCloumns(cloumnLists, voCloumnLists, paramClass, table);
            sb.append(cloumn);
        } catch (Exception e) {
            System.out.println("createCloumns Exception , Message : "+e.getMessage());
            System.out.println("cloumnLists : "+cloumnLists + " voCloumnLists : "+voCloumnLists + " paramClass : "+paramClass.toString());
        }
        // 创建表尾部
        sb.append(");");
        boolean haveAppearFrequencyInsert = combinaTableAppearFrequency.containsKey(table);
        if (haveAppearFrequencyInsert) {
            Map<String, String> tableInfo = combinaTableAppearFrequency.get(table);
            tableInfo.put(COLUMN_PLACEHOLDER, sb.toString());
            combinaTableAppearFrequency.put(table, tableInfo);
            return "";
        } else {
            return sb.toString();
        }
    }

    /**
     * 根据表字段和vo映射字段生成表列字段
     * @param cloumnLists 表字段
     * @param voCloumnLists vo映射字段
     * @param paramClass vo映射入参对象
     * @return
     */
    private String createCloumns(List<String> cloumnLists, List<String> voCloumnLists, Class[] paramClass, String table) {
        // 出现多条插入语句使用map对列名去重
        boolean haveAppearFrequencyInsert = combinaTableAppearFrequency.containsKey(table);
        Map<String, String> frequencyInsertcolumn = combinaTableAppearFrequency.get(table);
        // 创建sql列名部分
        StringBuffer sbCloumns = new StringBuffer();
        for (int i = 0; i < cloumnLists.size(); i++ ) {
            String cloumn = cloumnLists.get(i);
            String voCloumn = voCloumnLists.get(i);
            String jdbcType=null;
            for (Class clazz : paramClass) {
                // 根据vo字段动态构建jdbcType
                try {
                    jdbcType = getJdbcType(clazz, voCloumn);
                    if (null != jdbcType) {
                        break;
                    }
                } catch (NoSuchFieldException e) {
                    System.out.println("clazz : "+clazz.getName() + " voCloumn : "+voCloumn);
                    e.printStackTrace();
                }
            }
            boolean isSelfIncreceKey = Objects.equals(cloumn, SELF_INCRECE_PRIMARY_KEY);
            // 如果为id主键,增加自增定义
            jdbcType = isSelfIncreceKey ? jdbcType + SELF_INCRECE_PRIMARY_VALUE : jdbcType;
            if (haveAppearFrequencyInsert) {
                // 有重复的插入语句使用map去重
                frequencyInsertcolumn.put(cloumn, jdbcType);
            } else {
                sbCloumns = buildCloumns(sbCloumns, cloumn, jdbcType);
            }
        }
        if (haveAppearFrequencyInsert) {
            combinaTableAppearFrequency.put(table, frequencyInsertcolumn);
            // 返回特定占位符,方便替换
            return COLUMN_PLACEHOLDER;
        } else {
            return sbCloumns.toString();
        }
    }

    /**
     * 构建表列参数,可以将主键Id置为第一列
     * @param sbCloumns 已经组装的一部分列数据
     * @param cloumn mysql数据字段名
     * @param jdbcType mysql数据类型
     * @return
     */
    private StringBuffer buildCloumns(StringBuffer sbCloumns, String cloumn, String jdbcType) {
        // 是否为id主键
        boolean isSelfIncreceKey = Objects.equals(cloumn, SELF_INCRECE_PRIMARY_KEY);
        // 将主键放在第一列
        if (isSelfIncreceKey) {
            StringBuffer selfIncreceKeySb = new StringBuffer();
            selfIncreceKeySb.append("\n")
                    .append(cloumn)
                    .append(" ")
                    .append(jdbcType)
                    .append(",")
                    .append(sbCloumns);
            sbCloumns = selfIncreceKeySb;
        } else {
            if (!"".equals(sbCloumns.toString())) {
                sbCloumns.append(",");
            }
            sbCloumns.append("\n")
                    .append(cloumn)
                    .append(" ")
                    .append(jdbcType);
        }
        return sbCloumns;
    }

    /**
     * 将生成的sql写入文件
     * @param sql 生成的创建表sql语句
     * @param sqlFilePath 生成的文件位置
     */
    private void writeSql2File(String sql, String sqlFilePath) {
        File sqlFile = new File(sqlFilePath);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            if (!sqlFile.exists()) {
                boolean hasFile = sqlFile.createNewFile();
                if(hasFile){
                    System.out.println("file not exists, create new file");
                }
                fos = new FileOutputStream(sqlFile);
            } else {
//                System.out.println("file exists");
                fos = new FileOutputStream(sqlFile, true);
            }
            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(sql); //写入内容
            osw.write("\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        }finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 替换dateformat日期类型为默认字段createTime
     * @param sqlStr
     * @return
     */
    private String replaceDateFormatSql(String sqlStr) {
        StringBuffer stringBuffer = new StringBuffer();
        boolean sqlContainsDateFormat = sqlStr.contains("date_format");
        if (!sqlContainsDateFormat) {
            return sqlStr;
        }
        // 开始部分部分到date_format
        String startSql = sqlStr.substring(0, sqlStr.indexOf("date_format("));
        // 结束部分从第一个sysdate()开始 +9为减去长度, +1为减去括号的长度
        String endSql = sqlStr.substring(sqlStr.indexOf(")", sqlStr.indexOf("sysdate()")+9) +1);
        stringBuffer.append(startSql)
                .append("#{createTime}")
                .append( replaceDateFormatSql(endSql));
        return stringBuffer.toString();
    }

    /**
     * 获取插入语句into开始位置
     * @param sql
     * @return
     */
    private int indexOfInto(String sql){
        return sql.indexOf("into")+4;
    }

    /**
     * 获取vo映射字段 #{的开始位置
     * @param sql
     * @param isStart 获取开始位置还是结束位置
     * @return
     */
    private int indexOfCloumn(String sql, boolean isStart){

        if (isStart) {
            return sql.indexOf("#{");
        } else {
            if (sql.indexOf(")") != -1) {
                return sql.indexOf(")");
            } else {
                // 兼容没有括号的sql
                return sql.indexOf("#{")-1;
            }
        }
    }

    /**
     * 通过获取到的voCloumn字段映射到vo中获取对应类型
     * @param clazz 对应vo入参对象
     * @param voCloumn 列名映射字段名
     * @return
     */
    private String getDeclaredField (Class<?> clazz, String voCloumn) {
        Field[] fields = clazz.getDeclaredFields();
        String fieldType = null;
        for (Field field : fields) {
            if(voCloumn.equalsIgnoreCase(field.getName())) {
                fieldType =  field.getGenericType().getTypeName();
                break;
            }
        }
        if (StringUtils.isBlank(fieldType)) {
            System.out.println("clazz : "+ clazz.getName() +", voCloumn : "+voCloumn);
            // java.lang.Object往上没有父类了,再递归调用会有空指针,需要提前返回
            if ( null == clazz.getSuperclass() || "java.lang.Object".equals(clazz.getSuperclass().getName())) {
                return "java.lang.Object";
            }
            return getDeclaredField(clazz.getSuperclass(), voCloumn);
        }
        return fieldType;
    }

    /**
     * 根据映射vo的字段名和mySql中的类型一一对应转换
     * @param clazz 映射vo的对象
     * @param voCloumn 映射vo的字段名
     * @return
     * @throws NoSuchFieldException
     */
    private String getJdbcType(Class<?> clazz, String voCloumn) throws NoSuchFieldException {
        // 含有点的数据,从点取到最后
        voCloumn = voCloumn.contains(".") ? voCloumn.substring(voCloumn.indexOf(".")+1) : voCloumn;
        String fieldType = getDeclaredField(clazz, voCloumn);
        switch (fieldType){
            case "java.lang.String":
            case "java.lang.Object":
                return DEFAULT_VARCHAR_LENGTH;
            case  "java.lang.Character":
            case "char":
                return "CHAR";
            case "java.math.BigInteger":
                return "bigint(20)";
            case "java.lang.Integer":
            case "int":
                return "int(11)";
            case "java.lang.Long":
            case "long":
                return "int(20)";
            case "java.math.BigDecimal":
                return"decimal(10,2)";
            case "java.lang.Number":
                return "decimal(10,2)";
            case "java.lang.Double":
                return"double";
            case "java.lang.Boolean":
            case "boolean":
                return"tinyint(1)";
            case "java.lang.Float":
            case "float":
                return "float";
            case "java.util.Date":
                return "datetime";
            case "java.sql.Timestamp":
                return "timestamp";
            default:
                return null;
        }
    }

    /**
     * 该方法暂时无用
     * @param doc
     * @param alias
     * @return
     * @throws JDOMException
     */
//    private String getTableName(Document doc, String alias) throws JDOMException {
//        String startPath = "//typeAlias[@alias=\"";
//        String endPath = "\"]";
//        String tableName = "";
//        String classPath = null;
//        // 这里的alias可能是一个别名，也可能是一个java类路径，这里我通过该alias是否有点"."这个符号来区别
//        if (alias.indexOf(".") > 0) {// 是JAVA类
//            classPath = alias;
//        } else {// 是别名，就到配置的别名中去找
//            String path = startPath+alias+endPath;
//            Element aliasElement = (Element) XPath.selectSingleNode(doc, path);
//            classPath = aliasElement.getAttributeValue("type");
//        }
//        String[] classPathArray = classPath.split("\\.");
//        // 取到DO的名称
//        classPath = classPathArray[classPathArray.length - 1];
//        int i = classPath.lastIndexOf("DO");
//        // 取到根据表名生成的DO名称，无“DO”两个字符
//        classPath = classPath.substring(0, i);
//        char[] chars = classPath.toCharArray();
//        boolean isFirst = Boolean.TRUE;
//        // 生成真实的表名
//        for (char c : chars) {
//            if (!isFirst && c >= 65 && c <= 90) {
//                tableName += "_";
//            }
//            if (isFirst) {
//                isFirst = Boolean.FALSE;
//            }
//            tableName += c;
//        }
//        // 表名转换为大写返回
//        return tableName.toUpperCase();
//    }

    /**
     * 过滤性阅读
     *
     * @param filePath 文件路径
     * @param notIncludeLineStartWith 不包括的字符，即某行的开头是这样的字符串，则在读取的时候该行忽略
     * @return
     * @throws IOException
     */
    private String filterRead(String filePath, String notIncludeLineStartWith) throws IOException {
        String result = "";
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (line != null) {
            if (!line.startsWith(notIncludeLineStartWith)) {
                result += line;
            }
            line = br.readLine();
            if (line != null && !line.startsWith(notIncludeLineStartWith)) {
                result += "\n";
            }
        }
        br.close();
        fr.close();
        return result;
    }

    /**
     * 根据XML 字符串 建立JDom的Document对象
     *
     * @param xmlString XML格式的字符串
     * @return Document 返回建立的JDom的Document对象，建立不成功将抛出异常。
     * @throws IOException
     * @throws JDOMException
     */
    private Document getDocument(String xmlString) throws JDOMException, IOException {
        System.out.println("xmlString : "+xmlString);
        SAXBuilder builder = new SAXBuilder();
        Document anotherDocument = builder.build(new StringReader(xmlString));
        return anotherDocument;

    }

}

