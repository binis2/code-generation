package net.binis.codegen.generation.core;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class Constants {

    public static final String MODIFIER_METHOD_NAME = "with";
    public static final String MODIFIER_INTERFACE_NAME = "Modify";
    public static final String MODIFIER_CLASS_NAME_SUFFIX = MODIFIER_INTERFACE_NAME + "Impl";
    public static final String MODIFIER_DONE_METHOD_NAME = "done";
    public static final String MIXIN_MODIFYING_METHOD_PREFIX = "as";
    public static final String MODIFIER_FIELD_GENERIC = "T";

    public static final String MODIFIER_KEY = "Modifier";
    public static final String MODIFIER_INTF_KEY = MODIFIER_KEY + "Intf";
    public static final String MODIFIER_FIELDS_KEY = "ModifierFields";

    public static final String EMBEDDED_MODIFIER_KEY = "Embedded" + MODIFIER_KEY;
    public static final String EMBEDDED_MODIFIER_INTF_KEY = EMBEDDED_MODIFIER_KEY + "Intf";

    public static final String QUERY_KEY = "Query";
    public static final String QUERY_EXECUTOR_KEY = QUERY_KEY + "Executor";
    public static final String QUERY_ORDER_KEY = QUERY_KEY + "Order";
    public static final String QUERY_ORDER_INTF_KEY = QUERY_KEY + QUERY_ORDER_KEY + "Intf";
    public static final String QUERY_NAME_KEY = QUERY_KEY + "Name";
    public static final String QUERY_NAME_INTF_KEY = QUERY_KEY + QUERY_NAME_KEY + "Intf";

    public static final String CLONE_METHOD = "clone";

}
