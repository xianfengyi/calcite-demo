<#--
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to you under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
-->
SqlNode SqlCreateMaterializedView() :
{
    SqlParserPos pos;
    SqlIdentifier viewName;
    boolean existenceCheck = false;
    SqlSelect query;
}
{
    <CREATE> { pos = getPos(); }
    <MATERIALIZED> <VIEW>
    <#-- [] 代表里面的元素可能出现 -->
        [ <IF> <NOT> <EXISTS> { existenceCheck = true; } ]
    <#-- CompoundIdentifier() 为 Calcite 内置函数，
    可以解析类似 catalog.schema.tableName 这样的全路径表示形式 -->
    viewName = CompoundIdentifier()
    <AS>
    <#-- SqlSelect() 为 Calcite 内置函数，解析一个 select sql -->
    query = SqlSelect()
    {
        return new CreateMaterializedView(pos, viewName, existenceCheck, query);
    }
}